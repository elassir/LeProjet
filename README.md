# LeProjet - Jeu de dames (JavaFX)

Point d'entree du projet pour les equipes fonctionnelles et techniques.

## Documentation

- Documentation fonctionnelle: `docs/fonctionnel/README.md`
- Documentation technique: `docs/technique/README.md`

Parcours recommande pour un nouveau developpeur:
1. Lire `docs/fonctionnel/README.md` pour le comportement utilisateur reel.
2. Lire `docs/technique/architecture.md` pour l'architecture client/serveur, les dependances et le deep dive.
3. Lire `docs/technique/flux-donnees.md` puis les UML dans `docs/technique/uml/`.

## Etat actuel

Le projet implemente un jeu de dames francais (10x10) en mode reseau client/serveur:
- attribution automatique des couleurs et demarrage de partie a 2 joueurs,
- validation serveur des coups (prise obligatoire, chaine de prises, promotion),
- fin de partie, abandon et flux de revanche,
- interface JavaFX avec damier interactif, chat texte et ecran de fin de partie.

## Structure rapide

- Code source principal: `src/main/java/org/example/leprojet`
- Noyau metier: `src/main/java/org/example/leprojet/core`
- Composants d'interface: `src/main/java/org/example/leprojet/ui`
- Client reseau: `src/main/java/org/example/leprojet/joueur`
- Serveur reseau: `src/main/java/org/example/leprojet/server`
- Ressources JavaFX: `src/main/resources/org/example/leprojet`
- Documentation projet: `docs/`

## Lancer le projet

Le build est base sur Maven Wrapper (`mvnw.cmd`) et JavaFX.

### Prerequis

- JDK compatible avec la configuration du projet (`source/target` a `25` dans `pom.xml`).
- PowerShell sur Windows (commandes ci-dessous).

### Demarrage rapide (serveur + clients)

1. Ouvrir un premier terminal pour le serveur.
2. Ouvrir un ou deux autres terminaux/instances IDE pour les clients.

#### 1) Lancer le serveur (terminal)

```powershell
./mvnw.cmd clean compile
./mvnw.cmd javafx:run
```

Le `javafx:run` par defaut demarre `org.example.leprojet.server.MainServer` (port `6000`).

#### 2) Lancer le client (depuis l'IDE)

- Executer la classe `src/main/java/org/example/leprojet/ClientJoueur.java`.
- Dans la fenetre de connexion, utiliser:
  - Hote: `localhost`
  - Port: `6000`
  - Pseudo: au choix

Pour jouer une partie, lancer 2 instances client et connecter les deux au meme serveur.

### Changer le port serveur (optionnel)

Le point d'entree serveur accepte un argument de port (`MainServer [port]`).
Si vous demarrez le serveur depuis l'IDE, passez l'argument programme (exemple: `7000`) puis connectez les clients sur ce port.

