package org.example.leprojet.client;

import org.example.leprojet.common.Message;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainGui extends Application {
    public MainGui() {}

    @Override
    public void start(Stage stage) throws IOException {
        ClientPanel clientPanel = new ClientPanel();
        clientPanel.printNewMessage(new Message("System", "Hello world!!!!!"));
        Group root = new Group();
        root.getChildren().add(clientPanel);

        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("TP4 - Client");
        stage.setScene(scene);
        stage.show();

        // Arguments: host port
        String host = "127.0.0.1";
        int port = 6000;

        if (getParameters().getRaw().size() >= 2) {
            host = getParameters().getRaw().get(0);
            port = Integer.parseInt(getParameters().getRaw().get(1));
        }

        Client client = new Client(host, port);

        // lien bidirectionnel
        clientPanel.setClient(client);
        client.setView(clientPanel);
    }

    public static void main(String[] args) {
        launch(args);
    }
}