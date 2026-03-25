package org.example.leprojet;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new DamierView(), 760, 820);
        stage.setTitle("Jeu de dames");
        stage.setScene(scene);
        stage.show();
    }
}
