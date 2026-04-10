package org.example.leprojet.common;

import java.io.Serializable;

/**
 * Message échangé entre serveur et clients via ObjectStream.
 * Peut véhiculer du texte simple ou un coup de jeu de dames.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 2L;

    private MessageType type;
    private String sender;
    private String content;

    // Coordonnées d'un coup (utilisées quand type == COUP ou COUP_VALIDE)
    private int ligneDepart;
    private int colonneDepart;
    private int ligneArrivee;
    private int colonneArrivee;

    // Couleur transmise (utilisée pour ASSIGNATION_COULEUR, FIN_PARTIE…)
    private String couleur;

    // ── Constructeur texte (rétro-compatible) ──────────────────────────

    public Message(String sender, String content) {
        this.type = MessageType.TEXTE;
        this.sender = sender;
        this.content = content;
    }

    // ── Constructeur typé sans données supplémentaires ─────────────────

    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    // ── Factory : coup ─────────────────────────────────────────────────

    public static Message coup(int lDep, int cDep, int lArr, int cArr) {
        Message m = new Message(MessageType.COUP, "", "");
        m.ligneDepart = lDep;
        m.colonneDepart = cDep;
        m.ligneArrivee = lArr;
        m.colonneArrivee = cArr;
        return m;
    }

    public static Message coupValide(int lDep, int cDep, int lArr, int cArr) {
        Message m = new Message(MessageType.COUP_VALIDE, "Serveur", "");
        m.ligneDepart = lDep;
        m.colonneDepart = cDep;
        m.ligneArrivee = lArr;
        m.colonneArrivee = cArr;
        return m;
    }

    // ── Factory : assignation couleur ──────────────────────────────────

    public static Message assignationCouleur(String couleur) {
        Message m = new Message(MessageType.ASSIGNATION_COULEUR, "Serveur", couleur);
        m.couleur = couleur;
        return m;
    }

    // ── Factory : début / fin ──────────────────────────────────────────

    public static Message debutPartie() {
        return new Message(MessageType.DEBUT_PARTIE, "Serveur", "La partie commence !");
    }

    public static Message finPartie(String gagnant) {
        Message m = new Message(MessageType.FIN_PARTIE, "Serveur", gagnant);
        m.couleur = gagnant;
        return m;
    }

    // ── Getters ────────────────────────────────────────────────────────

    public MessageType getType()     { return type; }
    public String getSender()        { return sender; }
    public String getContent()       { return content; }
    public int getLigneDepart()      { return ligneDepart; }
    public int getColonneDepart()    { return colonneDepart; }
    public int getLigneArrivee()     { return ligneArrivee; }
    public int getColonneArrivee()   { return colonneArrivee; }
    public String getCouleur()       { return couleur; }

    // ── Setters ────────────────────────────────────────────────────────

    public void setSender(String sender) { this.sender = sender; }

    @Override
    public String toString() {
        return switch (type) {
            case COUP, COUP_VALIDE -> type + " (" + ligneDepart + "," + colonneDepart
                    + ")→(" + ligneArrivee + "," + colonneArrivee + ")";
            case ASSIGNATION_COULEUR -> "COULEUR=" + couleur;
            case FIN_PARTIE -> "FIN gagnant=" + couleur;
            default -> sender + " : " + content;
        };
    }
}