package org.example.leprojet.core;

/**
 * Représente un joueur métier dans une partie de dames.
 */
public class JoueurPartie {

    int id;
    String nom;
    Couleur couleur;

    /**
     * Construit un joueur.
     *
     * @param id identifiant applicatif du joueur
     * @param nom nom affiché
     * @param couleur couleur des pièces du joueur
     */
    public JoueurPartie(int id, String nom, Couleur couleur) {
        super();
        this.id = id;
        this.nom = nom;
        this.couleur = couleur;
    }

    /**
     * @return identifiant du joueur
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id nouvel identifiant
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return nom du joueur
     */
    public String getNom() {
        return this.nom;
    }

    /**
     * @param nom nouveau nom affiché
     */
    public void setNom(String nom) {
        this.nom = nom;
    }


    /**
     * @return couleur des pièces du joueur
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

    @Override
    public String toString() {
        return "Joueur [id=" + this.id + ", nom=" + this.nom +  ", couleur=" + this.couleur + "]";
    }

}

