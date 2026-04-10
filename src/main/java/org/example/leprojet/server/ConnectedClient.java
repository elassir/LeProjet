package org.example.leprojet.server;

import org.example.leprojet.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Représente un client connecté au serveur.
 * Chaque client possède un id unique et une couleur assignée.
 */
public class ConnectedClient implements Runnable {

    private static int idCounter = 0;

    private final int id;
    private final Server server;
    private final Socket socket;
    private final ObjectOutputStream out;
    private ObjectInputStream in;

    /** Couleur assignée : "BLANC" ou "NOIR" (null tant que pas assigné). */
    private String couleur;

    public ConnectedClient(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.id = idCounter++;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("[SERVEUR] Nouvelle connexion, id=" + id);
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public int getId()              { return id; }
    public String getCouleur()      { return couleur; }
    public void setCouleur(String c) { this.couleur = c; }

    // ── Envoi ──────────────────────────────────────────────────────────

    public void sendMessage(Message mess) {
        try {
            out.writeObject(mess);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Réception (boucle thread) ──────────────────────────────────────

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Message mess = (Message) in.readObject();
                if (mess != null) {
                    server.onMessageRecu(this, mess);
                }
            }
        } catch (Exception e) {
            server.disconnectedClient(this);
        }
    }

    // ── Fermeture ──────────────────────────────────────────────────────

    public void closeClient() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}