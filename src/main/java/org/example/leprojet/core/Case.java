package org.example.leprojet.core;

/**
 * Représente une case du damier.
 * <p>
 * Une case est définie par:
 * <ul>
 *   <li>sa couleur de fond (BLANC/NOIR),</li>
 *   <li>ses coordonnées (ligne, colonne),</li>
 *   <li>une éventuelle pièce occupante.</li>
 * </ul>
 */
public class Case {

    private Couleur couleur;

    private int ligne;

    private int colonne;

    private Piece piece;

    /**
     * Crée une case du damier.
     *
     * @param couleur couleur de la case
     * @param ligne index de ligne (0..9)
     * @param colonne index de colonne (0..9)
     */
    public Case(Couleur couleur, int ligne, int colonne) {
        super();
        this.couleur = couleur;
        this.ligne = ligne;
        this.colonne = colonne;
        this.piece = null;
    }

    /**
     * @return couleur de la case
     */
    public Couleur getCouleur() {
        return this.couleur;
    }

    /**
     * Modifie la couleur de la case.
     *
     * @param couleur nouvelle couleur
     */
    public void setCouleur(Couleur couleur) {
        this.couleur = couleur;
    }

    /**
     * @return pièce présente sur la case, ou {@code null} si vide
     */
    public Piece getPiece() {
        return this.piece;
    }

    /**
     * Définit la pièce occupante.
     *
     * @param piece pièce à placer, ou {@code null} pour vider la case
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * @return numéro de ligne
     */
    public int getLigne() {
        return this.ligne;
    }

    /**
     * @param ligne nouveau numéro de ligne
     */
    public void setLigne(int ligne) {
        this.ligne = ligne;
    }

    /**
     * @return numéro de colonne
     */
    public int getColonne() {
        return this.colonne;
    }

    /**
     * @param colonne nouveau numéro de colonne
     */
    public void setColonne(int colonne) {
        this.colonne = colonne;
    }

    /**
     * Indique si la case ne contient aucune pièce.
     *
     * @return {@code true} si la case est vide
     */
    public Boolean estVide() {
        return this.piece == null;
    }

    @Override
    public String toString() {
        return "Case [couleur=" + this.couleur + ", ligne=" + this.ligne + ", colonne=" + this.colonne + ", piece="
                + this.piece + "]";
    }


}

