package org.example.leprojet.core;

/**
 * Représente une dame de dames.
 */
public class Dame extends Piece {

    /**
     * Construit une dame.
     *
     * @param couleur couleur de la dame
     * @param position case initiale
     */
    public Dame(Couleur couleur, Case position) {
        super(couleur, position);

    }

    @Override
    public String toString() {
        return "Dame [couleur=" + this.couleur + "]";
    }

}
