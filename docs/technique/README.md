# Documentation technique

Public cible:
- developpeur reprenant le projet,
- mainteneur,
- reviewer technique.

## Sommaire

- `docs/technique/architecture.md`: architecture, packages, points d'entree
- `docs/technique/flux-donnees.md`: flux local et reseau

## Principes de code retenus

- SOLID: classes a responsabilite claire, dependances explicites
- DRY: eviter les duplications de logique metier
- KISS: priorite a la lisibilite et a la simplicite des flux

## Etat de la documentation in-code

La base Javadoc existe deja sur plusieurs classes.
Une passe incrementale va uniformiser les sections:
- parametres,
- retours,
- exceptions,
- invariants metier.

