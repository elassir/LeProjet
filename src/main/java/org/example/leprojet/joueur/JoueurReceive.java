package org.example.leprojet.joueur;

import org.example.leprojet.common.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

public class JoueurReceive implements Runnable {

    private Joueur joueur;
    private Socket socket;

    public JoueurReceive(Joueur joueur, Socket socket) {
        this.joueur = joueur;
        this.socket = socket;
    }

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