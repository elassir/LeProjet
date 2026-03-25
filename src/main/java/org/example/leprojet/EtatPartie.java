package org.example.leprojet;

/**
 * Représente l'état courant d'une partie de dames.
 */
public enum EtatPartie {

    /** La partie n'a pas encore démarré. */
    EN_ATTENTE,

    /** La partie est en cours. */
    EN_COURS,

    /** Le joueur BLANC a gagné. */
    BLANC_GAGNE,

    /** Le joueur NOIR a gagné. */
    NOIR_GAGNE,

    /** Match nul (aucun mouvement possible des deux côtés). */
    MATCH_NUL
}
