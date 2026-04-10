package org.example.leprojet;

/**
 * Représente un coup joué sur le damier.
 * Peut être un déplacement simple ou une prise.
 */
public class Coup {

    /** Types de coups possibles. */
    public enum TypeCoup {
        DEPLACEMENT,
        PRISE
    }

    private final Case caseDepart;
    private final Case caseArrivee;
    private final Piece piecePrise; // null si déplacement simple
    private final TypeCoup type;

    /**
     * Crée un coup de déplacement simple.
     */
    public Coup(Case caseDepart, Case caseArrivee) {
        this.caseDepart = caseDepart;
        this.caseArrivee = caseArrivee;
        this.piecePrise = null;
        this.type = TypeCoup.DEPLACEMENT;
    }

    /**
     * Crée un coup de prise.
     */
    public Coup(Case caseDepart, Case caseArrivee, Piece piecePrise) {
        this.caseDepart = caseDepart;
        this.caseArrivee = caseArrivee;
        this.piecePrise = piecePrise;
        this.type = TypeCoup.PRISE;
    }

    // ── Getters ────────────────────────────────────────────────────────

    public Case getCaseDepart()   { return caseDepart; }
    public Case getCaseArrivee()  { return caseArrivee; }
    public Piece getPiecePrise()  { return piecePrise; }
    public TypeCoup getType()     { return type; }
    public boolean estPrise()     { return type == TypeCoup.PRISE; }

    public int getLigneDepart()    { return caseDepart.getLigne(); }
    public int getColonneDepart()  { return caseDepart.getColonne(); }
    public int getLigneArrivee()   { return caseArrivee.getLigne(); }
    public int getColonneArrivee() { return caseArrivee.getColonne(); }

    @Override
    public String toString() {
        return type + " (" + caseDepart.getLigne() + "," + caseDepart.getColonne()
                + ") → (" + caseArrivee.getLigne() + "," + caseArrivee.getColonne() + ")"
                + (piecePrise != null ? " [prise: " + piecePrise + "]" : "");
    }
}
