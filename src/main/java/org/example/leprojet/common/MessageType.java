package org.example.leprojet.common;

/**
 * Types de messages échangés entre le serveur et les clients.
 */
public enum MessageType {

    /** Client → Serveur : pseudo choisi au moment de la connexion. */
    HELLO_PSEUDO,

    /** Serveur → Client : couleur assignée au joueur. */
    ASSIGNATION_COULEUR,

    /** Serveur → Clients : pseudos BLANC/NOIR synchronisés. */
    INFOS_JOUEURS,

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

    /** Client → Serveur : le joueur abandonne la partie. */
    ABANDON,

    /** Client → Serveur puis Serveur → Client : demande de revanche. */
    REVANCHE_DEMANDE,

    /** Client → Serveur puis Serveur → Client : réponse à une revanche. */
    REVANCHE_REPONSE,

    /** Message texte classique (chat / info). */
    TEXTE
}
