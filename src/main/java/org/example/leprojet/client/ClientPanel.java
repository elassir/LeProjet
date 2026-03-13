package org.example.leprojet.client;

import org.example.leprojet.common.Message;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;

public class ClientPanel extends Parent {

    private TextArea textToSend;
    private ScrollPane scrollReceivedText;
    private TextFlow receivedText;
    private Button sendBtn;
    private Button clearBtn;

    private Client client;

    public ClientPanel() {
        textToSend = new TextArea();
        scrollReceivedText = new ScrollPane();
        receivedText = new TextFlow();
        sendBtn = new Button("Send");
        clearBtn = new Button("Clear");

        scrollReceivedText.setLayoutX(50);
        scrollReceivedText.setLayoutY(50);
        scrollReceivedText.setPrefWidth(400);
        scrollReceivedText.setPrefHeight(350);

        receivedText.setPrefWidth(400);
        receivedText.setVisible(true);

        scrollReceivedText.setContent(receivedText);
        scrollReceivedText.vvalueProperty().bind(receivedText.heightProperty());

        textToSend.setLayoutX(50);
        textToSend.setLayoutY(420);
        textToSend.setPrefWidth(400);
        textToSend.setPrefHeight(60);

        sendBtn.setLayoutX(470);
        sendBtn.setLayoutY(420);
        sendBtn.setPrefWidth(80);
        sendBtn.setVisible(true);

        clearBtn.setLayoutX(470);
        clearBtn.setLayoutY(460);
        clearBtn.setPrefWidth(80);
        clearBtn.setVisible(true);

        this.getChildren().add(scrollReceivedText);
        this.getChildren().add(textToSend);
        this.getChildren().add(clearBtn);
        this.getChildren().add(sendBtn);

        clearBtn.setOnAction(e -> textToSend.clear());

        sendBtn.setOnAction(e -> {
            String txt = textToSend.getText().trim();
            if (txt.isEmpty()) return;

            Message mess = new Message("Moi", txt);

            printNewMessage(mess);
            textToSend.clear();

            if (client != null) client.sendMessage(mess);
        });
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void printNewMessage(Message mess) {
        Platform.runLater(() -> {
            Label text = new Label(mess.toString());
            text.setWrapText(true);
            text.setPrefWidth(scrollReceivedText.getPrefWidth() - 20);
            text.setAlignment(Pos.CENTER_LEFT);
            receivedText.getChildren().add(text);
        });
    }
}