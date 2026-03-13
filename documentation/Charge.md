
## 1. Présentation du projet

**Nom du projet :** Jeu de Dames JavaFX
**Langage :** Java
**Framework UI :** JavaFX 21

Le projet consiste à développer une application permettant de jouer au jeu de dames selon les règles officielles  avec une interface graphique réalisée en JavaFX.

---

## 2. Règles métier (Jeu de dames français)

- Plateau de 10×10 cases (variante française officielle)
- 20 pions par joueur (blanc / noir)
- Les pions se déplacent en diagonale vers l'avant uniquement
- Une prise est **obligatoire** si elle est possible
- En cas de choix, la **prise maximale** est obligatoire
- Un pion atteignant la dernière rangée adverse devient une **dame**
- La dame se déplace en diagonale sur toute la longueur
- La partie se termine quand un joueur n'a plus de pions ou ne peut plus jouer
- Égalité possible (50 coups sans prise = match nul)

---

## 3. Priorisation MoSCoW

### Must Have (Indispensable)
| ID  | Fonctionnalité                                  |
| --- | ----------------------------------------------- |
| M1  | Affichage du plateau 10×10 avec pions           |
| M2  | Déplacement des pions par clic souris           |
| M3  | Validation des mouvements légaux                |
| M4  | Gestion des prises (simples et multiples)       |
| M6  | Promotion pion → dame                           |
| M7  | Déplacement de la dame                          |
| M8  | Détection de fin de partie (victoire / défaite) |
| M9  | Tour par tour (joueur 1 / joueur 2)             |
| M10 | Mode multijoueur en ligne (réseau)              |


### Should Have (Important)
| ID | Fonctionnalité                        |
|----| ------------------------------------- |
| S1 | Historique des coups joués            |
| S2 | Timer par joueur                      |
| S3 | Score et statistiques de la partie    |
| S4 | Affichage du nombre de pions restants |
| S5 | Surbrillance des cases jouables       |

### Could Have (Optionnel)
| ID  | Fonctionnalité                             |
| --- | ------------------------------------------ |
| C1  | Sauvegarde / chargement de partie          |
| C2  | Sons et animations                         |
| C3  | Thèmes visuels (couleurs du plateau)       |
| C4  | Mode tournoi (plusieurs manches)           |


### Won't Have (Hors périmètre)
| ID | Fonctionnalité                 |
|----| ------------------------------ |
| W1 | Application mobile             |
| W2 | Compte utilisateur / base de données |
| W3 | Classement ELO en ligne        |
| W4 | Annuler le dernier coup        |

---

## 4. Fonctionnalités métier détaillées

### 4.1 Gestion du plateau
- Initialisation automatique des pions en début de partie
- Représentation interne via une matrice `Board[10][10]`
- Distinction visuelle case claire / case foncée
- Mise en évidence des cases de départ et d'arrivée possibles

### 4.2 Gestion des pièces
- Deux types : `Pion` et `Dame`
- Deux couleurs : `BLANC` et `NOIR`
- Promotion automatique à l'atteinte de la rangée opposée

### 4.3 Gestion des mouvements
- Calcul des mouvements légaux à chaque tour
- Blocage des mouvements illégaux
- Prises en chaîne (multiples captures en un seul tour)
- Vérification de la prise maximale


### 4.5 Fin de partie
- Détection victoire : adversaire sans pions ou bloqué
- Détection match nul : 50 coups sans capture
- Écran de résultat avec statistiques

---

## 5. Architecture technique


### 5.2 Architecture MVC

```
src/
├── main/
│   ├── java/
│   │   └── com/dames/
│   │       ├── model/
│   │       │   ├── Board.java
│   │       │   ├── Piece.java
│   │       │   ├── Pion.java
│   │       │   ├── Dame.java
│   │       │   ├── Move.java
│   │       │   ├── Player.java
│   │       │   └── GameState.java
│   │       ├── controller/
│   │       │   ├── GameController.java
│   │       │   ├── MoveValidator.java
│   │       │   └── AIController.java
│   │       ├── view/
│   │       │   ├── BoardView.java
│   │       │   ├── CellView.java
│   │       │   ├── PieceView.java
│   │       │   └── GameInfoView.java
│   │       └── App.java
│   └── resources/
│       ├── fxml/
│       │   ├── main.fxml
│       │   └── menu.fxml
│       ├── css/
│       │   └── style.css
│       └── images/
└── test/
    └── java/
        └── com/dames/
            ├── model/
            └── controller/
```


## 6. Interfaces utilisateur

### 6.1 Écrans prévus

| Écran             | Description                     |
| ----------------- | ------------------------------- |
| Menu principal    | Choix du mode de jeu, options   |
| Plateau de jeu    | Vue principale, plateau + infos |
| Écran de résultat | Vainqueur, stats, rejouer       |
| Paramètres        | Thème, timer                    |

### 6.2 Interactions utilisateur

- **Clic sur un pion** → affiche les mouvements possibles en surbrillance
- **Clic sur une case valide** → déplace le pion
- **Clic ailleurs** → désélectionne
- **Bouton Annuler** → revient au coup précédent
- **Bouton Menu** → retour au menu principal

---

## 8. Jalons de développement

| Étape                   | Contenu                                           |
| ----------------------- | ------------------------------------------------- |
| **1 - Fondations**      | Modèle de données, plateau vide, affichage JavaFX |
| **2 - Règles de base**  | Déplacements, prises, validation des coups        |
| **3 - Règles avancées** | Prise obligatoire/maximale, promotion dame        |
| **4 - Fin de partie**   | Détection victoire/défaite/nul, écran résultat    |
| **6 - Finitions**       | UI polish, sons, undo, timer, tests               |
# Règles métier - Mouvements légaux et illégaux

## Jeu de Dames Français (10×10)

---

## 1. Règles générales du plateau

| Règle | Description |
|-------|-------------|
| R-G1 | Le plateau est de 10×10 cases |
| R-G2 | Les pièces se déplacent **uniquement sur les cases noires** |
| R-G3 | Les cases noires sont numérotées de 1 à 50 |
| R-G4 | Les blancs jouent en premier |
| R-G5 | Les joueurs jouent **alternativement** |
| R-G6 | Un joueur ne peut **pas passer son tour** sauf s'il n'a aucun mouvement possible |

---

## 2. Mouvements légaux du PION

### 2.1 Déplacement simple (sans prise)

| Règle | Description |
|-------|-------------|
| R-P1 | Un pion se déplace **en diagonale** uniquement |
| R-P2 | Un pion se déplace **d'une seule case** à la fois |
| R-P3 | Un pion blanc se déplace **uniquement vers l'avant** (vers les noirs) |
| R-P4 | Un pion noir se déplace **uniquement vers l'avant** (vers les blancs) |
| R-P5 | La case d'arrivée doit être **vide** |
| R-P6 | Un pion **ne peut pas reculer** lors d'un déplacement simple |

### 2.2 Prise avec un pion

| Règle | Description |
|-------|-------------|
| R-P7 | Une prise se fait en **sautant par-dessus** une pièce adverse |
| R-P8 | La case **derrière la pièce adverse** (dans la direction du saut) doit être **vide** |
| R-P9 | Un pion peut prendre **vers l'avant ET vers l'arrière** |
| R-P10 | La pièce adverse prise est **retirée du plateau** |
| R-P11 | Après une prise, si une **autre prise est possible**, elle doit être effectuée (prise en chaîne) |
| R-P12 | Lors d'une prise en chaîne, les pièces prises ne sont retirées qu'**après la fin du tour complet** |
| R-P13 | Une pièce adverse déjà sautée dans la chaîne **ne peut pas être sautée une deuxième fois** |

---

## 3. Mouvements légaux de la DAME

### 3.1 Déplacement simple (sans prise)

| Règle | Description |
|-------|-------------|
| R-D1 | La dame se déplace en **diagonale** sur **autant de cases qu'elle veut** |
| R-D2 | La dame peut se déplacer **vers l'avant ET vers l'arrière** |
| R-D3 | Toutes les cases sur la trajectoire doivent être **vides** |
| R-D4 | La dame peut s'arrêter sur **n'importe quelle case libre** de sa diagonale |

### 3.2 Prise avec une dame

| Règle | Description |
|-------|-------------|
| R-D5 | La dame saute **par-dessus une pièce adverse** sur sa diagonale |
| R-D6 | Il peut y avoir **plusieurs cases vides** entre la dame et la pièce adverse |
| R-D7 | Après le saut, la dame peut s'arrêter sur **n'importe quelle case libre** derrière la pièce prise |
| R-D8 | Toutes les cases entre la dame et la pièce adverse doivent être **vides** |
| R-D9 | La dame peut enchaîner des prises dans **plusieurs directions différentes** |
| R-D10 | Une pièce déjà sautée dans la chaîne **ne peut pas être re-sautée** |
| R-D11 | Les pièces prises sont retirées **uniquement à la fin du tour complet** |

---

## 4. Règles de priorité des prises (obligatoires)

| Règle | Description |
|-------|-------------|
| R-PR1 | **La prise est OBLIGATOIRE** si une prise est possible, le joueur DOIT prendre |
| R-PR2 | Un joueur **ne peut pas faire un déplacement simple** s'il peut effectuer une prise |
| R-PR3 | **La prise maximale est obligatoire** : si plusieurs prises sont possibles, le joueur doit choisir celle qui capture le **plus grand nombre de pièces** |
| R-PR4 | En cas d'égalité de nombre de prises, le joueur peut **choisir librement** parmi les options maximales |
| R-PR5 | La règle de prise maximale s'applique **globalement** sur tout le tour (chaînes incluses) |
| R-PR6 | Un pion qui peut prendre **prime sur une dame** qui ferait moins de prises |

---

## 5. Promotion Pion → Dame

| Règle | Description |
|-------|-------------|
| R-PM1 | Un pion blanc atteignant la **rangée 1** (rangée des noirs) devient une dame |
| R-PM2 | Un pion noir atteignant la **rangée 10** (rangée des blancs) devient une dame |
| R-PM3 | La promotion a lieu **immédiatement** quand le pion arrive sur la dernière rangée |
| R-PM4 | Si un pion arrive sur la dernière rangée **lors d'une prise**, il est promu mais **ne continue pas** à prendre en tant que dame dans ce même tour |
| R-PM5 | La dame nouvellement promue joue normalement **au prochain tour** |

---

## 6. Mouvements ILLÉGAUX

### 6.1 Illégaux pour le pion

| Règle | Description |
|-------|-------------|
| I-P1 | Se déplacer **vers l'arrière** sans effectuer de prise |
| I-P2 | Se déplacer sur une case **occupée** (alliée ou adverse) |
| I-P3 | Se déplacer de **plus d'une case** sans prise |
| I-P4 | Se déplacer **horizontalement ou verticalement** |
| I-P5 | Sauter par-dessus une **pièce alliée** |
| I-P6 | Sauter si la case d'atterrissage est **occupée** |
| I-P7 | **Ne pas prendre** alors qu'une prise est disponible |
| I-P8 | **Ne pas maximiser** les prises quand plusieurs options existent |
| I-P9 | Sauter **deux fois** la même pièce dans une chaîne |
| I-P10 | Continuer une chaîne de prise **après la promotion** en dame |

### 6.2 Illégaux pour la dame

| Règle | Description |
|-------|-------------|
| I-D1 | Se déplacer **horizontalement ou verticalement** |
| I-D2 | Passer **par-dessus plus d'une pièce** sur une même diagonale |
| I-D3 | Passer par-dessus une **pièce alliée** |
| I-D4 | Sauter une pièce adverse si une case derrière elle est **occupée** |
| I-D5 | **Ne pas prendre** alors qu'une prise est disponible |
| I-D6 | **Ne pas maximiser** les prises quand plusieurs options existent |
| I-D7 | Sauter **deux fois** la même pièce adverse dans une chaîne |

### 6.3 Illégaux pour les deux types

| Règle | Description |
|-------|-------------|
| I-G1 | **Jouer hors tour** |
| I-G2 | Déplacer une **pièce adverse** |
| I-G3 | Jouer sur une **case blanche** |
| I-G4 | Quitter le **plateau** |
| I-G5 | Faire un déplacement simple **quand une prise est obligatoire** |

---

## 7. Conditions de fin de partie

| Règle | Description |
|-------|-------------|
| R-FP1 | Un joueur qui n'a **plus de pièces** perd la partie |
| R-FP2 | Un joueur qui **ne peut plus bouger** (toutes ses pièces sont bloquées) perd la partie |
| R-FP3 | **Match nul** si 25 coups consécutifs sont joués avec seulement des dames, sans prise |
| R-FP4 | **Match nul** si la même position se répète **3 fois** avec le même joueur à jouer |
| R-FP5 | **Match nul** si les deux joueurs s'accordent sur un nul |
| R-FP6 | **Match nul** si une dame seule ne parvient pas à battre 3 dames adverses en 16 coups |
diagrrame de classe .
diagramme sequence
diagramme de temps (gantt)
diagramme de cas d'utilisation 
