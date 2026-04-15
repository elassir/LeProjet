# Fonctionnalites par module

## 1) Module menu

Ce module affiche l'ecran d'accueil.

Ce que voit l'utilisateur:
- un formulaire pour une partie locale (pseudo blanc, pseudo noir),
- un formulaire pour une partie reseau (hote, port, pseudo),
- des boutons pour lancer le mode choisi.

Exemple d'usage:
1. L'utilisateur saisit deux pseudos.
2. Il clique sur "Jouer en local".
3. Deux fenetres de jeu sont ouvertes, une par couleur.

## 2) Module partie locale

Ce module permet de jouer sur une seule machine.

Ce que fait le systeme:
- initialise les pieces,
- applique les regles de deplacement,
- alterne les tours,
- bloque les coups invalides,
- annonce la fin de partie.

Exemple d'usage:
1. Le joueur selectionne une piece.
2. Le systeme affiche les destinations valides.
3. Le joueur clique sur une destination.
4. Le systeme joue le coup et passe au joueur suivant (ou force la chaine de prises).

## 3) Module partie reseau

Ce module permet de jouer a distance.

Ce que fait le systeme:
- le serveur accepte 2 clients,
- assigne une couleur a chaque client,
- valide les coups recus,
- diffuse les coups valides aux deux clients.

Exemple d'usage:
1. Deux clients rejoignent le meme serveur.
2. Le serveur demarre la partie.
3. Un client joue un coup.
4. Les deux interfaces se synchronisent.

## 4) Module regles du jeu

Ce module applique les regles metier.

Regles couvertes:
- deplacements des pions,
- deplacements des dames,
- prise obligatoire,
- prise en chaine,
- promotion pion -> dame,
- fin de partie.

## 5) Module retour visuel et sonore

Ce module ameliore la lisibilite de la partie:
- surbrillance de la piece selectionnee,
- indication des destinations jouables,
- sons de deplacement, capture et victoire.

Valeur utilisateur:
- meilleure comprehension des actions possibles,
- retour immediat apres chaque coup.

