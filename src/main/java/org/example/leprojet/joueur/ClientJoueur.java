package org.example.leprojet.joueur;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.leprojet.ui.MenuView;

import java.io.IOException;
import java.net.URL;

/**
 * Point d'entrée JavaFX d'un joueur de dames.
 * <p>
 * Affiche un menu d'accueil pour saisir le pseudo et les paramètres de connexion,
 * puis se connecte au serveur et affiche le damier.
 */
public class ClientJoueur extends Application {

    @Override
    public void start(Stage stage) {
        MenuView menu = new MenuView();

        menu.setOnNetworkStart((pseudo, host, port) -> {
            try {
                lancerModeReseau(stage, pseudo, host, port);
            } catch (IOException e) {
                System.err.println("[CLIENT] Impossible de se connecter : " + e.getMessage());
            }
        });

        Scene menuScene = new Scene(menu, 600, 450);
        applyStylesheet(menuScene);
        stage.setTitle("♟ Jeu de Dames — Connexion");
        stage.setScene(menuScene);
        stage.show();
    }

    private void lancerModeReseau(Stage stage, String pseudo, String host, int port) throws IOException {
        InterfaceGraphique gui = new InterfaceGraphique(pseudo);

        Joueur joueur = new Joueur(host, port, pseudo);
        gui.setClient(joueur);
        joueur.setView(gui);

        Scene scene = new Scene(gui, 620, 680);
        applyStylesheet(scene);
        stage.setTitle("♟ Jeu de Dames — " + pseudo);
        stage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        URL css = getClass().getResource("/org/example/leprojet/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}