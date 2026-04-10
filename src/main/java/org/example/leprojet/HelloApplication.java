package org.example.leprojet;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application JavaFX.
 * Affiche un menu d'accueil puis lance le jeu en mode local.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        MenuView menu = new MenuView();

        menu.setOnLocalStart((pseudoBlanc, pseudoNoir) -> {
            // Lancer le mode local via TestPartieApp
            new TestPartieApp().start(stage);
        });

        Scene scene = new Scene(menu, 600, 450);
        stage.setTitle("♟ Jeu de Dames");
        stage.setScene(scene);
        stage.show();
    }
}
