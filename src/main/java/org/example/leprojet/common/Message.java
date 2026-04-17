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
    private String pseudoBlanc;
    private String pseudoNoir;
    private long timestamp;

    // ── Constructeur texte (rétro-compatible) ──────────────────────────

    public Message(String sender, String content) {
        this.type = MessageType.TEXTE;
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    // ── Constructeur typé sans données supplémentaires ─────────────────

    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public static Message texte(String sender, String content) {
        return new Message(MessageType.TEXTE, sender, content);
    }

    public static Message helloPseudo(String pseudo) {
        return new Message(MessageType.HELLO_PSEUDO, pseudo, pseudo);
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

    public static Message infosJoueurs(String pseudoBlanc, String pseudoNoir) {
        Message m = new Message(MessageType.INFOS_JOUEURS, "Serveur", "");
        m.pseudoBlanc = pseudoBlanc;
        m.pseudoNoir = pseudoNoir;
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

    public static Message abandon(String couleurAbandonne) {
        Message m = new Message(MessageType.ABANDON, "", "ABANDON");
        m.couleur = couleurAbandonne;
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
    public String getPseudoBlanc()   { return pseudoBlanc; }
    public String getPseudoNoir()    { return pseudoNoir; }
    public long getTimestamp()       { return timestamp; }

    // ── Setters ────────────────────────────────────────────────────────

    public void setSender(String sender) { this.sender = sender; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setPseudoBlanc(String pseudoBlanc) { this.pseudoBlanc = pseudoBlanc; }
    public void setPseudoNoir(String pseudoNoir) { this.pseudoNoir = pseudoNoir; }

    public String toDebugJson() {
        return "{"
                + "\"type\":\"" + type + "\","
                + "\"sender\":\"" + safe(sender) + "\","
                + "\"content\":\"" + safe(content) + "\","
                + "\"ligneDepart\":" + ligneDepart + ","
                + "\"colonneDepart\":" + colonneDepart + ","
                + "\"ligneArrivee\":" + ligneArrivee + ","
                + "\"colonneArrivee\":" + colonneArrivee + ","
                + "\"couleur\":\"" + safe(couleur) + "\","
                + "\"pseudoBlanc\":\"" + safe(pseudoBlanc) + "\","
                + "\"pseudoNoir\":\"" + safe(pseudoNoir) + "\","
                + "\"timestamp\":" + timestamp
                + "}";
    }

    private String safe(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public String toString() {
        return switch (type) {
            case COUP, COUP_VALIDE -> type + " (" + ligneDepart + "," + colonneDepart
                    + ")→(" + ligneArrivee + "," + colonneArrivee + ")";
            case ASSIGNATION_COULEUR -> "COULEUR=" + couleur;
            case INFOS_JOUEURS -> "JOUEURS BLANC=" + pseudoBlanc + " NOIR=" + pseudoNoir;
            case FIN_PARTIE -> "FIN gagnant=" + couleur;
            case ABANDON -> "ABANDON couleur=" + couleur;
            default -> sender + " : " + content;
        };
    }
}