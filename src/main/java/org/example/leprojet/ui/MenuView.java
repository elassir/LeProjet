package org.example.leprojet.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Menu d'accueil du jeu de dames.
 * <p>
 * Permet au joueur de saisir son pseudo et les paramètres de connexion
 * avant de rejoindre un serveur en ligne.
 */
public class MenuView extends VBox {

    private final TextField pseudoReseauField;
    private final TextField hostField;
    private final TextField portField;
    private final Label lblStatus;
    private final Label lblPretReseau;

    /** Callback pour le mode réseau (connexion au serveur). */
    private OnNetworkStart onNetworkStart;

    // ── Interface de callback ──────────────────────────────────────────

    @FunctionalInterface
    public interface OnNetworkStart {
        void start(String pseudo, String host, int port);
    }

    // ── Construction ───────────────────────────────────────────────────

    public MenuView() {
        getStyleClass().add("menu-root");
        setAlignment(Pos.CENTER);
        setSpacing(18);
        setPadding(new Insets(26));
        setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");
        setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.20)));

        // Titre
        Label titre = new Label("♟ Jeu de Dames");
        titre.setFont(Font.font("System", FontWeight.BOLD, 28));
        titre.setStyle("-fx-text-fill: #3f2a1d;");

        Label sousTitre = new Label("Lobby - Réseau");
        sousTitre.setFont(Font.font("System", FontWeight.NORMAL, 14));
        sousTitre.setStyle("-fx-text-fill: #7b4b2a;");

        // ── Section : Partie réseau ────────────────────────────────────

        Label lblReseau = creerSection("Rejoindre un serveur");

        pseudoReseauField = new TextField("Joueur");
        pseudoReseauField.setPromptText("Pseudo réseau");
        pseudoReseauField.setMaxWidth(250);

        hostField = new TextField("127.0.0.1");
        hostField.setPromptText("Adresse du serveur");
        hostField.setMaxWidth(250);

        portField = new TextField("6000");
        portField.setPromptText("Port");
        portField.setMaxWidth(250);

        lblPretReseau = new Label();

        Button btnReseau = creerBouton("🌐  Rejoindre", "#2196f3");
        btnReseau.setOnAction(e -> onClickReseau());

        GridPane reseauGrid = new GridPane();
        reseauGrid.setHgap(8);
        reseauGrid.setVgap(8);
        reseauGrid.add(new Label("Pseudo"), 0, 0);
        reseauGrid.add(pseudoReseauField, 1, 0);
        reseauGrid.add(new Label("Serveur"), 0, 1);
        reseauGrid.add(hostField, 1, 1);
        reseauGrid.add(new Label("Port"), 0, 2);
        reseauGrid.add(portField, 1, 2);
        reseauGrid.add(new Label("Statut"), 0, 3);
        reseauGrid.add(lblPretReseau, 1, 3);

        VBox reseauBox = new VBox(8, lblReseau, reseauGrid, btnReseau);
        reseauBox.getStyleClass().add("menu-card");
        reseauBox.setAlignment(Pos.CENTER);
        reseauBox.setPadding(new Insets(12));
        reseauBox.setStyle("-fx-background-color: rgba(255,255,255,0.55); -fx-background-radius: 12;");

        // ── Status ─────────────────────────────────────────────────────

        lblStatus = new Label("");
        lblStatus.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        lblStatus.setStyle("-fx-text-fill: #f44336;");

        // ── Assemblage ─────────────────────────────────────────────────

        getChildren().addAll(titre, sousTitre, reseauBox, lblStatus);

        pseudoReseauField.textProperty().addListener((obs, o, n) -> mettreAJourStatut());
        hostField.textProperty().addListener((obs, o, n) -> mettreAJourStatut());
        portField.textProperty().addListener((obs, o, n) -> mettreAJourStatut());
        mettreAJourStatut();
    }

    // ── Callback ───────────────────────────────────────────────────────

    public void setOnNetworkStart(OnNetworkStart cb) { this.onNetworkStart = cb; }

    // ── Action ─────────────────────────────────────────────────────────

    private void onClickReseau() {
        String host = hostField.getText().isBlank() ? "127.0.0.1" : hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("⚠ Port invalide !");
            return;
        }
        String pseudo = pseudoReseauField.getText() == null ? "" : pseudoReseauField.getText().trim();
        if (pseudo.isBlank()) {
            lblStatus.setText("⚠ Pseudo requis pour le mode réseau.");
            return;
        }
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

    private void mettreAJourStatut() {
        boolean pseudoReseauOk = pseudoReseauField.getText() != null && !pseudoReseauField.getText().isBlank();
        boolean hostOk = !hostField.getText().isBlank();
        boolean portOk;
        try {
            int port = Integer.parseInt(portField.getText().trim());
            portOk = port > 0 && port <= 65535;
        } catch (Exception e) {
            portOk = false;
        }

        setPretLabel(lblPretReseau, pseudoReseauOk && hostOk && portOk);
    }

    private void setPretLabel(Label lbl, boolean pret) {
        if (pret) {
            lbl.setText("Pret");
            lbl.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        } else {
            lbl.setText("En attente");
            lbl.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        }
    }
}

