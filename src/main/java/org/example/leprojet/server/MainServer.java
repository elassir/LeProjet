package org.example.leprojet.server;

import java.io.IOException;

/**
 * Point d'entrée du serveur de jeu de dames.
 * Usage : java MainServer [port]   (port par défaut = 6000)
 */
public class MainServer {

    public static void main(String[] args) {
        int port = 6000;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        try {
            new Server(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}