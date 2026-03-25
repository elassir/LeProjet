package org.example.leprojet;

/**
 * Représente l'état courant d'une partie de dames.
 */
public enum Action {

    /** Déplacement d'un pion. */
    DEPLACER,

    /** Mange un pion */
    MANGER,

    /** Promotion */
    PROMOTION,

    /** Gagne */

    GAGNE,

    /** Perd */
    PERDRE,

    /** Match nul */
    MATCH_NUL
}
