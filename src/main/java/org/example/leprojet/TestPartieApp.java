package org.example.leprojet;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.leprojet.core.Couleur;
import org.example.leprojet.core.EtatPartie;
import org.example.leprojet.core.JoueurPartie;
import org.example.leprojet.core.Plateau;
import org.example.leprojet.ui.DamierView;
import org.example.leprojet.ui.MenuView;
import org.example.leprojet.ui.SoundManager;

import java.net.URL;

/**
 * Application de test locale (sans serveur).
 * <p>
 * Affiche un menu d'accueil avec saisie de pseudos, puis ouvre 2 fenêtres :
 * une pour le joueur BLANC et une pour le joueur NOIR.
 * Les deux partagent le même arbitre et le même plateau.
 */
public class TestPartieApp extends Application {

    private static final int PIONS_DEPART = 20;

    private Arbitre arb;

    // ── Fenêtre BLANC ──────────────────────────────────────────────────
    private DamierView damierBlanc;
    private Label lblTourBlanc;
    private Label lblScoreBlanc;
    private Label lblChaineBlanc;

    // ── Fenêtre NOIR ───────────────────────────────────────────────────
    private DamierView damierNoir;
    private Label lblTourNoir;
    private Label lblScoreNoir;
    private Label lblChaineNoir;

    private String nomBlanc;
    private String nomNoir;

    @Override
    public void start(Stage primaryStage) {
        MenuView menu = new MenuView();
        menu.setOnLocalStart((pseudoBlanc, pseudoNoir) -> {
            nomBlanc = pseudoBlanc;
            nomNoir = pseudoNoir;
            primaryStage.close();
            lancerPartie();
        });

        Scene menuScene = new Scene(menu, 600, 450);
        URL css = getClass().getResource("/org/example/leprojet/styles.css");
        if (css != null) {
            menuScene.getStylesheets().add(css.toExternalForm());
        }
        primaryStage.setTitle("♟ Jeu de Dames — Menu");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Lancement de la partie
    // ─────────────────────────────────────────────────────────────────────

    private void lancerPartie() {
        arb = new Arbitre(nomBlanc, nomNoir);

        // ── Fenêtre BLANC ──────────────────────────────────────────────
        Stage stageBlanc = new Stage();
        damierBlanc = new DamierView(arb.getPlateau(), arb, Couleur.BLANC);
        lblTourBlanc = creerLabelTour();
        lblScoreBlanc = creerLabelScore();
        lblChaineBlanc = creerLabelChaine();

        Scene sceneBlanc = creerScene(damierBlanc, lblTourBlanc, lblScoreBlanc, lblChaineBlanc, nomBlanc, Couleur.BLANC);
        stageBlanc.setTitle("♟ " + nomBlanc + " (BLANC)");
        stageBlanc.setScene(sceneBlanc);
        stageBlanc.setX(50);
        stageBlanc.setY(50);

        // ── Fenêtre NOIR ───────────────────────────────────────────────
        Stage stageNoir = new Stage();
        damierNoir = new DamierView(arb.getPlateau(), arb, Couleur.NOIR);
        lblTourNoir = creerLabelTour();
        lblScoreNoir = creerLabelScore();
        lblChaineNoir = creerLabelChaine();

        Scene sceneNoir = creerScene(damierNoir, lblTourNoir, lblScoreNoir, lblChaineNoir, nomNoir, Couleur.NOIR);
        stageNoir.setTitle("♟ " + nomNoir + " (NOIR)");
        stageNoir.setScene(sceneNoir);
        stageNoir.setX(700);
        stageNoir.setY(50);

        // ── Callback ───────────────────────────────────────────────────
        Runnable rafraichirTout = () -> {
            damierBlanc.rafraichir();
            damierNoir.rafraichir();
            mettreAJourLabels();
        };
        damierBlanc.setOnCoupJoue(rafraichirTout);
        damierNoir.setOnCoupJoue(rafraichirTout);

        stageBlanc.show();
        stageNoir.show();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Construction de la scène
    // ─────────────────────────────────────────────────────────────────────

    private Scene creerScene(DamierView damier, Label lblTour, Label lblScore,
                             Label lblChaine, String pseudo, Couleur couleur) {

        // ── Header : pseudo + couleur ──────────────────────────────────
        String emoji = (couleur == Couleur.BLANC) ? "⚪" : "⚫";
        Label lblPseudo = new Label(emoji + "  " + pseudo);
        lblPseudo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblPseudo.setStyle("-fx-text-fill: #3f2a1d;");

        // ── Boutons ────────────────────────────────────────────────────
        Button btnDebut = creerBouton("▶ Jouer", "#4caf50");
        Button btnFin = creerBouton("⏹ Terminer", "#e53935");
        btnDebut.setOnAction(e -> onDebutPartie());
        btnFin.setOnAction(e -> onFinPartie());

        HBox btnBox = new HBox(10, btnDebut, btnFin);
        btnBox.setAlignment(Pos.CENTER);

        // ── Header bar ─────────────────────────────────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, lblPseudo, spacer, btnBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(6, 12, 6, 12));
        header.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 8;");

        // ── Info bar : tour + score + chaîne ───────────────────────────
        HBox infoBar = new HBox(16, lblTour, lblScore, lblChaine);
        infoBar.setAlignment(Pos.CENTER);
        infoBar.setPadding(new Insets(6));

        // ── Assemblage ─────────────────────────────────────────────────
        VBox root = new VBox(6, header, new Separator(), infoBar, damier);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #ede0d0, #c9b99a);");

        return new Scene(root);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────────────────────────────

    private void onDebutPartie() {
        if (arb.getEtat() == EtatPartie.EN_COURS) return;
        arb.initialiserPartie();

        damierBlanc.setPlateau(arb.getPlateau());
        damierNoir.setPlateau(arb.getPlateau());
        mettreAJourLabels();
    }

    private void onFinPartie() {
        if (arb.getEtat() != EtatPartie.EN_COURS) return;

        Plateau p = arb.getPlateau();
        while (!p.getNoires().isEmpty()) {
            p.supprimerPiece(p.getNoires().getFirst());
        }
        arb.verifierFinDePartie();
        SoundManager.playVictory();

        damierBlanc.rafraichir();
        damierNoir.rafraichir();
        mettreAJourLabels();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Mise à jour des labels
    // ─────────────────────────────────────────────────────────────────────

    private void mettreAJourLabels() {
        JoueurPartie courant = arb.getJoueurCourant();
        JoueurPartie gagnant = arb.getGagnant();

        int nbBlanches = arb.getPlateau().getBlanches().size();
        int nbNoires = arb.getPlateau().getNoires().size();
        int prisesBlanc = PIONS_DEPART - nbNoires;  // pièces noires mangées par blanc
        int prisesNoir = PIONS_DEPART - nbBlanches;  // pièces blanches mangées par noir

        String chaine = arb.isEnChaineDePrise() ? "⚡ Chaîne de prises !" : "";

        // ── BLANC ──────────────────────────────────────────────────────
        if (gagnant != null) {
            lblTourBlanc.setText(gagnant.getCouleur() == Couleur.BLANC
                    ? "🏆 Victoire !"
                    : "💀 Défaite");
            lblTourBlanc.setStyle(gagnant.getCouleur() == Couleur.BLANC
                    ? styleTourActif() : styleTourInactif());
        } else if (courant != null && courant.getCouleur() == Couleur.BLANC) {
            lblTourBlanc.setText("🟢 C'est votre tour !");
            lblTourBlanc.setStyle(styleTourActif());
        } else {
            lblTourBlanc.setText("🔴 Tour adverse");
            lblTourBlanc.setStyle(styleTourInactif());
        }
        lblScoreBlanc.setText("⚔ Prises : " + prisesBlanc + "   |   Restantes : " + nbBlanches);
        lblChaineBlanc.setText(chaine);

        // ── NOIR ───────────────────────────────────────────────────────
        if (gagnant != null) {
            lblTourNoir.setText(gagnant.getCouleur() == Couleur.NOIR
                    ? "🏆 Victoire !"
                    : "💀 Défaite");
            lblTourNoir.setStyle(gagnant.getCouleur() == Couleur.NOIR
                    ? styleTourActif() : styleTourInactif());
        } else if (courant != null && courant.getCouleur() == Couleur.NOIR) {
            lblTourNoir.setText("🟢 C'est votre tour !");
            lblTourNoir.setStyle(styleTourActif());
        } else {
            lblTourNoir.setText("🔴 Tour adverse");
            lblTourNoir.setStyle(styleTourInactif());
        }
        lblScoreNoir.setText("⚔ Prises : " + prisesNoir + "   |   Restantes : " + nbNoires);
        lblChaineNoir.setText(chaine);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Factory labels / boutons
    // ─────────────────────────────────────────────────────────────────────

    private Label creerLabelTour() {
        Label l = new Label("En attente…");
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        return l;
    }

    private Label creerLabelScore() {
        Label l = new Label("");
        l.setFont(Font.font("System", FontWeight.NORMAL, 12));
        l.setStyle("-fx-text-fill: #5d4037;");
        return l;
    }

    private Label creerLabelChaine() {
        Label l = new Label("");
        l.setFont(Font.font("System", FontWeight.BOLD, 12));
        l.setStyle("-fx-text-fill: #e65100;");
        return l;
    }

    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-font-size: 11; -fx-padding: 5 14; "
                + "-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    private String styleTourActif() {
        return "-fx-text-fill: #2e7d32; -fx-font-weight: bold;";
    }

    private String styleTourInactif() {
        return "-fx-text-fill: #b71c1c; -fx-font-weight: bold;";
    }

    // ─────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}
