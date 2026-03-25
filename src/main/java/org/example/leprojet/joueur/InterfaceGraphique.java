package org.example.leprojet.joueur;

import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.example.leprojet.common.Message;
import javafx.application.Platform;

public class InterfaceGraphique extends Parent {

    private Joueur joueur;

    public InterfaceGraphique() {
        int tailleCase = 50;
        GridPane grid = new GridPane();
        grid.setBorder(new Border(new BorderStroke(
                Color.GRAY,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(5)
        )));

        for(int row = 0; row<10; row++){
            for(int col = 0; col<10; col++){
                StackPane casePane = new StackPane();
                Rectangle rect = new Rectangle(tailleCase, tailleCase);
                rect.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);

                casePane.getChildren().add(rect);

                if (rect.getFill() == Color.BLACK) { // On ne joue que sur les cases noires
                    if (row < 4) {
                        casePane.getChildren().add(new Circle(20, Color.RED));
                    } else if (row > 5) {
                        casePane.getChildren().add(new Circle(20,Color.WHITE));
                    }
                }
                grid.add(casePane, col, row);
            }
        }
        this.getChildren().add(grid);
    }

    public void setClient(Joueur joueur) {
        this.joueur = joueur;
    }

    public void printNewMessage(Message mess) {
        Platform.runLater(() -> {
        });
    }
}