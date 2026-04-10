package org.example.leprojet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue du damier 10×10.
 * <p>
 * Gère le rendu graphique (via {@link CaseRenderer}) et la sélection au clic.
 * Le coup est délégué à un {@link CoupCallback} (local ou réseau).
 */
public class DamierView extends BorderPane {

    private Plateau plateau;
    private final GridPane grille;
    private final arbitre arb;
    private final Couleur couleurJoueur;

    private Piece pieceSelectionnee;
    private final List<Case> destinationsPossibles = new ArrayList<>();

    /** Callback coup (mode local ou réseau). */
    private CoupCallback coupCallback;

    /** Callback post-coup pour rafraîchir d'autres vues. */
    private Runnable onCoupJoue;

    // ─────────────────────────────────────────────────────────────────
    //  Constructeurs
    // ─────────────────────────────────────────────────────────────────

    public DamierView() {
        this(null, null, null);
    }

    public DamierView(Plateau plateau) {
        this(plateau, null, null);
    }

    public DamierView(Plateau plateau, arbitre arb, Couleur couleurJoueur) {
        this.arb = arb;
        this.couleurJoueur = couleurJoueur;
        this.plateau = (plateau != null) ? plateau : creerPlateauParDefaut();

        this.grille = new GridPane();
        grille.setAlignment(Pos.CENTER);
        grille.setHgap(0);
        grille.setVgap(0);
        grille.setStyle("-fx-background-color: #3f2a1d; -fx-padding: 4;");

        dessinerGrille();

        setStyle("-fx-background-color: transparent;");
        setCenter(grille);
        setPadding(new Insets(4));
    }

    private Plateau creerPlateauParDefaut() {
        Plateau p = new Plateau();
        p.initPions();
        return p;
    }

    // ─────────────────────────────────────────────────────────────────
    //  Setters
    // ─────────────────────────────────────────────────────────────────

    public void setOnCoupJoue(Runnable r)        { this.onCoupJoue = r; }
    public void setCoupCallback(CoupCallback cb) { this.coupCallback = cb; }

    public void setPlateau(Plateau plateau) {
        this.plateau = plateau;
        rafraichir();
    }

    // ─────────────────────────────────────────────────────────────────
    //  Rafraîchissement
    // ─────────────────────────────────────────────────────────────────

    /** Efface la sélection et redessine. Auto-sélectionne en chaîne. */
    public void rafraichir() {
        pieceSelectionnee = null;
        destinationsPossibles.clear();
        autoSelectChaine();
        redessinGrille();
    }

    private void redessinGrille() {
        grille.getChildren().clear();
        dessinerGrille();
    }

    private void autoSelectChaine() {
        if (arb != null && arb.isEnChaineDePrise()
                && couleurJoueur != null
                && arb.getJoueurCourant().getCouleur() == couleurJoueur) {
            pieceSelectionnee = arb.getPieceEnChaine();
            calculerDestinations(pieceSelectionnee);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Dessin (délègue à CaseRenderer)
    // ─────────────────────────────────────────────────────────────────

    private void dessinerGrille() {
        Case[][] cases = plateau.getCases();
        for (int l = 0; l < Plateau.NB_LIGNES; l++) {
            for (int c = 0; c < Plateau.NB_COLONNES; c++) {
                Case cs = cases[l][c];
                boolean selected = pieceSelectionnee != null && pieceSelectionnee.getPosition() == cs;
                boolean isDest = destinationsPossibles.contains(cs);

                StackPane cell = CaseRenderer.creerCaseNode(cs, selected, isDest);
                cell.setOnMouseClicked(e -> onClic(cs));
                grille.add(cell, c, l);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Destinations possibles
    // ─────────────────────────────────────────────────────────────────

    private void calculerDestinations(Piece piece) {
        destinationsPossibles.clear();
        if (arb == null || piece == null) return;

        if (arb.isEnChaineDePrise()) {
            destinationsPossibles.addAll(arb.getPrisesPossiblesPour(piece));
            return;
        }

        if (arb.joueurCourantAPrisePossible()) {
            destinationsPossibles.addAll(arb.getPrisesPossiblesPour(piece));
        } else {
            destinationsPossibles.addAll(
                    MoveCalculator.getDeplacementsPossibles(piece, plateau));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Logique de clic
    // ─────────────────────────────────────────────────────────────────

    private void onClic(Case cs) {
        if (arb == null || arb.getEtat() != EtatPartie.EN_COURS) return;
        if (couleurJoueur != null && arb.getJoueurCourant().getCouleur() != couleurJoueur) return;

        // Chaîne de prises
        if (arb.isEnChaineDePrise()) {
            gererClicChaine(cs);
            return;
        }

        // Sélection / jeu normal
        if (pieceSelectionnee == null) {
            selectionnerPiece(cs);
        } else {
            jouerOuReselectionner(cs);
        }
    }

    private void selectionnerPiece(Case cs) {
        Piece p = cs.getPiece();
        if (p == null || p.getCouleur() != couleurJoueur) return;
        if (arb.joueurCourantAPrisePossible() && arb.getPrisesPossiblesPour(p).isEmpty()) return;

        pieceSelectionnee = p;
        calculerDestinations(p);
        redessinGrille();
    }

    private void jouerOuReselectionner(Case cs) {
        // Re-sélection d'une autre de ses pièces
        if (cs.getPiece() != null && cs.getPiece().getCouleur() == couleurJoueur) {
            Piece p = cs.getPiece();
            if (arb.joueurCourantAPrisePossible() && arb.getPrisesPossiblesPour(p).isEmpty()) return;
            pieceSelectionnee = p;
            calculerDestinations(p);
            redessinGrille();
            return;
        }

        // Tenter le coup
        int lDep = pieceSelectionnee.getPosition().getLigne();
        int cDep = pieceSelectionnee.getPosition().getColonne();
        boolean ok = tenteCoup(pieceSelectionnee, cs);

        if (ok) {
            envoyerCoup(lDep, cDep, cs.getLigne(), cs.getColonne());
            apresCoupe();
        } else {
            pieceSelectionnee = null;
            destinationsPossibles.clear();
            redessinGrille();
        }
    }

    private void gererClicChaine(Case cs) {
        Piece chain = arb.getPieceEnChaine();
        if (!cs.estVide() || !destinationsPossibles.contains(cs)) return;

        int lDep = chain.getPosition().getLigne();
        int cDep = chain.getPosition().getColonne();

        // Trouver la pièce prise sur le chemin (pion ou dame)
        Piece milieu = MoveCalculator.trouverPiecePrise(chain, cs, plateau);
        if (milieu == null) return;

        boolean ok = arb.jouerPrise(chain, milieu, cs);
        if (ok) {
            SoundManager.playCapture();
            envoyerCoup(lDep, cDep, cs.getLigne(), cs.getColonne());
            apresCoupe();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Exécution du coup via l'arbitre
    // ─────────────────────────────────────────────────────────────────

    private boolean tenteCoup(Piece piece, Case dest) {
        // Tenter une prise
        Piece piecePrise = MoveCalculator.trouverPiecePrise(piece, dest, plateau);
        if (piecePrise != null) {
            boolean ok = arb.jouerPrise(piece, piecePrise, dest);
            if (ok) {
                SoundManager.playCapture();
                return true;
            }
        }

        // Sinon déplacement simple
        boolean ok = arb.jouerDeplacement(piece, dest);
        if (ok) {
            SoundManager.playMove();
        }
        return ok;
    }

    private void envoyerCoup(int lDep, int cDep, int lArr, int cArr) {
        if (coupCallback != null) {
            coupCallback.onCoup(lDep, cDep, lArr, cArr);
        }
    }

    /**
     * Après un coup validé : rafraîchir la grille DANS TOUS LES CAS,
     * gérer la chaîne de prises, et notifier le callback.
     */
    private void apresCoupe() {
        if (arb.isEnChaineDePrise()) {
            pieceSelectionnee = arb.getPieceEnChaine();
            calculerDestinations(pieceSelectionnee);
        } else {
            pieceSelectionnee = null;
            destinationsPossibles.clear();
        }

        // ★ FIX : toujours redessiner la grille après un coup
        redessinGrille();

        // Vérifier fin de partie
        if (arb.getGagnant() != null) {
            SoundManager.playVictory();
        }

        if (onCoupJoue != null) onCoupJoue.run();
    }
}
