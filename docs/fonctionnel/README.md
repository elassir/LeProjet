# Documentation fonctionnelle (metier)

## Objectif

Cette section décrit l'application du point de vue utilisateur, en se basant uniquement sur le code present dans `src/main/java`.

## Perimetre reel du produit

Le produit permet de jouer une partie de dames francaise 10x10 en ligne a 2 joueurs:
- connexion a un serveur via pseudo, hote et port (`MenuView`, `Joueur`),
- partie synchrone avec validation serveur (`Server`, `Arbitre`),
- affichage du damier interactif et du chat (`InterfaceGraphique`, `DamierView`),
- fin de partie, abandon et revanche (`MessageType.FIN_PARTIE`, `ABANDON`, `REVANCHE_*`).

Points importants:
- la partie est limitee a 2 clients simultanes cote serveur (`Server.addClient(...)`),
- le mode documente est le mode reseau (point d'entree `org.example.leprojet.ClientJoueur`).

## Fonctionnalites implementees

- Lobby de connexion: saisie pseudo/hote/port et verification de validite minimale (`MenuView`).
- Assignation automatique des couleurs: premier client `NOIR`, second client `BLANC` (`Server.addClient(...)`).
- Demarrage automatique de partie quand les 2 joueurs sont connectes et ont envoye leur pseudo (`HELLO_PSEUDO`, `DEBUT_PARTIE`).
- Jeu en ligne au tour par tour avec coups valides/invalide (`COUP`, `COUP_VALIDE`, `COUP_INVALIDE`).
- Regles metier: prise obligatoire, chaine de prises, promotion pion->dame, victoire si plus de pieces ou plus de mouvement (`Arbitre`, `MoveCalculator`).
- Chat en partie (messages textes limites a 300 caracteres, nettoyes, horodates) (`MessageType.TEXTE`, `Server.traiterTexte(...)`).
- Fin de partie visuelle: ecran de resultat (victoire/defaite/nul) et desactivation du damier (`InterfaceGraphique.onFinPartie(...)`).
- Abandon: un joueur peut abandonner, ce qui termine immediatement la partie (`Joueur.abandonnerPartie()`, `Server.traiterAbandon(...)`).
- Revanche: demande/reponse en fin de partie, redemarrage automatique si acceptee (`REVANCHE_DEMANDE`, `REVANCHE_REPONSE`).

## Parcours utilisateur

Les flux utilisateurs sont resumes ci-dessous a partir du comportement observe dans l'application.

En resume:
1. Le joueur lance le client et rejoint un serveur.
2. Le serveur assigne une couleur et attend 2 joueurs prets.
3. La partie commence, les coups sont joues puis valides cote serveur.
4. La partie se termine par victoire, blocage, ou abandon.
5. Les joueurs peuvent demander une revanche ou se deconnecter.

## Regles metier appliquees

Les regles metier detaillees (avec references de classes/methodes) sont documentees dans `docs/technique/architecture.md` (sections 2 et 5).

