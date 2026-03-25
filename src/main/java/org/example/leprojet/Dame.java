package org.example.leprojet;

public class Dame extends Piece {

    public Dame(Couleur couleur, Case position) {
        super(couleur, position);

    }

    @Override
    public String toString() {
        return "Dame [couleur=" + this.couleur + "]";
    }

}