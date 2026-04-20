# LeProjet - Jeu de dames (JavaFX)

Point d'entree du projet pour les equipes fonctionnelles et techniques.

## Documentation

- Documentation fonctionnelle: `docs/functionel/README.md`
- Documentation technique: `docs/technique/README.md`

Parcours recommande pour un nouveau developpeur:
1. Lire `docs/functionel/README.md` pour le comportement utilisateur reel.
2. Lire `docs/functionel/modules.md` pour les flux de session et regles metier.
3. Lire `docs/technique/architecture.md` pour l'architecture client/serveur, les dependances et le deep dive.
4. Lire `docs/technique/flux-donnees.md` puis les UML dans `docs/technique/uml/`.

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
- Documentation historique (cahier de charge + UML): `documentation/`

## Lancer le projet

Le build est base sur Maven (`pom.xml`).

Exemples de commandes (PowerShell):

```powershell
./mvnw.cmd clean compile
./mvnw.cmd javafx:run
```

Note: selon `pom.xml`, le plugin JavaFX utilise actuellement `org.example.leprojet.server.MainServer` comme `mainClass` par defaut. Les autres points d'entree sont documentes dans `docs/technique/architecture.md`.

