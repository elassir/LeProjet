# Architecture technique

## Vue d'ensemble

Le projet est une application JavaFX de jeu de dames avec deux modes:
- local: deux vues partagent le meme arbitre et le meme plateau,
- reseau: le serveur heberge la logique de validation, les clients affichent la vue et appliquent les mises a jour diffusees.

La communication reseau est basee sur des objets serialisables (`Message`) transmis via `ObjectInputStream` / `ObjectOutputStream`.

## Packages actuels

Arborescence de reference:

```text
src/main/java/org/example/leprojet/
|-- Arbitre.java
|-- MoveCalculator.java
|-- Coup.java
|-- CoupCallback.java
|-- HelloApplication.java
|-- TestPartieApp.java
|-- core/
|   |-- Plateau.java
|   |-- Case.java
|   |-- Piece.java
|   |-- Pion.java
|   |-- Dame.java
|   |-- Couleur.java
|   |-- EtatPartie.java
|   `-- JoueurPartie.java
|-- ui/
|   |-- DamierView.java
|   |-- CaseRenderer.java
|   |-- MenuView.java
|   `-- SoundManager.java
|-- common/
|   |-- Message.java
|   `-- MessageType.java
|-- joueur/
|   |-- ClientJoueur.java
|   |-- InterfaceGraphique.java
|   |-- Joueur.java
|   `-- JoueurReceive.java
`-- server/
    |-- MainServer.java
    |-- Server.java
    |-- Connection.java
    `-- ConnectedClient.java
```

### `org.example.leprojet`

Orchestration / application:
- `Arbitre`: moteur de regles et tour de jeu.
- `MoveCalculator`: calcul stateless des deplacements et prises possibles.
- `Coup`, `CoupCallback`: abstraction de coup et contrat callback UI.
- `HelloApplication`: entree menu principal JavaFX.
- `TestPartieApp`: simulation locale en 2 fenetres.

### `org.example.leprojet.core`

Noyau metier:
- `Plateau`: etat du damier, placement/mutation des pieces.
- `Case`: case du damier (couleur, coordonnees, piece).
- `Piece` + `Pion` / `Dame`: hierarchie des pieces.
- `Couleur`, `EtatPartie`: enums de contexte.
- `JoueurPartie`: joueur metier manipule par l'arbitre.

### `org.example.leprojet.ui`

Composants de presentation JavaFX:
- `DamierView`: interactions clic, selection, tentative de coup.
- `CaseRenderer`: rendu des cases et pieces.
- `MenuView`: ecran d'entree local/reseau.
- `SoundManager`: effets sonores non critiques.

### `org.example.leprojet.common`

Contrats de communication reseau:
- `MessageType`: taxonomie des evenements reseau.
- `Message`: payload commun (texte, coup, couleur, metadonnees).

### `org.example.leprojet.server`

Serveur de partie:
- `MainServer`: point d'entree JVM du serveur.
- `Connection`: boucle d'accept TCP.
- `ConnectedClient`: session d'un client (id, couleur, flux I/O).
- `Server`: orchestration globale (admission, arbitrage, diffusion des messages).

### `org.example.leprojet.joueur`

Client reseau:
- `ClientJoueur`: point d'entree JavaFX du client reseau.
- `Joueur`: facade socket (envoi + routage des messages recus vers la vue).
- `JoueurReceive`: thread de lecture bloquante depuis le serveur.
- `InterfaceGraphique`: adaptation UI des messages serveurs (`ASSIGNATION_COULEUR`, `COUP_VALIDE`, etc.).

## Points d'entree

- Application menu/local: `org.example.leprojet.HelloApplication`
- Application de test local: `org.example.leprojet.TestPartieApp`
- Serveur: `org.example.leprojet.server.MainServer`
- Client reseau: `org.example.leprojet.joueur.ClientJoueur`

## Cycle de vie d'une session reseau

1. Le serveur demarre (`MainServer`), instancie `Server`, puis `Connection` ecoute les sockets entrantes.
2. A chaque connexion, `ConnectedClient` est cree et enregistre dans `Server.addClient(...)`.
3. Le serveur assigne une couleur (`ASSIGNATION_COULEUR`) au client.
4. Au 2e client, le serveur initialise `Arbitre` et diffuse `DEBUT_PARTIE`.
5. Chaque coup client (`COUP`) est valide cote serveur (`traiterCoup`) puis diffuse en `COUP_VALIDE`.
6. A la fin, le serveur diffuse `FIN_PARTIE`.
7. En cas de deconnexion, `disconnectedClient(...)` nettoie la session et notifie l'autre joueur.

## Architecture reseau (vue logique)

```text
Client A (Joueur + InterfaceGraphique)      Client B (Joueur + InterfaceGraphique)
              |                                            |
              | Message (ObjectOutputStream)              |
              +--------------------+-----------------------+
                                   |
                          Server (onMessageRecu)
                                   |
                         Arbitre + MoveCalculator
                                   |
                  Broadcast MessageType.* vers les clients
```

## Guide d'integration - Messagerie entre joueurs

### Points d'extension naturels

- `Server.onMessageRecu(...)`: ajouter un routage explicite des messages de chat (direct vs broadcast).
- `ConnectedClient`: stocker les metadonnees de session utiles (pseudo, dernier ACK message).
- `Joueur`: exposer une API d'envoi de chat (ex: `envoyerMessageTexte(...)`).
- `InterfaceGraphique.onMessageRecu(...)`: gerer affichage chat, etats d'envoi et erreurs.
- `MessageType` / `Message`: ajouter les nouveaux types et champs de messagerie.

### Contrat d'interface cible (sans implementation)

```java
// Cote client
void sendMessage(String fromPlayerId, String toPlayerId, String content);

// Cote serveur
void onChatMessage(String fromPlayerId, String toPlayerId, String content);
void acknowledgeMessage(String messageId, boolean delivered, String reason);
```

### Modele de donnees suggere

```text
ChatMessage {
  messageId: String,
  senderId: String,
  receiverId: String,
  content: String,
  timestamp: long,
  sequence: long,
  status: SENT | DELIVERED | FAILED
}
```

### Vigilances techniques

- concurrence: plusieurs threads clients appellent `Server.onMessageRecu(...)`; conserver une section critique courte et deterministe,
- ordre: imposer un ordre serveur via `sequence` monotone,
- erreurs reseau: ack applicatif necessaire pour distinguer "envoye" et "recu",
- robustesse UI: toute mise a jour JavaFX doit rester dans `Platform.runLater(...)`,
- evolution protocole: maintenir retrocompatibilite tant que des clients anciens existent.

## Dependances

Definies dans `pom.xml`:
- JavaFX (`controls`, `fxml`, `media`, etc.)
- JUnit 5 pour les tests
- librairies UI additionnelles (controlsfx, formsfx, etc.)

## Ameliorations techniques planifiees

1. Extraire la messagerie de `Message` vers un objet dedie (`ChatMessage`) si le protocole grandit.
2. Introduire des DTO de transport versionnes pour limiter le couplage client/serveur.
3. Reducer le couplage UI/metier dans `DamierView` (pilotage par application service).
4. Ajouter des tests d'integration reseau (2 clients + serveur, ordre des messages, perte connexion).

