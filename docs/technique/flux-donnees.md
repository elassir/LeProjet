# Flux de donnees

## 1) Flux local (sans serveur)

Composants:
- `org.example.leprojet.TestPartieApp`
- `org.example.leprojet.ui.DamierView`
- `org.example.leprojet.Arbitre`
- `org.example.leprojet.MoveCalculator`
- `org.example.leprojet.core.Plateau`

Sequence simplifiee:
1. `DamierView` recoit un clic utilisateur.
2. La vue determine si le coup est une prise ou un deplacement.
3. La vue appelle `Arbitre.jouerPrise(...)` ou `Arbitre.jouerDeplacement(...)`.
4. `Arbitre` valide via `MoveCalculator`, applique sur `Plateau`, gere promotion et tour.
5. La vue rafraichit l'affichage et labels.

## 2) Flux reseau (client/serveur)

Composants serveur:
- `org.example.leprojet.server.Server`, `ConnectedClient`, `Connection`, `Arbitre`

Composants client:
- `org.example.leprojet.joueur.Joueur`, `JoueurReceive`, `InterfaceGraphique`, `org.example.leprojet.ui.DamierView`

Sequence simplifiee:
1. Chaque client cree un `Joueur` (socket + stream de sortie) et un thread `JoueurReceive`.
2. Le serveur accepte la socket (`Connection`), cree un `ConnectedClient` puis appelle `addClient(...)`.
3. Le serveur assigne une couleur (`ASSIGNATION_COULEUR`) et, au 2e client, diffuse `DEBUT_PARTIE`.
4. Le client envoie `Message.coup(...)` au serveur.
5. Le serveur verifie le tour, reconstruit le coup et le valide avec `Arbitre`.
6. Si valide: broadcast `MessageType.COUP_VALIDE` aux deux clients, sinon `COUP_INVALIDE` au seul emetteur.
7. Chaque client applique le coup sur son arbitre local et rafraichit la vue.
8. En fin de partie, le serveur diffuse `MessageType.FIN_PARTIE`.

## 3) Contrat message

Le contrat reseau est centralise dans:
- `org.example.leprojet.common.MessageType`
- `org.example.leprojet.common.Message`

Types principaux:
- `ASSIGNATION_COULEUR`
- `DEBUT_PARTIE`
- `COUP`
- `COUP_VALIDE`
- `COUP_INVALIDE`
- `FIN_PARTIE`
- `TEXTE`

## 4) Cycle de vie d'une session de jeu

1. **Bootstrap serveur**: `MainServer` -> `Server` -> thread `Connection`.
2. **Admission clients**: `ConnectedClient` cree un id local serveur.
3. **Initialisation partie**: a 2 clients, creation de `Arbitre` et `Plateau` cote serveur.
4. **Boucle de jeu**: clients envoient des coups, serveur arbitre, diffusion d'etat via messages.
5. **Cloture**: `FIN_PARTIE` ou deconnexion, fermeture des streams et nettoyage des clients.

## 5) Guide d'integration - Messagerie entre joueurs

### 5.1 Ou brancher la feature dans le code existant

- `Server.onMessageRecu(...)`: ajouter une branche dediee aux evenements chat.
- `ConnectedClient`: ajouter des champs de suivi chat (pseudo, dernier message acquitte).
- `Joueur`: ajouter une methode d'envoi haut niveau (ex: `envoyerMessageChat(...)`).
- `InterfaceGraphique`: ajouter un composant UI chat et un handler de messages chat entrants.
- `MessageType`/`Message`: ajouter le contrat de transport pour ACK, erreurs et rattrapage.

### 5.2 Modele de donnees suggere pour un message

```text
messageId: String
senderId: String
receiverId: String
content: String
timestamp: long
sequence: long
deliveryState: SENT | DELIVERED | FAILED
```

### 5.3 Contrat d'interface propose (specification)

```java
public interface ChatService {
	void sendMessage(String fromPlayerId, String toPlayerId, String content);
	void acknowledge(String messageId, boolean delivered, String reason);
	void onMissedMessagesRequest(String playerId, long lastAckSequence);
}
```

### 5.4 Evenements a etendre / creer

- **Existants a conserver**: `TEXTE` (retrocompatibilite), `COUP_*`, `DEBUT_PARTIE`, `FIN_PARTIE`.
- **Nouveaux recommandes**:
  - `CHAT_MESSAGE` (payload chat complet),
  - `CHAT_ACK` (accuse reception positif/negatif),
  - `CHAT_ERROR` (message refuse, destinataire absent, taille invalide),
  - `CHAT_SYNC_REQUEST` (demande de messages manques),
  - `CHAT_SYNC_RESPONSE` (lot de messages manques).

### 5.5 Points de vigilance

- concurrence serveur: synchronisation de l'ordre des messages,
- ordre global: sequence monotone emise par le serveur,
- idempotence: ignorer les doublons cote client via `messageId`,
- gestion d'erreur: ne jamais bloquer la partie si le chat echoue,
- UX deconnexion: afficher une erreur explicite et laisser le client stable.

## 6) Risques connus

- Couplage fort entre vue et logique de jeu dans certaines classes UI.
- Le renommage `JoueurPartie` (metier) vs `joueur.Joueur` (reseau) reduit l'ambiguite, mais le couplage reste present dans certaines signatures.
- Validation en double (serveur + client) utile pour UX mais a cadrer clairement.
- En l'etat, `TEXTE` est diffuse en broadcast sans notion de destinataire ni ACK.

