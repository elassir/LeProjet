# Fonctionnalites par module

## 1) Module menu (`MenuView`)

Ce module affiche l'ecran d'accueil et oriente l'utilisateur vers le mode local ou reseau.

Comportement attendu:
- saisie des pseudos pour le mode local,
- saisie de l'hote et du port pour le mode reseau,
- validation minimale du port,
- affichage d'un message d'erreur utilisateur si le port n'est pas numerique.

Exemple d'usage:
1. L'utilisateur saisit deux pseudos.
2. Il clique sur "Jouer en local".
3. Deux fenetres de jeu sont ouvertes, une par couleur.

## 2) Module partie locale (`TestPartieApp` + `DamierView`)

Ce module permet de jouer sur une seule machine, avec un arbitre partage.

Comportement attendu:
- initialisation de la partie au clic sur "Jouer",
- affichage des coups legalement possibles via la selection,
- refus des coups illegaux (aucune mutation de plateau),
- gestion de la prise obligatoire et de la chaine de prises,
- mise a jour immediate des deux vues et des indicateurs de tour.

Exemple d'usage:
1. Le joueur selectionne une piece.
2. Le systeme affiche les destinations valides.
3. Le joueur clique sur une destination.
4. Le systeme joue le coup et passe au joueur suivant (ou force la chaine de prises).

## 3) Module partie reseau (`Server`, `ConnectedClient`, `Joueur`, `InterfaceGraphique`)

Ce module permet de jouer a distance en mode client/serveur.

Comportement attendu:
- le serveur accepte au maximum 2 joueurs dans une partie,
- le serveur assigne NOIR au premier client puis BLANC au second,
- la partie demarre automatiquement a la connexion du second joueur,
- le serveur valide les coups recus et diffuse uniquement les coups valides,
- le client affiche les erreurs (`COUP_INVALIDE`) sans desynchroniser sa vue.

Exemple d'usage:
1. Deux clients rejoignent le meme serveur.
2. Le serveur assigne les couleurs et envoie `DEBUT_PARTIE`.
3. Un client joue un coup.
4. Les deux interfaces se synchronisent sur `COUP_VALIDE`.

## 4) Module regles du jeu (`Arbitre`, `MoveCalculator`, `Plateau`)

Ce module applique les regles metier du jeu de dames.

Regles couvertes:
- deplacements des pions,
- deplacements des dames,
- prise obligatoire,
- prise en chaine (tour conserve tant qu'une nouvelle prise est possible),
- promotion pion -> dame,
- fin de partie (plus de pieces ou plus de mouvements legaux).

## 5) Module retour visuel et sonore (`CaseRenderer`, `SoundManager`)

Ce module ameliore la lisibilite de la partie:
- surbrillance de la piece selectionnee,
- indication des destinations jouables,
- styles de statut de tour,
- sons de deplacement, capture et victoire.

Valeur utilisateur:
- meilleure comprehension des actions possibles,
- retour immediat apres chaque coup,
- reduction des erreurs de manipulation.

## 6) Messagerie entre joueurs - Specification fonctionnelle

Cette section decrit le comportement cible pour une feature de messagerie au-dessus du canal reseau existant.

### 6.1 Cas d'usage

- **Lobby pre-partie**: un joueur connecte peut envoyer un message texte au joueur deja connecte avant `DEBUT_PARTIE`.
- **En partie**: chaque joueur peut envoyer un message texte pendant son tour ou le tour adverse.
- **Fin de partie**: l'echange texte reste disponible jusqu'a fermeture de la session.
- **Destinataire**: dans la version 2 joueurs actuelle, chaque message est destine a "l'autre joueur".

### 6.2 Contraintes metier

- Taille maximale d'un message: 300 caracteres.
- Message vide ou compose uniquement d'espaces: refuse cote client et cote serveur.
- Historique en memoire: conserver au moins les 50 derniers messages cote client pour affichage.
- Modération minimale: filtrer les caracteres de controle non imprimables, conserver texte brut sinon.
- Horodatage: chaque message affiche une date/heure d'emission (source serveur recommandee).

### 6.3 Comportements attendus

- **Accuse de reception**: apres reception serveur, un ACK applicatif est renvoye a l'emetteur (succes ou echec).
- **Messages manques**:
  - si un joueur est temporairement deconnecte puis reconnecte dans la meme session, il recoit les messages non lus depuis son dernier ACK,
  - si la session est terminee, les messages sont perdus (pas de persistence disque dans cette version).
- **Deconnexion**:
  - le joueur restant voit une notification systeme de deconnexion,
  - les envois suivants retournent un echec explicite (destinataire indisponible),
  - aucun plantage UI ne doit survenir.

### 6.4 Criteres d'acceptation

1. Envoyer un message valide affiche ce message chez les deux joueurs.
2. Un message invalide (vide ou > 300 caracteres) est refuse avec feedback utilisateur.
3. L'ordre d'affichage est identique pour les deux joueurs (ordre serveur).
4. Une deconnexion d'un joueur n'empeche pas l'autre de terminer sa session proprement.

