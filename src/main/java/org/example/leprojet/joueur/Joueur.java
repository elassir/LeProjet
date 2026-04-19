package org.example.leprojet.joueur;

import org.example.leprojet.common.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client réseau d'un joueur de dames.
 * Gère la connexion socket et l'envoi de messages au serveur.
 */
public class Joueur {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final String pseudo;
    private final List<Message> messagesEnAttente = new ArrayList<>();
    private InterfaceGraphique view;
    private volatile boolean deconnecte;

    /** Couleur assignée par le serveur ("BLANC" ou "NOIR"). */
    private String couleurAssignee;

    public Joueur(String address, int port, String pseudo) throws IOException {
        this.socket = new Socket(address, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.pseudo = normaliserPseudo(pseudo);
        new Thread(new JoueurReceive(this, socket)).start();
        sendMessage(Message.helloPseudo(this.pseudo));
    }

    // ── Envoi ──────────────────────────────────────────────────────────

    /** Envoie un coup au serveur. */
    public void envoyerCoup(int lDep, int cDep, int lArr, int cArr) {
        System.out.println("[CLIENT][MOUVEMENT] envoi dep=(" + lDep + "," + cDep + ") arr=(" + lArr + "," + cArr + ")");
        sendMessage(Message.coup(lDep, cDep, lArr, cArr));
    }

    public boolean envoyerMessageTexte(String contenu) {
        if (contenu == null) return false;
        String nettoye = contenu.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        if (nettoye.isBlank() || nettoye.length() > 300) {
            return false;
        }
        sendMessage(Message.texte(pseudo, nettoye));
        return true;
    }

    public void abandonnerPartie() {
        String couleur = (couleurAssignee != null) ? couleurAssignee : "";
        sendMessage(Message.abandon(couleur));
    }

    public void proposerRevanche() {
        sendMessage(Message.demandeRevanche(pseudo));
    }

    public void repondreRevanche(boolean acceptee) {
        sendMessage(Message.reponseRevanche(pseudo, acceptee));
    }

    public void sendMessage(Message mess) {
        if (deconnecte) {
            return;
        }
        try {
            System.out.println("[CLIENT][SOCKET][OUT] " + mess.toDebugJson());
            out.writeObject(mess);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Réception (appelé par JoueurReceive) ───────────────────────────

    public void messageReceived(Message mess) {
        System.out.println("[CLIENT][SOCKET][IN] " + mess.toDebugJson());
        InterfaceGraphique cible;
        synchronized (this) {
            cible = view;
            if (cible == null) {
                messagesEnAttente.add(mess);
                return;
            }
        }
        cible.onMessageRecu(mess);
    }

    // ── Déconnexion ────────────────────────────────────────────────────

    public void disconnectedServer() {
        if (deconnecte) return;
        deconnecte = true;
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
    public void setView(InterfaceGraphique view) {
        List<Message> aRejouer;
        synchronized (this) {
            this.view = view;
            aRejouer = new ArrayList<>(messagesEnAttente);
            messagesEnAttente.clear();
        }
        for (Message message : aRejouer) {
            view.onMessageRecu(message);
        }
    }
    public String getPseudo()                       { return pseudo; }

    private String normaliserPseudo(String brut) {
        if (brut == null) return "Joueur";
        String nettoye = brut.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        if (nettoye.isBlank()) return "Joueur";
        return (nettoye.length() > 24) ? nettoye.substring(0, 24) : nettoye;
    }
}