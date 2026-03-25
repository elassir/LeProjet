package org.example.leprojet.joueur;

import javafx.scene.layout.StackPane;
import org.example.leprojet.DamierView;
import org.example.leprojet.common.Message;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientJoueur extends Application {
    public ClientJoueur() {}

    @Override
    public void start(Stage stage) throws IOException {
        InterfaceGraphique interfaceGraphique = new InterfaceGraphique();
        interfaceGraphique.printNewMessage(new Message("System", "Hello world!!!!!"));
        StackPane root = new StackPane();
        root.getChildren().add(new DamierView());
        root.getChildren().add(interfaceGraphique);

        Scene scene = new Scene(root, 760, 820);
        stage.setTitle("Jeu de dames");
        stage.setScene(scene);
        stage.show();

        // Arguments: host port
        String host = "127.0.0.1";
        int port = 6000;

        if (getParameters().getRaw().size() >= 2) {
            host = getParameters().getRaw().get(0);
            port = Integer.parseInt(getParameters().getRaw().get(1));
        }

        Joueur joueur = new Joueur(host, port);

        // lien bidirectionnel
        interfaceGraphique.setClient(joueur);
        joueur.setView(interfaceGraphique);
    }

    public static void main(String[] args) {
        launch(args);
    }
}