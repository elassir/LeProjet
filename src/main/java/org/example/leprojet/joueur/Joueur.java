package org.example.leprojet.joueur;

import org.example.leprojet.common.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client réseau d'un joueur de dames.
 * Gère la connexion socket et l'envoi de messages au serveur.
 */
public class Joueur {

    private final Socket socket;
    private final ObjectOutputStream out;
    private InterfaceGraphique view;

    /** Couleur assignée par le serveur ("BLANC" ou "NOIR"). */
    private String couleurAssignee;

    public Joueur(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        new Thread(new JoueurReceive(this, socket)).start();
    }

    // ── Envoi ──────────────────────────────────────────────────────────

    /** Envoie un coup au serveur. */
    public void envoyerCoup(int lDep, int cDep, int lArr, int cArr) {
        sendMessage(Message.coup(lDep, cDep, lArr, cArr));
    }

    public void sendMessage(Message mess) {
        try {
            out.writeObject(mess);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Réception (appelé par JoueurReceive) ───────────────────────────

    public void messageReceived(Message mess) {
        if (view != null) {
            view.onMessageRecu(mess);
        } else {
            System.out.println("[JOUEUR] " + mess);
        }
    }

    // ── Déconnexion ────────────────────────────────────────────────────

    public void disconnectedServer() {
        try {
            if (out != null) out.close();
            socket.close();
            System.out.println("[JOUEUR] Déconnecté du serveur.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public String getCouleurAssignee()              { return couleurAssignee; }
    public void setCouleurAssignee(String couleur)  { this.couleurAssignee = couleur; }
    public void setView(InterfaceGraphique view)    { this.view = view; }
}