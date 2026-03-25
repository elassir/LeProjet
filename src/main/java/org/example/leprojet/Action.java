package org.example.leprojet;

/**
 * Représente l'état courant d'une partie de dames.
 */
public enum Action {

    /** Connexion nouveau client */
    CONNEXIONNOUVEAUCLIENT,

    /** Deconnexion client */
    DECONNEXIONCLIENT,

    /** Début partie. */
    DEBUTPARTIE,

    /** Déplacement d'un pion. */
    DEPLACER,

    /** Mange un pion */
    MANGER,

    /** Changement de joueur */
    CHANGEMENTJOUEUR,

    /** Promotion */
    PROMOTION,

    /** Gagne */
    GAGNE,

    /** Perd */
    PERDRE,

    /** Match nul */
    MATCH_NUL
}
