package org.example.leprojet.core;

/**
 * Classe abstraite de base des pièces du jeu de dames.
 * <p>
 * Une pièce connaît sa couleur et sa case courante.
 */
public abstract class Piece {

    protected Couleur couleur;

    protected Case position;

    /**
     * Construit une pièce.
     *
     * @param couleur couleur de la pièce
     * @param position case initiale
     */
    public Piece(Couleur couleur, Case position) {
        super();
        this.couleur = couleur;
        this.position = position;
    }

    /**
     * @return couleur de la pièce
     */
    public Couleur getCouleur() {
        return this.couleur;
    }

    /**
     * @param couleur nouvelle couleur
     */
    public void setCouleur(Couleur couleur) {
        this.couleur = couleur;
    }

    /**
     * @return case courante de la pièce
     */
    public Case getPosition() {
        return this.position;
    }

    /**
     * Met à jour la case courante de la pièce.
     *
     * @param position nouvelle case
     */
    public void setPosition(Case position) {
        this.position = position;
    }

}
