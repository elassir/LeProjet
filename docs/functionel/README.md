# Documentation fonctionnelle

Public cible:
- product owner,
- utilisateur metier,
- chef de projet,
- developpeur externe devant implementer une nouvelle feature sans accompagnement oral.

Objectif: decrire ce que fait l'application en langage clair, avec un niveau de precision suffisant pour cadrer l'evolution du produit.

## Sommaire

- `docs/functionel/modules.md`: description detaillee des fonctionnalites existantes et specification fonctionnelle de la messagerie entre joueurs.

## Perimetre fonctionnel

Le produit permet de jouer au jeu de dames francais sur un damier 10x10:
- en local (2 joueurs sur la meme machine),
- en reseau (2 joueurs connectes a un serveur),
- avec les regles de prise obligatoire et prise en chaine,
- avec gestion de l'etat de partie (attente, en cours, victoire).

## Cas d'usage principaux

1. Ouvrir l'application et choisir un mode local ou reseau.
2. Demarrer une partie locale et jouer un tour complet en respectant les regles.
3. Rejoindre un serveur et jouer contre un autre joueur avec synchronisation des coups.
4. Afficher les erreurs de coup invalide cote client.
5. Terminer automatiquement la partie quand un joueur n'a plus de pieces ou de coups legaux.
6. Envoyer et recevoir des messages texte entre joueurs (specification cible, a implementer).

## Regles metier de reference

Le detail des regles du jeu est dans `documentation/Charge.md`.

## Traceabilite implementation

- Le comportement metier est porte principalement par `Arbitre` et `MoveCalculator`.
- Le mode local est orchestre par `TestPartieApp` et `DamierView`.
- Le mode reseau est orchestre par `Server`, `ConnectedClient`, `Joueur` et `InterfaceGraphique`.

