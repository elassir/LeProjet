package org.example.leprojet.server;
import org.example.leprojet.common.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ConnectedClient> clients;

    public Server(int port) throws IOException {
        this.port = port;
        this.clients = new ArrayList<>();

        new Thread(new Connection(this)).start();
    }

    public int getPort() { return port; }
    public int getNumClients() { return clients.size(); }

    public synchronized void addClient(ConnectedClient newClient) {
        clients.add(newClient);
        broadcastMessage(new Message("Serveur", "Nouveau client connecté (ID: " + newClient.getId() + ")"), -1); // [cite: 159]
    }

    public synchronized void broadcastMessage(Message mess, int id) {
        for (ConnectedClient client : clients) {
            if (client.getId() != id) {
                client.sendMessage(mess);
            }
        }
    }

    public synchronized void disconnectedClient(ConnectedClient discClient) {
        discClient.closeClient();
        clients.remove(discClient);
        broadcastMessage(new Message("Serveur", "Le client " + discClient.getId() + " est parti."), -1);
    }
}