package org.example.leprojet;

import javafx.scene.shape.Circle;

public class Pion {
    private Couleur.CouleurEnum couleur;
    private int x;
    private int y;

    public Pion(Couleur.CouleurEnum couleur, int x, int y) {
        this.couleur = couleur;
        this.x = x;
        this.y = y;
    }

    public Couleur.CouleurEnum getCouleur() {
        return couleur;
    }

    public void setCouleur(Couleur.CouleurEnum couleur) {
        this.couleur = couleur;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
