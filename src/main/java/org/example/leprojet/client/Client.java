package org.example.leprojet.client;

import org.example.leprojet.common.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ClientPanel view;

    public Client(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());


        new Thread(new ClientReceive(this, socket)).start();
//        new Thread(new ClientSend(socket, out)).start();
    }

    public void sendMessage(Message mess) {
        try {
            out.writeObject(mess);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void messageReceived(Message mess) {
        if (view != null) view.printNewMessage(mess);
        else System.out.println(mess);
    }

    public void disconnectedServer() {
        try {
            if (out != null) out.close();
            socket.close();
            System.out.println("Déconnecté du serveur.");
            System.exit(0);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setView(ClientPanel view) {
        this.view = view;
    }
}