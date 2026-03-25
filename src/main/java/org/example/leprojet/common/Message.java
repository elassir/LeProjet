package org.example.leprojet.common;
import org.example.leprojet.Action;
import org.example.leprojet.Couleur;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private Action action;
    private String content;
    public Couleur couleurJoueur;
    private Integer colonesDepart;
    private Integer lignesDepart;
    private Integer colonesFin;
    private Integer lignesFin;

    public Message(String sender, Action action, String content) {
        this.sender = sender;
        this.action = action;
        this.content = content;
        this.couleurJoueur = null;
        this.colonesDepart = null;
        this.lignesDepart = null;
        this.colonesFin = null;
        this.lignesFin= null;
    }

    public Message(String sender, Action action, String content, Couleur couleurJoueur, Integer colonesDepart, Integer lignesDepart, Integer colonesFin, Integer lignesFin) {
        this.sender = sender;
        this.action = action;
        this.content = content;
        this.couleurJoueur = couleurJoueur;
        this.colonesDepart = colonesDepart;
        this.lignesDepart = lignesDepart;
        this.colonesFin = colonesFin;
        this.lignesFin= lignesFin;
    }



    public void setSender(String sender) { this.sender = sender; }

    public String getContent(){
        return this.content;
    }

    public Action getAction() {
        return this.action;
    }

    public Integer getColonesDepart() {
        return this.colonesDepart;
    }

    public Integer getColonesFin() {
        return this.colonesFin;
    }

    public Integer getLignesDepart() {
        return this.lignesDepart;
    }

    public Integer getLignesFin() {
        return this.lignesFin;
    }

    @Override
    public String toString() {
        return sender + " : " + content;
    }
}