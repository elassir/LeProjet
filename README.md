# LeProjet - Jeu de dames (JavaFX)

Point d'entree du projet pour les equipes fonctionnelles et techniques.

## Documentation

- Documentation fonctionnelle: `docs/functionel/README.md`
- Documentation technique: `docs/technique/README.md`

## Etat actuel

Le projet implemente un jeu de dames francais (10x10):
- mode local (deux joueurs sur la meme machine),
- mode reseau client/serveur,
- validation des mouvements et prises,
- promotion pion -> dame,
- detection de fin de partie.

## Structure rapide

- Code source principal: `src/main/java/org/example/leprojet`
- Ressources JavaFX: `src/main/resources/org/example/leprojet`
- Documentation historique (cahier de charge + UML): `documentation/`

## Lancer le projet

Le build est base sur Maven (`pom.xml`).

Exemples de commandes (PowerShell):

```powershell
./mvnw.cmd clean compile
./mvnw.cmd javafx:run
```

Note: la classe `mainClass` configuree dans le plugin JavaFX peut orienter le lancement vers le serveur. Voir `docs/technique/architecture.md` pour les points d'entree applicatifs.

