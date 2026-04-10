package org.example.leprojet.common;

/**
 * Types de messages échangés entre le serveur et les clients.
 */
public enum MessageType {

    /** Serveur → Client : couleur assignée au joueur. */
    ASSIGNATION_COULEUR,

    /** Serveur → Clients : la partie commence. */
    DEBUT_PARTIE,

    /** Client → Serveur : le joueur tente un coup (déplacement ou prise). */
    COUP,

    /** Serveur → Clients : un coup validé par l'arbitre. */
    COUP_VALIDE,

    /** Serveur → Client : le coup envoyé était invalide. */
    COUP_INVALIDE,

    /** Serveur → Clients : la partie est terminée. */
    FIN_PARTIE,

    /** Message texte classique (chat / info). */
    TEXTE
}
