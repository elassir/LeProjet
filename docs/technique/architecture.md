# Architecture technique

## Vue d'ensemble

Le projet est une application JavaFX de jeu de dames avec deux modes:
- local: deux vues partagent le meme arbitre et le meme plateau,
- reseau: le serveur heberge la logique de validation, les clients affichent la vue.

## Packages actuels

### `org.example.leprojet`

Noyau metier + UI locale:
- `Plateau`, `Case`, `Piece`, `Pion`, `Dame`, `Couleur`
- `MoveCalculator` (calcul de coups)
- `arbitre` (orchestration de partie)
- `DamierView`, `CaseRenderer`, `MenuView`, `TestPartieApp`, `HelloApplication`

### `org.example.leprojet.common`

Contrats de communication reseau:
- `MessageType`
- `Message`

### `org.example.leprojet.server`

Serveur de partie:
- `MainServer` (point d'entree)
- `Server` (gestion clients + validation coups)
- `Connection`, `ConnectedClient`

### `org.example.leprojet.joueur`

Client reseau:
- `ClientJoueur` (point d'entree JavaFX)
- `Joueur` (socket client)
- `JoueurReceive` (ecoute)
- `InterfaceGraphique` (vue de partie cote client)

## Points d'entree

- Application menu/local: `org.example.leprojet.HelloApplication`
- Application de test local: `org.example.leprojet.TestPartieApp`
- Serveur: `org.example.leprojet.server.MainServer`
- Client reseau: `org.example.leprojet.joueur.ClientJoueur`

## Dependances

Definies dans `pom.xml`:
- JavaFX (`controls`, `fxml`, `media`, etc.)
- JUnit 5 pour les tests
- librairies UI additionnelles (controlsfx, formsfx, etc.)

## Ameliorations techniques planifiees

1. Renommer `arbitre` en `Arbitre` pour respecter les conventions Java.
2. Clarifier les noms entre `Joueur` metier et `joueur.Joueur` reseau.
3. Extraire les services metier de validation/orchestration pour reduire le couplage UI.
4. Uniformiser les Javadocs (parametres/retours/exceptions).

