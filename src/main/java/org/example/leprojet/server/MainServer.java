package org.example.leprojet.server;
import java.io.IOException;

public class MainServer {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: java com.devops.server.MainServer <port>"); // [cite: 270]
            } else {
                int port = Integer.parseInt(args[0]);
                new Server(port);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}