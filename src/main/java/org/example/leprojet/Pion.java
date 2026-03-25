package org.example.leprojet;


public class Pion extends Piece {

    public Pion(Couleur couleur, Case position) {
        super(couleur, position);

    }

    @Override
    public String toString() {
        return "Pion [couleur=" + this.couleur + "]";
    }

}