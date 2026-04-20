# Flux de donnees

## 1) Session en ligne: vue d'ensemble

Composants serveur:
- `server.Connection`
- `server.ConnectedClient`
- `server.Server`
- `Arbitre`

Composants client:
- `joueur.Joueur`
- `joueur.JoueurReceive`
- `joueur.InterfaceGraphique`
- `ui.DamierView`

Transport:
- socket TCP + `ObjectOutputStream` / `ObjectInputStream`,
- messages serialises via `common.Message`.

## 2) Flux connexion -> demarrage partie

1. Le serveur demarre (`MainServer`), cree `Server`, puis lance `Connection`.
2. `Connection` accepte une socket et instancie `ConnectedClient`.
3. `Server.addClient(...)`:
   - refuse si la partie est deja pleine (plus de 2),
   - assigne `NOIR` au 1er client, `BLANC` au 2e,
   - envoie `ASSIGNATION_COULEUR`.
4. Chaque client envoie `HELLO_PSEUDO`.
5. Quand 2 clients ont fourni un pseudo, `Server.demarrerPartie()`:
   - cree `Arbitre`,
   - initialise le plateau,
   - diffuse `INFOS_JOUEURS` puis `DEBUT_PARTIE`.

## 3) Flux coup de jeu

1. Le client envoie `COUP(lDep, cDep, lArr, cArr)`.
2. `Server.traiterCoup(...)` verifie:
   - partie demarree,
   - tour du joueur,
   - piece presente en case de depart.
3. Le serveur tente une prise puis un deplacement via `Arbitre`.
4. Resultat:
   - succes: broadcast `COUP_VALIDE`,
   - echec: retour `COUP_INVALIDE` au joueur emetteur.
5. Si `Arbitre` n'est plus en `EN_COURS`, le serveur diffuse `FIN_PARTIE`.

## 4) Flux de chat (`TEXTE`)

1. Le client envoie `TEXTE` via `Joueur.envoyerMessageTexte(...)`.
2. Le serveur nettoie et valide le texte (`1..300` caracteres).
3. Le serveur ajoute un horodatage (`LocalTime`) au contenu.
4. Le message est diffuse en broadcast aux 2 clients.
5. Le client affiche dans le panneau chat de `InterfaceGraphique`.

## 5) Flux abandon et revanche

### 5.1 Abandon

1. Le client envoie `ABANDON`.
2. Le serveur appelle `Arbitre.abandonner(...)` si partie en cours.
3. Le serveur diffuse `FIN_PARTIE` puis un message systeme `TEXTE`.

### 5.2 Revanche

1. En fin de partie, un joueur envoie `REVANCHE_DEMANDE`.
2. Le serveur verifie qu'une revanche est autorisee et qu'aucune demande n'est deja en attente.
3. L'adversaire recoit la demande et repond via `REVANCHE_REPONSE`.
4. Si acceptee:
   - notification des deux joueurs,
   - redemarrage via `Server.demarrerPartie()`.
5. Si refusee:
   - notification des deux joueurs,
   - pas de redemarrage.

## 6) Contrat de messages utilise

Types definis dans `MessageType` et utilises dans le code:
- `HELLO_PSEUDO`
- `ASSIGNATION_COULEUR`
- `INFOS_JOUEURS`
- `DEBUT_PARTIE`
- `COUP`
- `COUP_VALIDE`
- `COUP_INVALIDE`
- `FIN_PARTIE`
- `ABANDON`
- `REVANCHE_DEMANDE`
- `REVANCHE_REPONSE`
- `TEXTE`

## 7) Points de vigilance techniques observes

- Le serveur est synchronise au niveau de `onMessageRecu(...)` et des methodes de diffusion.
- Les clients maintiennent un arbitre local pour l'affichage, mais l'autorite metier reste serveur.


