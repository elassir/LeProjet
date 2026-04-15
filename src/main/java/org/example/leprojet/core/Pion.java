package org.example.leprojet.core;


/**
 * Représente un pion de dames.
 */
public class Pion extends Piece {

    /**
     * Construit un pion.
     *
     * @param couleur couleur du pion
     * @param position case initiale
     */
    public Pion(Couleur couleur, Case position) {
        super(couleur, position);

    }

    @Override
    public String toString() {
        return "Pion [couleur=" + this.couleur + "]";
    }

}
