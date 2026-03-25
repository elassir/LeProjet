package org.example.leprojet;

public class Couleur {
    public static final Couleur BLANC = new Couleur("BLANC");
    public static final Couleur NOIR = new Couleur("NOIR");

    private final String nom;

    private Couleur(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return this.nom;
    }
}
