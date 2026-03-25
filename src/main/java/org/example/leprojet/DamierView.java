package org.example.leprojet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class DamierView extends BorderPane {

    private static final double TAILLE_CASE = 50;
    private Plateau plateau;
    private GridPane grille;

    /** Arbitre partagé (peut être null si vue passive). */
    private arbitre arb;

    /** Couleur du joueur propriétaire de cette fenêtre (null = spectateur). */
    private Couleur couleurJoueur;

    /** Pièce actuellement sélectionnée par un clic. */
    private Piece pieceSelectionnee;

    /** Cases de destination possibles pour la pièce sélectionnée. */
    private final List<Case> destinationsPossibles = new ArrayList<>();

    /** Callback exécuté après chaque coup joué (pour rafraîchir les autres vues). */
    private Runnable onCoupJoue;

    // ─────────────────────────────────────────────────────────────────────
    //  Constructeurs
    // ─────────────────────────────────────────────────────────────────────

    public DamierView() {
        this(null, null, null);
    }

    public DamierView(Plateau plateau) {
        this(plateau, null, null);
    }

    public DamierView(Plateau plateau, arbitre arb, Couleur couleurJoueur) {
        this.arb = arb;
        this.couleurJoueur = couleurJoueur;

        if (plateau != null) {
            this.plateau = plateau;
        } else {
            this.plateau = new Plateau();
            this.plateau.initPions();
        }

        this.grille = new GridPane();
        grille.setAlignment(Pos.CENTER);
        grille.setHgap(0);
        grille.setVgap(0);
        grille.setStyle("-fx-background-color: #3f2a1d; -fx-padding: 6;");

        dessinerGrille();

        setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");
        setCenter(grille);
        setPadding(new Insets(8));
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Setters / Rafraîchissement
    // ─────────────────────────────────────────────────────────────────────

    public void setOnCoupJoue(Runnable callback) { this.onCoupJoue = callback; }

    /** Rafraîchit l'affichage et efface la sélection. */
    public void rafraichir() {
        pieceSelectionnee = null;
        destinationsPossibles.clear();
        redessinGrille();
    }

    /** Rafraîchit l'affichage en conservant la sélection. */
    private void redessinGrille() {
        grille.getChildren().clear();
        dessinerGrille();
    }

    public void setPlateau(Plateau plateau) {
        this.plateau = plateau;
        rafraichir();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Dessin de la grille
    // ─────────────────────────────────────────────────────────────────────

    private void dessinerGrille() {
        Case[][] cases = plateau.getCases();
        for (int ligne = 0; ligne < Plateau.NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < Plateau.NB_COLONNES; colonne++) {
                grille.add(creerCase(cases[ligne][colonne]), colonne, ligne);
            }
        }
    }

    private StackPane creerCase(Case casePlateau) {
        Rectangle fond = new Rectangle(TAILLE_CASE, TAILLE_CASE);

        // Surligner la case sélectionnée en jaune
        boolean estSelectionnee = pieceSelectionnee != null
                && pieceSelectionnee.getPosition() == casePlateau;
        // Surligner les destinations possibles en vert
        boolean estDestination = destinationsPossibles.contains(casePlateau);

        if (estSelectionnee) {
            fond.setFill(Color.web("#ffeb3b"));           // jaune
        } else if (estDestination) {
            fond.setFill(Color.web("#81c784", 0.85));     // vert clair
        } else {
            fond.setFill(couleurCase(casePlateau));
        }

        StackPane cellule = new StackPane(fond);
        Piece piece = casePlateau.getPiece();
        if (piece != null) {
            cellule.getChildren().add(creerNoeudPiece(piece));
        }

        // Indicateur de destination (petit cercle semi-transparent)
        if (estDestination && casePlateau.estVide()) {
            Circle indicateur = new Circle(TAILLE_CASE * 0.15);
            indicateur.setFill(Color.web("#388e3c", 0.6));
            cellule.getChildren().add(indicateur);
        }

        cellule.setOnMouseClicked(event -> onCaseCliquee(casePlateau));
        return cellule;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Calcul des destinations possibles
    // ─────────────────────────────────────────────────────────────────────

    private void calculerDestinations(Piece piece) {
        destinationsPossibles.clear();
        if (arb == null || piece == null) return;

        Case[][] cases = plateau.getCases();
        for (int i = 0; i < Plateau.NB_LIGNES; i++) {
            for (int j = 0; j < Plateau.NB_COLONNES; j++) {
                Case dest = cases[i][j];
                if (!dest.estVide()) continue;
                if (dest.getCouleur() != Couleur.NOIR) continue;

                // Déplacement simple ?
                if (arb.estDeplacementValide(piece, dest)) {
                    destinationsPossibles.add(dest);
                }

                // Prise possible ?
                int dl = i - piece.getPosition().getLigne();
                int dc = j - piece.getPosition().getColonne();
                if (Math.abs(dl) == 2 && Math.abs(dc) == 2) {
                    int lm = (piece.getPosition().getLigne() + i) / 2;
                    int cm = (piece.getPosition().getColonne() + j) / 2;
                    Piece milieu = cases[lm][cm].getPiece();
                    if (milieu != null && milieu.getCouleur() != piece.getCouleur()) {
                        if (arb.estPriseValide(piece, milieu, dest)) {
                            destinationsPossibles.add(dest);
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Logique de clic
    // ─────────────────────────────────────────────────────────────────────

    private void onCaseCliquee(Case casePlateau) {
        System.out.println("[CLIC] Case (" + casePlateau.getLigne() + "," + casePlateau.getColonne()
                + ") couleurJoueur=" + couleurJoueur
                + " arb=" + (arb != null)
                + " etat=" + (arb != null ? arb.getEtat() : "null")
                + " tourDe=" + (arb != null && arb.getJoueurCourant() != null ? arb.getJoueurCourant().getCouleur() : "null")
                + " piece=" + casePlateau.getPiece());

        if (arb == null) { System.out.println("[CLIC] BLOQUÉ: arb est null"); return; }
        if (arb.getEtat() != EtatPartie.EN_COURS) { System.out.println("[CLIC] BLOQUÉ: état=" + arb.getEtat()); return; }

        // Vérifier que c'est le tour de ce joueur
        if (couleurJoueur != null && arb.getJoueurCourant().getCouleur() != couleurJoueur) {
            System.out.println("[CLIC] BLOQUÉ: pas ton tour ! tour=" + arb.getJoueurCourant().getCouleur() + " toi=" + couleurJoueur);
            return;
        }

        if (pieceSelectionnee == null) {
            // ── 1er clic : sélectionner une pièce ──────────────────────
            Piece piece = casePlateau.getPiece();
            if (piece != null && piece.getCouleur() == couleurJoueur) {
                pieceSelectionnee = piece;
                calculerDestinations(piece);
                System.out.println("[CLIC] Sélection: " + piece + " → " + destinationsPossibles.size() + " destinations");
                redessinGrille();
            } else {
                System.out.println("[CLIC] Rien à sélectionner (piece=" + piece + ")");
            }
        } else {
            // ── Clic sur une de ses propres pièces → changer la sélection
            if (casePlateau.getPiece() != null && casePlateau.getPiece().getCouleur() == couleurJoueur) {
                pieceSelectionnee = casePlateau.getPiece();
                calculerDestinations(pieceSelectionnee);
                System.out.println("[CLIC] Changement sélection: " + pieceSelectionnee + " → " + destinationsPossibles.size() + " destinations");
                redessinGrille();
                return;
            }

            // ── 2e clic : tenter de jouer le coup ──────────────────────
            boolean coupJoue = false;

            // Tenter une prise (distance 2 en diagonale)
            int dl = casePlateau.getLigne() - pieceSelectionnee.getPosition().getLigne();
            int dc = casePlateau.getColonne() - pieceSelectionnee.getPosition().getColonne();
            if (Math.abs(dl) == 2 && Math.abs(dc) == 2) {
                int lm = (pieceSelectionnee.getPosition().getLigne() + casePlateau.getLigne()) / 2;
                int cm = (pieceSelectionnee.getPosition().getColonne() + casePlateau.getColonne()) / 2;
                Piece pieceMilieu = plateau.getCases()[lm][cm].getPiece();
                if (pieceMilieu != null && pieceMilieu.getCouleur() != couleurJoueur) {
                    coupJoue = arb.jouerPrise(pieceSelectionnee, pieceMilieu, casePlateau);
                    System.out.println("[CLIC] Prise tentée → " + coupJoue);
                }
            }

            // Sinon tenter un déplacement simple
            if (!coupJoue) {
                coupJoue = arb.jouerDeplacement(pieceSelectionnee, casePlateau);
                System.out.println("[CLIC] Déplacement tenté → " + coupJoue);
            }

            pieceSelectionnee = null;
            destinationsPossibles.clear();

            if (coupJoue && onCoupJoue != null) {
                System.out.println("[CLIC] ✅ Coup joué ! Rafraîchissement des 2 fenêtres.");
                onCoupJoue.run();   // rafraîchit les 2 fenêtres
            } else {
                System.out.println("[CLIC] ❌ Coup invalide ou pas de callback.");
                redessinGrille();   // désélectionner même si coup invalide
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Rendu visuel
    // ─────────────────────────────────────────────────────────────────────

    private Color couleurCase(Case casePlateau) {
        return casePlateau.getCouleur() == Couleur.NOIR
                ? Color.web("#7b4b2a")
                : Color.web("#f4d7a1");
    }

    private Node creerNoeudPiece(Piece piece) {
        Circle pion = new Circle(TAILLE_CASE * 0.33);
        pion.setFill(piece.getCouleur() == Couleur.NOIR ? Color.web("#1e1e1e") : Color.web("#f7f7f7"));
        pion.setStroke(piece.getCouleur() == Couleur.NOIR ? Color.web("#d0a85c") : Color.web("#7b4b2a"));
        pion.setStrokeWidth(3);

        StackPane pieceView = new StackPane(pion);
        if (piece instanceof Dame) {
            Label dame = new Label("D");
            dame.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
            dame.setTextFill(piece.getCouleur() == Couleur.NOIR ? Color.web("#f5d48b") : Color.web("#5a3418"));
            pieceView.getChildren().add(dame);
        }
        return pieceView;
    }
}
