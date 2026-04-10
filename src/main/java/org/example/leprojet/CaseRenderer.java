package org.example.leprojet;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Rendu graphique d'une case et d'une pièce du damier.
 * <p>
 * Style esthétique inspiré de chess.com / lichess :
 * pièces avec dégradés, ombres portées, couronne pour les dames.
 */
public class CaseRenderer {

    /** Taille d'une case en pixels. */
    public static final double TAILLE_CASE = 54;

    private CaseRenderer() {
    }

    // ── Couleurs du thème ──────────────────────────────────────────────

    private static final Color CASE_FONCEE = Color.web("#b58863");
    private static final Color CASE_CLAIRE = Color.web("#f0d9b5");

    private static final Color SELECTION_FILL = Color.web("#f6f669", 0.75);
    private static final Color DESTINATION_FILL = Color.web("#829769", 0.45);

    // Pièces
    private static final Color PIECE_NOIRE_CENTRE = Color.web("#3d3d3d");
    private static final Color PIECE_NOIRE_BORD = Color.web("#1a1a1a");
    private static final Color PIECE_BLANCHE_CENTRE = Color.web("#ffffff");
    private static final Color PIECE_BLANCHE_BORD = Color.web("#d4d4d4");

    private static final Color CONTOUR_PIECE_NOIRE = Color.web("#8b7355");
    private static final Color CONTOUR_PIECE_BLANCHE = Color.web("#6b5a3e");

    // ── Rendu d'une case ───────────────────────────────────────────────

    /**
     * Crée le nœud graphique d'une case du damier.
     */
    public static StackPane creerCaseNode(Case cs, boolean selected, boolean isDest) {
        Rectangle fond = new Rectangle(TAILLE_CASE, TAILLE_CASE);
        fond.setArcWidth(0);
        fond.setArcHeight(0);

        // Couleur de fond
        Color baseCouleur = cs.getCouleur() == Couleur.NOIR ? CASE_FONCEE : CASE_CLAIRE;
        fond.setFill(baseCouleur);

        StackPane cell = new StackPane(fond);

        // Surbrillance sélection
        if (selected) {
            Rectangle highlight = new Rectangle(TAILLE_CASE, TAILLE_CASE);
            highlight.setFill(SELECTION_FILL);
            cell.getChildren().add(highlight);
        }

        // Pièce sur la case
        Piece piece = cs.getPiece();
        if (piece != null) {
            cell.getChildren().add(creerNoeudPiece(piece));
        }

        // Indicateur de destination (point ou cercle)
        if (isDest) {
            if (cs.estVide()) {
                // Point gris semi-transparent (style chess.com)
                Circle dot = new Circle(TAILLE_CASE * 0.14);
                dot.setFill(Color.web("#000000", 0.2));
                cell.getChildren().add(dot);
            } else {
                // Cercle autour de la pièce capturable
                Circle ring = new Circle(TAILLE_CASE * 0.44);
                ring.setFill(Color.TRANSPARENT);
                ring.setStroke(Color.web("#000000", 0.2));
                ring.setStrokeWidth(TAILLE_CASE * 0.08);
                cell.getChildren().add(ring);
            }
        }

        return cell;
    }

    // ── Rendu d'une pièce ──────────────────────────────────────────────

    /**
     * Crée le nœud graphique d'une pièce (pion ou dame) avec dégradé et ombre.
     */
    public static Node creerNoeudPiece(Piece piece) {
        boolean noir = piece.getCouleur() == Couleur.NOIR;
        double rayon = TAILLE_CASE * 0.37;

        // Cercle principal avec dégradé radial
        Circle cercle = new Circle(rayon);
        RadialGradient gradient = new RadialGradient(
                0, 0, 0.35, 0.35, 0.8, true, CycleMethod.NO_CYCLE,
                new Stop(0, noir ? PIECE_NOIRE_CENTRE : PIECE_BLANCHE_CENTRE),
                new Stop(1, noir ? PIECE_NOIRE_BORD : PIECE_BLANCHE_BORD)
        );
        cercle.setFill(gradient);

        // Contour
        cercle.setStroke(noir ? CONTOUR_PIECE_NOIRE : CONTOUR_PIECE_BLANCHE);
        cercle.setStrokeWidth(2.2);

        // Ombre portée
        DropShadow ombre = new DropShadow();
        ombre.setRadius(4);
        ombre.setOffsetX(1.5);
        ombre.setOffsetY(2.5);
        ombre.setColor(Color.web("#000000", 0.35));
        cercle.setEffect(ombre);

        StackPane sp = new StackPane(cercle);

        // Dame : couronne dorée ♛
        if (piece instanceof Dame) {
            Label crown = new Label("♛");
            crown.setFont(Font.font("System", FontWeight.BOLD, rayon * 1.1));
            crown.setTextFill(noir ? Color.web("#ffd700") : Color.web("#c8a415"));

            // Ombre interne sur le texte
            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setRadius(2);
            innerShadow.setColor(Color.web("#000000", 0.3));
            crown.setEffect(innerShadow);

            sp.getChildren().add(crown);
        }

        return sp;
    }

    // ── Couleur de fond d'une case ─────────────────────────────────────

    /**
     * Retourne la couleur de fond d'une case.
     */
    public static Color couleurFond(Case cs) {
        return cs.getCouleur() == Couleur.NOIR ? CASE_FONCEE : CASE_CLAIRE;
    }
}
