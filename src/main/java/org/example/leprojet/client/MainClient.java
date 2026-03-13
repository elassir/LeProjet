package org.example.leprojet.client;
import java.io.IOException;

public class MainClient {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("<ip> <port>");
            } else {
                new Client(args[0], Integer.parseInt(args[1]));
            }
        } catch (IOException e) {
            System.out.println("Erreur : Impossible de se connecter au serveur.");
        }
    }
}