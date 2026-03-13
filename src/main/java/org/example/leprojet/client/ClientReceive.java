package org.example.leprojet.client;

import org.example.leprojet.common.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientReceive implements Runnable {

    private Client client;
    private Socket socket;

    public ClientReceive(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message mess = (Message) in.readObject();
                if (mess != null) {
                    client.messageReceived(mess);
                }
            }
        } catch (Exception e) {
            client.disconnectedServer();
        }
    }
}