package org.example.leprojet.server;

import org.example.leprojet.MoveCalculator;
import org.example.leprojet.Arbitre;
import org.example.leprojet.core.Case;
import org.example.leprojet.core.Piece;
import org.example.leprojet.core.Plateau;
import org.example.leprojet.core.Couleur;
import org.example.leprojet.core.EtatPartie;
import org.example.leprojet.common.Message;
import org.example.leprojet.common.MessageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serveur de jeu de dames.
 * <ul>
 *   <li>Accepte 2 clients, leur assigne NOIR et BLANC.</li>
 *   <li>Héberge l'arbitre qui valide chaque coup.</li>
 *   <li>Broadcast les coups validés aux 2 clients.</li>
 * </ul>
 */
public class Server {

    private final int port;
    private final List<ConnectedClient> clients;
    private Arbitre arb;
    private boolean partieDemarree;

    /**
     * Cree un serveur de jeu et demarre le thread d'ecoute des connexions.
     *
     * @param port port TCP d'ecoute
     * @throws IOException si la socket serveur ne peut pas etre creee
     */
    public Server(int port) throws IOException {
        this.port = port;
        this.clients = new ArrayList<>();
        this.partieDemarree = false;
        new Thread(new Connection(this)).start();
        System.out.println("[SERVEUR] Démarré sur le port " + port);
    }

    /**
     * @return port TCP d'ecoute du serveur
     */
    public int getPort() { return port; }

    // ── Connexion d'un client ──────────────────────────────────────────

    /**
     * Ajoute un client connecte, assigne sa couleur et demarre la partie a 2 joueurs.
     *
     * @param newClient client nouvellement connecte
     */
    public synchronized void addClient(ConnectedClient newClient) {
        if (clients.size() >= 2) {
            newClient.sendMessage(new Message("Serveur", "Partie déjà pleine."));
            newClient.closeClient();
            return;
        }

        clients.add(newClient);

        // Assigner la couleur : 1er = NOIR, 2e = BLANC
        String couleur = (clients.size() == 1) ? "NOIR" : "BLANC";
        newClient.setCouleur(couleur);
        newClient.sendMessage(Message.assignationCouleur(couleur));
        System.out.println("[SERVEUR] Client " + newClient.getId() + " → " + couleur);

        essayerDemarrerPartie();
    }

    // ── Démarrage de la partie ─────────────────────────────────────────

    private void demarrerPartie() {
        ConnectedClient blanc = trouverParCouleur("BLANC");
        ConnectedClient noir = trouverParCouleur("NOIR");
        String nomBlanc = (blanc == null) ? "BLANC" : blanc.getPseudoAffiche();
        String nomNoir = (noir == null) ? "NOIR" : noir.getPseudoAffiche();

        arb = new Arbitre(nomBlanc, nomNoir);
        arb.initialiserPartie();
        partieDemarree = true;

        diffuserInfosJoueurs();
        broadcastToAll(Message.debutPartie());
        System.out.println("[SERVEUR] Partie démarrée ! " + arb);
    }

    private void essayerDemarrerPartie() {
        if (partieDemarree || clients.size() != 2) return;
        if (!clientsPrets()) return;
        demarrerPartie();
    }

    private boolean clientsPrets() {
        for (ConnectedClient c : clients) {
            if (!c.hasHelloPseudo()) {
                return false;
            }
        }
        return true;
    }

    private void diffuserInfosJoueurs() {
        ConnectedClient blanc = trouverParCouleur("BLANC");
        ConnectedClient noir = trouverParCouleur("NOIR");
        if (blanc == null || noir == null) return;

        Message infos = Message.infosJoueurs(blanc.getPseudoAffiche(), noir.getPseudoAffiche());
        broadcastToAll(infos);
    }

    private ConnectedClient trouverParCouleur(String couleur) {
        for (ConnectedClient client : clients) {
            if (couleur.equals(client.getCouleur())) {
                return client;
            }
        }
        return null;
    }

    // ── Réception d'un message d'un client ─────────────────────────────

    /**
     * Route un message recu vers le traitement metier adapte.
     *
     * @param sender client emetteur
     * @param mess message recu
     */
    public synchronized void onMessageRecu(ConnectedClient sender, Message mess) {
        System.out.println("[SERVEUR][DISPATCH] client=" + sender.getId() + " type=" + mess.getType());
        switch (mess.getType()) {
            case HELLO_PSEUDO -> traiterHelloPseudo(sender, mess);
            case COUP -> traiterCoup(sender, mess);
            case ABANDON -> traiterAbandon(sender, mess);
            case TEXTE -> traiterTexte(sender, mess);
            default -> {
                mess.setSender(sender.getPseudoAffiche());
                broadcastToAll(mess);
            }
        }
    }

    private void traiterHelloPseudo(ConnectedClient sender, Message mess) {
        String brut = mess.getContent() == null ? "" : mess.getContent();
        String pseudo = brut.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        if (pseudo.isBlank()) {
            sender.sendMessage(new Message("Serveur", "Pseudo invalide."));
            return;
        }
        if (pseudo.length() > 24) {
            pseudo = pseudo.substring(0, 24);
        }

        sender.setPseudo(pseudo);
        diffuserInfosJoueurs();
        essayerDemarrerPartie();
    }

    private void traiterTexte(ConnectedClient sender, Message mess) {
        String brut = mess.getContent() == null ? "" : mess.getContent();
        String nettoye = brut.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        if (nettoye.isBlank() || nettoye.length() > 300) {
            sender.sendMessage(new Message("Serveur", "Message chat invalide (1..300 caractères requis)."));
            return;
        }

        String horodatage = java.time.LocalTime.now().withNano(0).toString();
        Message diffusion = Message.texte(sender.getPseudoAffiche(), "[" + horodatage + "] " + nettoye);
        diffusion.setTimestamp(System.currentTimeMillis());
        broadcastToAll(diffusion);
    }

    private void traiterAbandon(ConnectedClient sender, Message mess) {
        if (arb != null && arb.getEtat() == EtatPartie.EN_COURS) {
            Couleur couleurAbandonne = "BLANC".equals(sender.getCouleur()) ? Couleur.BLANC : Couleur.NOIR;
            arb.abandonner(couleurAbandonne);
        }
        String gagnant = "BLANC".equals(sender.getCouleur()) ? "NOIR" : "BLANC";
        System.out.println("[SERVEUR] Abandon reçu du client " + sender.getPseudoAffiche() + " -> gagnant=" + gagnant);
        broadcastToAll(Message.finPartie(gagnant));
        broadcastToAll(new Message("Serveur", "Le joueur " + sender.getPseudoAffiche() + " a abandonné."));
    }

    // ── Traitement d'un coup ───────────────────────────────────────────

    private void traiterCoup(ConnectedClient sender, Message mess) {
        if (arb == null) {
            sender.sendMessage(new Message(MessageType.COUP_INVALIDE, "Serveur", "Partie non démarrée."));
            return;
        }

        // Vérifier que c'est le tour de ce client
        String tourCouleur = arb.getJoueurCourant().getCouleur().toString();
        if (!tourCouleur.equals(sender.getCouleur())) {
            sender.sendMessage(new Message(MessageType.COUP_INVALIDE, "Serveur", "Ce n'est pas votre tour."));
            return;
        }

        Plateau plateau = arb.getPlateau();
        Case[][] cases = plateau.getCases();

        int lDep = mess.getLigneDepart();
        int cDep = mess.getColonneDepart();
        int lArr = mess.getLigneArrivee();
        int cArr = mess.getColonneArrivee();

        Case caseDepart = cases[lDep][cDep];
        Case caseArrivee = cases[lArr][cArr];
        Piece piece = caseDepart.getPiece();

        if (piece == null) {
            sender.sendMessage(new Message(MessageType.COUP_INVALIDE, "Serveur", "Pas de pièce en départ."));
            return;
        }

        boolean ok = false;

        // Tenter une prise (utilise MoveCalculator pour supporter pions ET dames)
        Piece piecePrise = MoveCalculator.trouverPiecePrise(piece, caseArrivee, plateau);
        if (piecePrise != null) {
            ok = arb.jouerPrise(piece, piecePrise, caseArrivee);
        }

        // Sinon déplacement simple
        if (!ok) {
            ok = arb.jouerDeplacement(piece, caseArrivee);
        }

        if (ok) {
            Message coupOk = Message.coupValide(lDep, cDep, lArr, cArr);
            broadcastToAll(coupOk);
            System.out.println("[SERVEUR][MOUVEMENT] dep=(" + lDep + "," + cDep + ") arr=(" + lArr + "," + cArr + ")");
            System.out.println("[SERVEUR] Coup validé : " + coupOk + " | " + arb);

            // Vérifier fin de partie
            if (arb.getGagnant() != null) {
                broadcastToAll(Message.finPartie(arb.getGagnant().getCouleur().toString()));
                System.out.println("[SERVEUR] Partie terminée ! Gagnant : " + arb.getGagnant());
            }
        } else {
            sender.sendMessage(new Message(MessageType.COUP_INVALIDE, "Serveur", "Coup invalide."));
        }
    }

    // ── Broadcast ──────────────────────────────────────────────────────

    /**
     * Diffuse un message a tous les clients connectes.
     *
     * @param mess message a diffuser
     */
    public synchronized void broadcastToAll(Message mess) {
        for (ConnectedClient client : clients) {
            client.sendMessage(mess);
        }
    }

    // ── Déconnexion ────────────────────────────────────────────────────

    /**
     * Supprime un client deconnecte et notifie les autres clients.
     *
     * @param discClient client deconnecte
     */
    public synchronized void disconnectedClient(ConnectedClient discClient) {
        discClient.closeClient();
        clients.remove(discClient);
        partieDemarree = false;
        broadcastToAll(new Message("Serveur", "Le joueur " + discClient.getPseudoAffiche() + " s'est déconnecté."));
        System.out.println("[SERVEUR] Client " + discClient.getPseudoAffiche() + " déconnecté.");
    }
}