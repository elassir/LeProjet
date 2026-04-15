# LeProjet - Jeu de dames (JavaFX)

Point d'entree du projet pour les equipes fonctionnelles et techniques.

## Documentation

- Documentation fonctionnelle: `docs/functionel/README.md`
- Documentation technique: `docs/technique/README.md`

Parcours recommande pour un nouveau developpeur:
1. Lire `docs/functionel/README.md` pour le comportement produit attendu.
2. Lire `docs/functionel/modules.md` pour les cas d'usage par module, dont la specification de messagerie.
3. Lire `docs/technique/architecture.md` pour la cartographie des packages, points d'entree et responsabilites.
4. Lire `docs/technique/flux-donnees.md` pour les sequences locales/reseau et le guide d'integration de messagerie.

## Etat actuel

Le projet implemente un jeu de dames francais (10x10):
- mode local (deux joueurs sur la meme machine),
- mode reseau client/serveur,
- validation des mouvements et prises,
- promotion pion -> dame,
- detection de fin de partie,
- canal de messages reseau deja present (`MessageType.TEXTE`) servant de base a la messagerie entre joueurs.

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

