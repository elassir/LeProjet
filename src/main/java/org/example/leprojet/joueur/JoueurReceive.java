package org.example.leprojet.joueur;

import org.example.leprojet.common.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Thread de réception côté client.
 * <p>
 * Lit les objets {@link Message} envoyés par le serveur et les redirige vers
 * {@link Joueur#messageReceived(Message)}.
 */
public class JoueurReceive implements Runnable {

    private final Joueur joueur;
    private final Socket socket;

    /**
     * Crée un récepteur de messages serveur.
     *
     * @param joueur client métier propriétaire
     * @param socket socket connectée au serveur
     */
    public JoueurReceive(Joueur joueur, Socket socket) {
        this.joueur = joueur;
        this.socket = socket;
    }

    /**
     * Boucle bloquante de lecture de messages.
     * En cas d'erreur réseau, notifie le client via `disconnectedServer`.
     */
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message mess = (Message) in.readObject();
                if (mess != null) {
                    joueur.messageReceived(mess);
                }
            }
        } catch (Exception e) {
            joueur.disconnectedServer();
        }
    }
}