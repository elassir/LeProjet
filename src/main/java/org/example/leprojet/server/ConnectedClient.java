package org.example.leprojet.server;

import org.example.leprojet.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectedClient implements Runnable {
    private static int idCounter = 0;
    private int id;
    private Server server;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ConnectedClient(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.id = idCounter++;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Nouvelle connexion, id = " + id);
    }

    public int getId() { return id; }

    public void sendMessage(Message mess) {
        try {
            out.writeObject(mess);
            out.flush();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Message mess = (Message) in.readObject();
                if (mess != null) {
                    mess.setSender(String.valueOf(id));
                    server.broadcastMessage(mess, id);
                }
            }
        } catch (Exception e) {
            server.disconnectedClient(this);
        }
    }

    public void closeClient() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}