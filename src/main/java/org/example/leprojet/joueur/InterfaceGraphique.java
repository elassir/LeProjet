package org.example.leprojet.joueur;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.leprojet.Arbitre;
import org.example.leprojet.CoupCallback;
import org.example.leprojet.MoveCalculator;
import org.example.leprojet.common.Message;
import org.example.leprojet.core.Case;
import org.example.leprojet.core.Couleur;
import org.example.leprojet.core.Piece;
import org.example.leprojet.ui.DamierView;
import org.example.leprojet.ui.SoundManager;

/**
 * Interface graphique d'un joueur connecté au serveur.
 * <p>
 * Contient un {@link DamierView} interactif et des labels d'information.
 * Chaque coup local est envoyé au serveur via {@link CoupCallback}.
 * Les coups de l'adversaire arrivent via {@code onMessageRecu(COUP_VALIDE)}.
 */
public class InterfaceGraphique extends VBox {

    private Joueur joueur;
    private Arbitre arb;
    private DamierView damierView;
    private Couleur maCouleur;

    private final Label lblEtat;
    private final Label lblTour;
    private final Label lblInfo;

    public InterfaceGraphique() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(8));
        setSpacing(6);
        setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");

        lblEtat = creerLabel("Connexion au serveur…");
        lblTour = creerLabel("");
        lblInfo = creerLabel("");

        VBox infoBox = new VBox(3, lblEtat, lblTour, lblInfo);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(4));
        infoBox.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 8;");

        damierView = new DamierView();
        getChildren().addAll(infoBox, damierView);
    }

    public void setClient(Joueur joueur) {
        this.joueur = joueur;
    }

    // ── Dispatch des messages serveur ──────────────────────────────────

    public void onMessageRecu(Message mess) {
        Platform.runLater(() -> {
            switch (mess.getType()) {
                case ASSIGNATION_COULEUR -> onAssignation(mess.getCouleur());
                case DEBUT_PARTIE        -> onDebutPartie();
                case COUP_VALIDE         -> onCoupValide(mess);
                case COUP_INVALIDE       -> lblInfo.setText("⚠ " + mess.getContent());
                case FIN_PARTIE          -> onFinPartie(mess);
                default                  -> lblInfo.setText(mess.toString());
            }
        });
    }

    /** Rétro-compatibilité. */
    public void printNewMessage(Message mess) {
        onMessageRecu(mess);
    }

    // ── Handlers ───────────────────────────────────────────────────────

    private void onAssignation(String couleur) {
        joueur.setCouleurAssignee(couleur);
        maCouleur = "BLANC".equals(couleur) ? Couleur.BLANC : Couleur.NOIR;
        lblEtat.setText("Vous êtes : " + couleur);
        lblInfo.setText("En attente du 2e joueur…");
    }

    private void onDebutPartie() {
        arb = new Arbitre("BLANC", "NOIR");
        arb.initialiserPartie();

        getChildren().remove(damierView);
        damierView = new DamierView(arb.getPlateau(), arb, maCouleur);

        // Quand un coup est joué localement → l'envoyer au serveur
        damierView.setCoupCallback((lDep, cDep, lArr, cArr) ->
                joueur.envoyerCoup(lDep, cDep, lArr, cArr));

        damierView.setOnCoupJoue(this::mettreAJourLabels);
        getChildren().add(damierView);

        mettreAJourLabels();
    }

    private void onCoupValide(Message mess) {
        if (arb == null) return;

        int lDep = mess.getLigneDepart();
        int cDep = mess.getColonneDepart();
        int lArr = mess.getLigneArrivee();
        int cArr = mess.getColonneArrivee();

        Case[][] cases = arb.getPlateau().getCases();
        Piece piece = cases[lDep][cDep].getPiece();

        // Si la pièce est déjà partie → ce coup a été joué localement, on ignore
        if (piece == null) return;

        // Appliquer le coup de l'adversaire sur l'arbitre local
        Case dest = cases[lArr][cArr];
        boolean ok = false;

        // Utiliser MoveCalculator pour trouver la pièce prise (supporte pions ET dames)
        Piece piecePrise = MoveCalculator.trouverPiecePrise(piece, dest, arb.getPlateau());
        if (piecePrise != null) {
            ok = arb.jouerPrise(piece, piecePrise, dest);
            if (ok) SoundManager.playCapture();
        }
        if (!ok) {
            ok = arb.jouerDeplacement(piece, dest);
            if (ok) SoundManager.playMove();
        }

        damierView.rafraichir();
        mettreAJourLabels();

        // Vérifier fin de partie
        if (arb.getGagnant() != null) {
            SoundManager.playVictory();
        }
    }

    private void onFinPartie(Message mess) {
        lblEtat.setText("Partie terminée !");
        String gagnant = mess.getCouleur();
        if (gagnant != null && gagnant.equals(joueur.getCouleurAssignee())) {
            lblTour.setText("🏆 Vous avez gagné !");
            lblTour.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 16;");
        } else {
            lblTour.setText("Vous avez perdu.");
            lblTour.setStyle("-fx-text-fill: #f44336; -fx-font-size: 16;");
        }
        lblInfo.setText("");
    }

    // ── Labels ─────────────────────────────────────────────────────────

    private void mettreAJourLabels() {
        if (arb == null) return;
        lblEtat.setText("État : " + arb.getEtat());

        String tour = (arb.getJoueurCourant() != null) ? arb.getJoueurCourant().getNom() : "—";
        boolean monTour = (maCouleur != null && arb.getJoueurCourant() != null
                && arb.getJoueurCourant().getCouleur() == maCouleur);

        String indicateurTour = monTour ? "🟢 C'est votre tour !" : "🔴 Tour de l'adversaire (" + tour + ")";
        lblTour.setText(indicateurTour
                + (arb.isEnChaineDePrise() ? "  ⚡ chaîne de prises" : ""));
        lblTour.setStyle(monTour
                ? "-fx-text-fill: #2e7d32; -fx-font-size: 13; -fx-font-weight: bold;"
                : "-fx-text-fill: #c62828; -fx-font-size: 13; -fx-font-weight: bold;");

        lblInfo.setText("Pièces – B:" + arb.getPlateau().getBlanches().size()
                + "  N:" + arb.getPlateau().getNoires().size());
    }

    private Label creerLabel(String texte) {
        Label l = new Label(texte);
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        return l;
    }
}

