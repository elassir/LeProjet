package org.example.leprojet.joueur;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.leprojet.Arbitre;
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
 * Chaque coup local est envoyé au serveur via un callback de coup.
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
    private final Label lblJoueurs;
    private final VBox chatMessagesBox;
    private final TextField chatInput;
    private final Label lblChatStatus;
    private final Button btnAbandon;
    private final VBox chatPanel;
    private final StackPane damierContainer;
    private StackPane rootStack;
    private StackPane overlayFinPartie;
    private Label lblFinTitre;
    private Label lblFinDetail;
    private Button btnRevanche;
    private Button btnDeconnecter;
    private boolean revancheDemandee;

    private final String pseudoLocal;
    private String pseudoBlanc = "Joueur BLANC";
    private String pseudoNoir = "Joueur NOIR";

    public InterfaceGraphique(String pseudoLocal) {
        this.pseudoLocal = (pseudoLocal == null || pseudoLocal.isBlank()) ? "Joueur" : pseudoLocal.trim();

        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(8));
        setSpacing(6);
        setStyle("-fx-background-color: linear-gradient(to bottom, #f6eee3, #dfd1bc);");

        lblEtat = creerLabel("Connexion au serveur…");
        lblTour = creerLabel("");
        lblInfo = creerLabel("");
        lblJoueurs = creerLabel("Joueurs : en attente de synchronisation...");

        VBox infoBox = new VBox(3, lblEtat, lblTour, lblInfo, lblJoueurs);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(4));
        infoBox.getStyleClass().add("game-info-box");

        btnAbandon = new Button("Abandonner");
        btnAbandon.getStyleClass().add("danger-btn");
        btnAbandon.setDisable(true);
        btnAbandon.setOnAction(e -> onAbandon());

        HBox topBar = new HBox(10, infoBox, btnAbandon);
        topBar.setAlignment(Pos.CENTER);

        damierView = new DamierView();
        damierContainer = new StackPane(damierView);
        appliquerResponsiveDamier(damierView);
        damierContainer.getStyleClass().add("board-container");
        damierContainer.setMinSize(260, 260);
        HBox.setHgrow(damierContainer, Priority.ALWAYS);

        chatMessagesBox = new VBox(4);
        chatMessagesBox.setPadding(new Insets(6));
        chatMessagesBox.getStyleClass().add("chat-messages-box");

        ScrollPane chatScroll = new ScrollPane(chatMessagesBox);
        chatScroll.setFitToWidth(true);
        chatScroll.setFitToHeight(true);
        chatScroll.setPrefViewportHeight(220);
        chatScroll.getStyleClass().add("chat-scroll");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        chatInput = new TextField();
        chatInput.setPromptText("Ecrire un message (max 300 caracteres)");
        chatInput.setOnAction(e -> envoyerChat());

        Button btnEnvoyer = new Button("Envoyer");
        btnEnvoyer.setOnAction(e -> envoyerChat());

        HBox chatActions = new HBox(8, chatInput, btnEnvoyer);
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        chatActions.setAlignment(Pos.CENTER);

        lblChatStatus = creerLabel("");
        lblChatStatus.setStyle("-fx-text-fill: #b71c1c;");

        chatPanel = new VBox(6, chatScroll, chatActions, lblChatStatus);
        chatPanel.setPadding(new Insets(6));
        chatPanel.getStyleClass().add("chat-panel");
        chatPanel.setPrefWidth(280);
        chatPanel.setMinWidth(220);
        chatPanel.setMaxWidth(380);

        HBox centerRow = new HBox(10, damierContainer, chatPanel);
        centerRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(damierContainer, Priority.ALWAYS);
        HBox.setHgrow(chatPanel, Priority.SOMETIMES);

        // Ratio responsive : le chat garde une part fixe, sans faire disparaître le plateau.
        chatPanel.prefWidthProperty().bind(widthProperty().multiply(0.32));
        BorderPane gameLayout = new BorderPane();
        gameLayout.setTop(topBar);
        gameLayout.setCenter(centerRow);
        BorderPane.setMargin(topBar, new Insets(0, 0, 6, 0));
        rootStack = new StackPane(gameLayout);
        overlayFinPartie = creerOverlayFinPartie();
        rootStack.getChildren().add(overlayFinPartie);
        VBox.setVgrow(rootStack, Priority.ALWAYS);

        getChildren().add(rootStack);
    }

    public void setClient(Joueur joueur) {
        this.joueur = joueur;
    }

    // ── Dispatch des messages serveur ──────────────────────────────────

    public void onMessageRecu(Message mess) {
        Platform.runLater(() -> {
            switch (mess.getType()) {
                case ASSIGNATION_COULEUR -> onAssignation(mess.getCouleur());
                case INFOS_JOUEURS       -> onInfosJoueurs(mess);
                case DEBUT_PARTIE        -> onDebutPartie();
                case COUP_VALIDE         -> onCoupValide(mess);
                case COUP_INVALIDE       -> lblInfo.setText("⚠ " + mess.getContent());
                case FIN_PARTIE          -> onFinPartie(mess);
                case REVANCHE_DEMANDE    -> onDemandeRevanche(mess);
                case REVANCHE_REPONSE    -> onReponseRevanche(mess);
                case TEXTE               -> ajouterMessageChat(mess.getSender(), mess.getContent());
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
        majLabelJoueurs();
    }

    private void onInfosJoueurs(Message mess) {
        if (mess.getPseudoBlanc() != null && !mess.getPseudoBlanc().isBlank()) {
            pseudoBlanc = mess.getPseudoBlanc();
        }
        if (mess.getPseudoNoir() != null && !mess.getPseudoNoir().isBlank()) {
            pseudoNoir = mess.getPseudoNoir();
        }
        majLabelJoueurs();
    }

    private void onDebutPartie() {
        arb = new Arbitre(pseudoBlanc, pseudoNoir);
        arb.initialiserPartie();
        revancheDemandee = false;

        damierContainer.getChildren().clear();
        damierView = new DamierView(arb.getPlateau(), arb, maCouleur);
        appliquerResponsiveDamier(damierView);

        // Quand un coup est joué localement → l'envoyer au serveur
        damierView.setCoupCallback((lDep, cDep, lArr, cArr) ->
                joueur.envoyerCoup(lDep, cDep, lArr, cArr));

        damierView.setOnCoupJoue(this::mettreAJourLabels);
        damierContainer.getChildren().add(damierView);
        damierView.setDisable(false);
        btnAbandon.setDisable(false);
        masquerOverlayFinPartie();

        mettreAJourLabels();
        ajouterMessageChat("Systeme", "La partie commence. Les blancs jouent en premier.");
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
            SoundManager.playEndgame();
        }
    }

    private void onFinPartie(Message mess) {
        lblEtat.setText("Partie terminée !");
        if (damierView != null) damierView.setDisable(true);
        btnAbandon.setDisable(true);
        String gagnant = mess.getCouleur();
        String titreOverlay;
        String detailOverlay;
        if (gagnant != null && gagnant.equals(joueur.getCouleurAssignee())) {
            lblTour.setText("🏆 Vous avez gagné !");
            lblTour.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 16;");
            titreOverlay = "Victoire !";
            detailOverlay = "Vous pouvez proposer une revanche à votre adversaire.";
        } else if (gagnant == null || gagnant.isBlank()) {
            lblTour.setText("Match nul.");
            lblTour.setStyle("-fx-text-fill: #6d4c41; -fx-font-size: 16;");
            titreOverlay = "Match nul";
            detailOverlay = "Choisissez la suite de la partie.";
        } else {
            lblTour.setText("Vous avez perdu.");
            lblTour.setStyle("-fx-text-fill: #f44336; -fx-font-size: 16;");
            titreOverlay = "Défaite";
            detailOverlay = "Vous pouvez demander une revanche à votre adversaire.";
        }
        lblInfo.setText("");
        ajouterMessageChat("Systeme", "Partie terminee.");
        afficherOverlayFinPartie(titreOverlay, detailOverlay);
        SoundManager.playEndgame();
    }

    private void onDemandeRevanche(Message mess) {
        if (joueur == null) return;

        String demandeur = (mess.getSender() == null || mess.getSender().isBlank()) ? "Votre adversaire" : mess.getSender();
        ButtonType accepter = new ButtonType("Accepter");
        ButtonType refuser = new ButtonType("Refuser");

        Alert prompt = new Alert(Alert.AlertType.CONFIRMATION);
        prompt.setTitle("Revanche");
        prompt.setHeaderText(demandeur + " propose une revanche");
        prompt.setContentText("Voulez-vous relancer automatiquement une nouvelle partie ?");
        prompt.getButtonTypes().setAll(accepter, refuser);

        boolean acceptee = prompt.showAndWait().orElse(refuser) == accepter;
        joueur.repondreRevanche(acceptee);
        lblInfo.setText(acceptee ? "Revanche acceptée. Relance en cours..." : "Revanche refusée.");
        ajouterMessageChat("Systeme", demandeur + (acceptee ? " : revanche acceptée." : " : revanche refusée."));
    }

    private void onReponseRevanche(Message mess) {
        boolean acceptee = Boolean.TRUE.equals(mess.getRevancheAcceptee());
        String repondeur = (mess.getSender() == null || mess.getSender().isBlank()) ? "Votre adversaire" : mess.getSender();

        if (acceptee) {
            lblInfo.setText(repondeur + " a accepté la revanche. Nouvelle partie en cours...");
            ajouterMessageChat("Systeme", repondeur + " a accepté la revanche.");
            if (btnRevanche != null) btnRevanche.setDisable(true);
            return;
        }

        revancheDemandee = false;
        lblInfo.setText(repondeur + " a refusé la revanche.");
        ajouterMessageChat("Systeme", repondeur + " a refusé la revanche.");
        if (btnRevanche != null) btnRevanche.setDisable(false);
    }

    private void onAbandon() {
        if (joueur == null) return;
        joueur.abandonnerPartie();
        btnAbandon.setDisable(true);
        if (damierView != null) damierView.setDisable(true);
        lblInfo.setText("Abandon envoye au serveur...");
    }

    private void envoyerChat() {
        if (joueur == null) return;
        String texte = chatInput.getText();
        boolean ok = joueur.envoyerMessageTexte(texte);
        if (ok) {
            chatInput.clear();
            lblChatStatus.setText("");
        } else {
            lblChatStatus.setText("Message invalide (1..300 caracteres non vides).");
        }
    }

    private void ajouterMessageChat(String auteur, String contenu) {
        String a = (auteur == null || auteur.isBlank()) ? "Systeme" : auteur;
        String c = (contenu == null) ? "" : contenu;
        Label ligne = new Label(a + " : " + c);
        ligne.setWrapText(true);
        ligne.getStyleClass().add("chat-line");
        chatMessagesBox.getChildren().add(ligne);
        if (chatMessagesBox.getChildren().size() > 50) {
            chatMessagesBox.getChildren().remove(0);
        }
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
        majLabelJoueurs();
    }

    private void majLabelJoueurs() {
        String moi = "?";
        String adv = "?";
        if (maCouleur == Couleur.BLANC) {
            moi = pseudoBlanc;
            adv = pseudoNoir;
        } else if (maCouleur == Couleur.NOIR) {
            moi = pseudoNoir;
            adv = pseudoBlanc;
        }
        lblJoueurs.setText("Moi: " + moi + "  |  Adversaire: " + adv);
    }

    private StackPane creerOverlayFinPartie() {
        lblFinTitre = new Label();
        lblFinTitre.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblFinTitre.setStyle("-fx-text-fill: #3f2a1d;");

        lblFinDetail = new Label();
        lblFinDetail.setWrapText(true);
        lblFinDetail.setMaxWidth(320);
        lblFinDetail.setStyle("-fx-text-fill: #5d4037; -fx-font-size: 12;");

        btnRevanche = new Button("Proposer une revanche");
        btnRevanche.setStyle("-fx-background-color: #6a1b9a; -fx-text-fill: white; -fx-background-radius: 8;");
        btnRevanche.setOnAction(e -> onProposerRevanche());

        btnDeconnecter = new Button("Se deconnecter");
        btnDeconnecter.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white; -fx-background-radius: 8;");
        btnDeconnecter.setOnAction(e -> onSeDeconnecter());

        HBox actions = new HBox(10, btnRevanche, btnDeconnecter);
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(12, lblFinTitre, lblFinDetail, actions);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: rgba(255,248,240,0.97);"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: rgba(63,42,29,0.20);"
                + "-fx-border-radius: 16;");

        StackPane overlay = new StackPane(card);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        overlay.setVisible(false);
        overlay.setManaged(false);
        return overlay;
    }

    private void afficherOverlayFinPartie(String titre, String detail) {
        if (overlayFinPartie == null) return;
        lblFinTitre.setText(titre);
        lblFinDetail.setText(detail);
        btnRevanche.setDisable(revancheDemandee || joueur == null);
        btnDeconnecter.setDisable(joueur == null);
        overlayFinPartie.setVisible(true);
        overlayFinPartie.setManaged(true);
    }

    private void masquerOverlayFinPartie() {
        if (overlayFinPartie == null) return;
        overlayFinPartie.setVisible(false);
        overlayFinPartie.setManaged(false);
    }

    private void onProposerRevanche() {
        if (joueur == null || revancheDemandee) return;
        revancheDemandee = true;
        btnRevanche.setDisable(true);
        joueur.proposerRevanche();
        lblInfo.setText("Demande de revanche envoyée...");
        ajouterMessageChat("Systeme", "Demande de revanche envoyée.");
    }

    private void onSeDeconnecter() {
        if (joueur != null) {
            joueur.disconnectedServer();
        }
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void appliquerResponsiveDamier(DamierView vue) {
        vue.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0.55, Math.min(1.0,
                        Math.min((damierContainer.getWidth() - 10) / DamierView.TAILLE_DAMIER_PREF,
                                (damierContainer.getHeight() - 10) / DamierView.TAILLE_DAMIER_PREF))),
                damierContainer.widthProperty(), damierContainer.heightProperty()));
        vue.scaleYProperty().bind(vue.scaleXProperty());
    }

    private Label creerLabel(String texte) {
        Label l = new Label(texte);
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        return l;
    }
}

