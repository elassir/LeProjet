package org.example.leprojet.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Boucle d'acceptation TCP du serveur de jeu.
 * <p>
 * Cette classe tourne dans un thread dédié et crée un {@link ConnectedClient}
 * par socket acceptée.
 */
public class Connection implements Runnable {
    private Server server;
    private ServerSocket serverSocket;

    /**
     * Crée le listener réseau du serveur.
     *
     * @param server instance serveur propriétaire
     * @throws IOException si la socket d'écoute ne peut pas être ouverte
     */
    public Connection(Server server) throws IOException {
        this.server = server;
        this.serverSocket = new ServerSocket(server.getPort());
    }

    /**
     * Accepte les clients en boucle et démarre un thread de lecture par client.
     */
    public void run() {
        while (true) {
            try {
                Socket sockNewClient = serverSocket.accept();
                ConnectedClient newClient = new ConnectedClient(server, sockNewClient);
                server.addClient(newClient);
                new Thread(newClient).start();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}