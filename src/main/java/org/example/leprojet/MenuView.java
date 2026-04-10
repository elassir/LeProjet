package org.example.leprojet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Menu d'accueil du jeu de dames.
 * <p>
 * Permet aux joueurs de saisir leur pseudo avant de commencer.
 * Fournit des boutons pour lancer la partie en mode local ou réseau.
 */
public class MenuView extends VBox {

    private final TextField pseudoField1;
    private final TextField pseudoField2;
    private final TextField hostField;
    private final TextField portField;
    private final Label lblStatus;

    /** Callback pour le mode local (2 joueurs sur la même machine). */
    private OnLocalStart onLocalStart;

    /** Callback pour le mode réseau (connexion au serveur). */
    private OnNetworkStart onNetworkStart;

    // ── Interfaces de callback ─────────────────────────────────────────

    @FunctionalInterface
    public interface OnLocalStart {
        void start(String pseudoBlanc, String pseudoNoir);
    }

    @FunctionalInterface
    public interface OnNetworkStart {
        void start(String pseudo, String host, int port);
    }

    // ── Construction ───────────────────────────────────────────────────

    public MenuView() {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");

        // Titre
        Label titre = new Label("♟ Jeu de Dames");
        titre.setFont(Font.font("System", FontWeight.BOLD, 28));
        titre.setStyle("-fx-text-fill: #3f2a1d;");

        Label sousTitre = new Label("Règles françaises — Damier 10×10");
        sousTitre.setFont(Font.font("System", FontWeight.NORMAL, 14));
        sousTitre.setStyle("-fx-text-fill: #7b4b2a;");

        // ── Section : Partie locale ────────────────────────────────────

        Label lblLocal = creerSection("Partie locale (2 joueurs)");

        pseudoField1 = new TextField();
        pseudoField1.setPromptText("Pseudo Joueur BLANC");
        pseudoField1.setMaxWidth(250);

        pseudoField2 = new TextField();
        pseudoField2.setPromptText("Pseudo Joueur NOIR");
        pseudoField2.setMaxWidth(250);

        Button btnLocal = creerBouton("▶  Jouer en local", "#4caf50");
        btnLocal.setOnAction(e -> onClickLocal());

        VBox localBox = new VBox(8, lblLocal, pseudoField1, pseudoField2, btnLocal);
        localBox.setAlignment(Pos.CENTER);
        localBox.setPadding(new Insets(12));
        localBox.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 10;");

        // ── Section : Partie réseau ────────────────────────────────────

        Label lblReseau = creerSection("Rejoindre un serveur");

        hostField = new TextField("127.0.0.1");
        hostField.setPromptText("Adresse du serveur");
        hostField.setMaxWidth(250);

        portField = new TextField("6000");
        portField.setPromptText("Port");
        portField.setMaxWidth(250);

        Button btnReseau = creerBouton("🌐  Rejoindre", "#2196f3");
        btnReseau.setOnAction(e -> onClickReseau());

        VBox reseauBox = new VBox(8, lblReseau, hostField, portField, btnReseau);
        reseauBox.setAlignment(Pos.CENTER);
        reseauBox.setPadding(new Insets(12));
        reseauBox.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 10;");

        // ── Status ─────────────────────────────────────────────────────

        lblStatus = new Label("");
        lblStatus.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        lblStatus.setStyle("-fx-text-fill: #f44336;");

        // ── Assemblage ─────────────────────────────────────────────────

        HBox sections = new HBox(20, localBox, reseauBox);
        sections.setAlignment(Pos.CENTER);

        getChildren().addAll(titre, sousTitre, sections, lblStatus);
    }

    // ── Callbacks ──────────────────────────────────────────────────────

    public void setOnLocalStart(OnLocalStart cb)     { this.onLocalStart = cb; }
    public void setOnNetworkStart(OnNetworkStart cb) { this.onNetworkStart = cb; }

    // ── Actions ────────────────────────────────────────────────────────

    private void onClickLocal() {
        String p1 = pseudoField1.getText().isBlank() ? "Joueur Blanc" : pseudoField1.getText().trim();
        String p2 = pseudoField2.getText().isBlank() ? "Joueur Noir" : pseudoField2.getText().trim();
        lblStatus.setText("");
        if (onLocalStart != null) {
            onLocalStart.start(p1, p2);
        }
    }

    private void onClickReseau() {
        String host = hostField.getText().isBlank() ? "127.0.0.1" : hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("⚠ Port invalide !");
            return;
        }
        String pseudo = pseudoField1.getText().isBlank() ? "Joueur" : pseudoField1.getText().trim();
        lblStatus.setText("");
        if (onNetworkStart != null) {
            onNetworkStart.start(pseudo, host, port);
        }
    }

    // ── Utilitaires visuels ────────────────────────────────────────────

    private Label creerSection(String texte) {
        Label l = new Label(texte);
        l.setFont(Font.font("System", FontWeight.BOLD, 15));
        l.setStyle("-fx-text-fill: #3f2a1d;");
        return l;
    }

    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-font-size: 13; -fx-padding: 8 20; "
                + "-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }
}
