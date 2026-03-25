package org.example.leprojet.joueur;
import javafx.scene.Parent;
import org.example.leprojet.common.Message;
import javafx.application.Platform;

public class InterfaceGraphique extends Parent {

    private Joueur joueur;

    public InterfaceGraphique() {

    }

    public void setClient(Joueur joueur) {
        this.joueur = joueur;
    }

    // C'est la que lon receptionne les messages et que l'on va dire quoi faire avec.
    public void printNewMessage(Message mess) {
        Platform.runLater(() -> {

            String contenu = mess.getContent();

            if (contenu.startsWith("MOVE")) {
                // exemple : "MOVE 2 3 3 4"
                // → déplacer une pièce sur le damier
            } else if (contenu.equals("START")) {
                // lancer la partie
            } else if (contenu.equals("WIN")) {
                // afficher victoire
            }
        });
    }
}