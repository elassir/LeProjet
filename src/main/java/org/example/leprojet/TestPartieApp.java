package org.example.leprojet;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Application de test locale (sans serveur) pour vérifier les états de la partie.
 * <p>
 * Ouvre <b>2 fenêtres</b> : une pour le joueur BLANC, une pour le joueur NOIR.
 * Les deux partagent le même arbitre et le même plateau.
 * Chaque mouvement effectué dans une fenêtre est immédiatement visible dans l'autre.
 * <p>
 * Chaque joueur ne peut cliquer que quand c'est son tour.
 */
public class TestPartieApp extends Application {

    private arbitre arb;

    // ── Fenêtre Joueur BLANC ───────────────────────────────────────────
    private DamierView damierBlanc;
    private Label lblEtatBlanc;
    private Label lblTourBlanc;
    private Label lblInfoBlanc;

    // ── Fenêtre Joueur NOIR ────────────────────────────────────────────
    private DamierView damierNoir;
    private Label lblEtatNoir;
    private Label lblTourNoir;
    private Label lblInfoNoir;

    @Override
    public void start(Stage stageBlanc) {

        // ── Arbitre partagé ────────────────────────────────────────────
        arb = new arbitre("Alice", "Bob");

        // ── Fenêtre BLANC ──────────────────────────────────────────────
        damierBlanc = new DamierView(arb.getPlateau(), arb, Couleur.BLANC);
        lblEtatBlanc = creerLabel("État : " + arb.getEtat());
        lblTourBlanc = creerLabel("Tour : —");
        lblInfoBlanc = creerLabel("En attente du début de la partie…");

        Scene sceneBlanc = creerScene(damierBlanc, lblEtatBlanc, lblTourBlanc, lblInfoBlanc);
        stageBlanc.setTitle("♟ Joueur BLANC – Alice");
        stageBlanc.setScene(sceneBlanc);
        stageBlanc.setX(50);
        stageBlanc.setY(50);

        // ── Fenêtre NOIR ───────────────────────────────────────────────
        Stage stageNoir = new Stage();
        damierNoir = new DamierView(arb.getPlateau(), arb, Couleur.NOIR);
        lblEtatNoir = creerLabel("État : " + arb.getEtat());
        lblTourNoir = creerLabel("Tour : —");
        lblInfoNoir = creerLabel("En attente du début de la partie…");

        Scene sceneNoir = creerScene(damierNoir, lblEtatNoir, lblTourNoir, lblInfoNoir);
        stageNoir.setTitle("♟ Joueur NOIR – Bob");
        stageNoir.setScene(sceneNoir);
        stageNoir.setX(650);
        stageNoir.setY(50);

        // ── Callback : quand un coup est joué, rafraîchir les 2 vues ──
        Runnable rafraichirTout = () -> {
            damierBlanc.rafraichir();
            damierNoir.rafraichir();
            mettreAJourLabels();
        };
        damierBlanc.setOnCoupJoue(rafraichirTout);
        damierNoir.setOnCoupJoue(rafraichirTout);

        // ── Affichage ──────────────────────────────────────────────────
        stageBlanc.show();
        stageNoir.show();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Construction d'une scène avec boutons + infos + damier
    // ─────────────────────────────────────────────────────────────────────

    private Scene creerScene(DamierView damier, Label lblEtat, Label lblTour, Label lblInfo) {

        Button btnDebut = new Button("▶  Début Partie");
        btnDebut.setStyle("-fx-font-size: 13; -fx-padding: 6 16; "
                + "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 8;");

        Button btnFin = new Button("⏹  Fin Partie");
        btnFin.setStyle("-fx-font-size: 13; -fx-padding: 6 16; "
                + "-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 8;");

        btnDebut.setOnAction(e -> onDebutPartie());
        btnFin.setOnAction(e -> onFinPartie());

        HBox boutonsBox = new HBox(16, btnDebut, btnFin);
        boutonsBox.setAlignment(Pos.CENTER);
        boutonsBox.setPadding(new Insets(4));

        VBox infoBox = new VBox(3, lblEtat, lblTour, lblInfo);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(4));
        infoBox.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 8;");

        VBox root = new VBox(6, infoBox, damier, boutonsBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(8));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");

        return new Scene(root);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Actions des boutons
    // ─────────────────────────────────────────────────────────────────────

    private void onDebutPartie() {
        if (arb.getEtat() == EtatPartie.EN_COURS) return; // déjà démarrée

        arb.initialiserPartie();

        damierBlanc.setPlateau(arb.getPlateau());
        damierNoir.setPlateau(arb.getPlateau());
        mettreAJourLabels();

        System.out.println("[TEST] Partie démarrée !");
        System.out.println("[TEST] " + arb);
    }

    private void onFinPartie() {
        if (arb.getEtat() != EtatPartie.EN_COURS) {
            System.out.println("[TEST] La partie n'est pas en cours.");
            return;
        }

        // Simuler la victoire des blancs
        Plateau p = arb.getPlateau();
        while (!p.getNoires().isEmpty()) {
            p.supprimerPiece(p.getNoires().getFirst());
        }
        arb.verifierFinDePartie();

        damierBlanc.rafraichir();
        damierNoir.rafraichir();
        mettreAJourLabels();

        System.out.println("[TEST] Fin de partie simulée !");
        Joueur gagnant = arb.getGagnant();
        if (gagnant != null) {
            System.out.println("[TEST] Gagnant : " + gagnant);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Mise à jour des labels des 2 fenêtres
    // ─────────────────────────────────────────────────────────────────────

    private void mettreAJourLabels() {
        String etatTxt = "État : " + arb.getEtat();
        String tourTxt = "Tour : "
                + (arb.getJoueurCourant() != null ? arb.getJoueurCourant().getNom() : "—");

        Joueur gagnant = arb.getGagnant();
        String infoTxt;
        if (gagnant != null) {
            infoTxt = "🏆 Gagnant : " + gagnant.getNom()
                    + "  (B:" + arb.getPlateau().getBlanches().size()
                    + " / N:" + arb.getPlateau().getNoires().size() + ")";
        } else {
            infoTxt = "Pièces – B:" + arb.getPlateau().getBlanches().size()
                    + "  N:" + arb.getPlateau().getNoires().size();
        }

        // Fenêtre BLANC
        lblEtatBlanc.setText(etatTxt);
        lblTourBlanc.setText(tourTxt);
        lblInfoBlanc.setText(infoTxt);

        // Fenêtre NOIR
        lblEtatNoir.setText(etatTxt);
        lblTourNoir.setText(tourTxt);
        lblInfoNoir.setText(infoTxt);
    }

    private Label creerLabel(String texte) {
        Label l = new Label(texte);
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        return l;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Main
    // ─────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}
