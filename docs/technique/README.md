# Documentation technique

Public cible:
- developpeur reprenant le projet,
- mainteneur,
- reviewer technique.

## Sommaire

- `docs/technique/architecture.md`: architecture, packages, points d'entree
- `docs/technique/flux-donnees.md`: flux local et reseau

Lecture recommandee pour une prise en main rapide:
1. `docs/technique/architecture.md`
2. `docs/technique/flux-donnees.md`
3. code des points d'entree (`HelloApplication`, `ClientJoueur`, `MainServer`)

## Principes de code retenus

- SOLID: classes a responsabilite claire, dependances explicites
- DRY: eviter les duplications de logique metier
- KISS: priorite a la lisibilite et a la simplicite des flux

## Etat de la documentation in-code

La base Javadoc existe deja sur plusieurs classes et a ete completee sur:
- le noyau metier (`Case`, `Piece`, `Pion`, `Dame`, `JoueurPartie`),
- le transport reseau (`Connection`, `JoueurReceive`),
- les sections de contrats fonctionnels/techniques de messagerie.

Les prochaines passes doivent conserver ce format:
- responsabilite de la classe,
- description du cycle de vie,
- contraintes et preconditions,
- details d'extension (si la classe est un point de branchement).

## Focus evolution: messagerie joueurs

Le canal reseau supporte deja les messages texte via `MessageType.TEXTE`.
Le guide d'integration complet se trouve dans `docs/technique/flux-donnees.md`, section "Guide d'integration - Messagerie entre joueurs".

