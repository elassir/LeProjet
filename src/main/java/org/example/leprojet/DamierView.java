package org.example.leprojet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DamierView extends BorderPane {

    private static final double TAILLE_CASE = 70;
    private final Plateau plateau;

    public DamierView() {
        this.plateau = new Plateau();
        this.plateau.initPions();

        GridPane grille = new GridPane();
        grille.setAlignment(Pos.CENTER);
        grille.setHgap(0);
        grille.setVgap(0);
        grille.setStyle("-fx-background-color: #3f2a1d; -fx-padding: 10;");

        Case[][] cases = plateau.getCases();
        for (int ligne = 0; ligne < Plateau.NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < Plateau.NB_COLONNES; colonne++) {
                grille.add(creerCase(cases[ligne][colonne]), colonne, ligne);
            }
        }

        Label titre = new Label("Jeu de dames");
        titre.setFont(Font.font("System", FontWeight.BOLD, 26));
        titre.setPadding(new Insets(0, 0, 12, 0));

        setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");
        setTop(titre);
        BorderPane.setAlignment(titre, Pos.CENTER);
        setCenter(grille);
        setPadding(new Insets(20));
    }

    private StackPane creerCase(Case casePlateau) {
        Rectangle fond = new Rectangle(TAILLE_CASE, TAILLE_CASE);
        fond.setFill(couleurCase(casePlateau));

        StackPane cellule = new StackPane(fond);
        Piece piece = casePlateau.getPiece();
        if (piece != null) {
            cellule.getChildren().add(creerNoeudPiece(piece));
        }
        return cellule;
    }

    private Color couleurCase(Case casePlateau) {
        return casePlateau.getCouleur() == Couleur.NOIR
                ? Color.web("#7b4b2a")
                : Color.web("#f4d7a1");
    }

    private Node creerNoeudPiece(Piece piece) {
        Circle pion = new Circle(TAILLE_CASE * 0.33);
        pion.setFill(piece.getCouleur() == Couleur.NOIR ? Color.web("#1e1e1e") : Color.web("#f7f7f7"));
        pion.setStroke(piece.getCouleur() == Couleur.NOIR ? Color.web("#d0a85c") : Color.web("#7b4b2a"));
        pion.setStrokeWidth(3);

        StackPane pieceView = new StackPane(pion);
        if (piece instanceof Dame) {
            Label dame = new Label("D");
            dame.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
            dame.setTextFill(piece.getCouleur() == Couleur.NOIR ? Color.web("#f5d48b") : Color.web("#5a3418"));
            pieceView.getChildren().add(dame);
        }
        return pieceView;
    }
}
