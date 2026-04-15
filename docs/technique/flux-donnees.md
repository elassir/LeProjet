# Flux de donnees

## 1) Flux local (sans serveur)

Composants:
- `TestPartieApp`
- `DamierView`
- `arbitre`
- `MoveCalculator`
- `Plateau`

Sequence simplifiee:
1. `DamierView` recoit un clic utilisateur.
2. La vue determine si le coup est une prise ou un deplacement.
3. La vue appelle `arbitre.jouerPrise(...)` ou `arbitre.jouerDeplacement(...)`.
4. `arbitre` valide via `MoveCalculator`, applique sur `Plateau`, gere promotion et tour.
5. La vue rafraichit l'affichage et labels.

## 2) Flux reseau (client/serveur)

Composants serveur:
- `Server`, `ConnectedClient`, `Connection`, `arbitre`

Composants client:
- `joueur.Joueur`, `JoueurReceive`, `InterfaceGraphique`, `DamierView`

Sequence simplifiee:
1. Le client envoie `Message.coup(...)` au serveur.
2. Le serveur verifie le tour, reconstruit le coup et le valide avec l'arbitre.
3. Si valide: broadcast `MessageType.COUP_VALIDE` a tous les clients.
4. Chaque client applique le coup sur son arbitre local et rafraichit la vue.
5. En fin de partie, le serveur diffuse `MessageType.FIN_PARTIE`.

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

## 4) Risques connus

- Couplage fort entre vue et logique de jeu dans certaines classes UI.
- Presence de noms ambigus (`Joueur` metier vs reseau).
- Validation en double (serveur + client) utile pour UX mais a cadrer clairement.

