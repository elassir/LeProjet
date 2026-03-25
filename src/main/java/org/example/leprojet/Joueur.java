package org.example.leprojet;

public class Joueur {

    int id;
    String nom;
    Couleur couleur;

    public Joueur(int id, String nom, Couleur couleur) {
        super();
        this.id = id;
        this.nom = nom;
        this.couleur = couleur;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public Couleur getCouleur() {
        return this.couleur;
    }

    public void setCouleur(Couleur couleur) {
        this.couleur = couleur;
    }

    @Override
    public String toString() {
        return "Joueur [id=" + this.id + ", nom=" + this.nom +  ", couleur=" + this.couleur + "]";
    }

}