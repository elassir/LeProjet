package org.example.leprojet.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection implements Runnable {
    private Server server;
    private ServerSocket serverSocket;

    public Connection(Server server) throws IOException {
        this.server = server;
        this.serverSocket = new ServerSocket(server.getPort());
    }

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