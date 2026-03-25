package org.example.leprojet;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbitre du jeu de dames (10x10, règles françaises).
 *
 * Responsabilités :
 *  - initialiser la partie (joueurs + plateau)
 *  - gérer le tour courant
 *  - valider un déplacement simple et une prise
 *  - promouvoir un pion en dame
 *  - détecter la fin de partie
 */
public class arbitre {

    private final Plateau plateau;
    private final Joueur joueurBlanc;
    private final Joueur joueurNoir;

    /** Joueur dont c'est le tour. */
    private Joueur joueurCourant;

    /** État global de la partie. */
    private EtatPartie etat;

    // -------------------------------------------------------------------------
    // Construction / initialisation
    // -------------------------------------------------------------------------

    /**
     * Crée un arbitre et démarre immédiatement la partie.
     *
     * @param nomBlanc nom du joueur qui joue les pièces blanches
     * @param nomNoir  nom du joueur qui joue les pièces noires
     */
    public arbitre(String nomBlanc, String nomNoir) {
        this.plateau     = new Plateau();
        this.joueurBlanc = new Joueur(1, nomBlanc, Couleur.BLANC);
        this.joueurNoir  = new Joueur(2, nomNoir,  Couleur.NOIR);
        this.etat        = EtatPartie.EN_ATTENTE;
    }

    /**
     * Initialise le plateau, place les pions et démarre la partie.
     * Les noirs jouent toujours en premier (règle française).
     */
    public void initialiserPartie() {
        plateau.initPions();
        joueurCourant = joueurNoir;   // les noirs commencent
        etat          = EtatPartie.EN_COURS;
    }

    // -------------------------------------------------------------------------
    // Tour / déplacement
    // -------------------------------------------------------------------------

    /**
     * Tente un déplacement simple (sans prise) de {@code piece} vers {@code destination}.
     * Met à jour le tour et vérifie la fin de partie si le mouvement est valide.
     *
     * @param piece       pièce à déplacer (doit appartenir au joueur courant)
     * @param destination case cible (doit être vide et noire)
     * @return {@code true} si le déplacement a été effectué
     */
    public boolean jouerDeplacement(Piece piece, Case destination) {
        if (etat != EtatPartie.EN_COURS) return false;
        if (piece.getCouleur() != joueurCourant.getCouleur()) return false;
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;

        if (!estDeplacementValide(piece, destination)) return false;

        plateau.deplacerPiece(piece, destination);
        verifierPromotion(piece);
        passerLeTour();
        verifierFinDePartie();
        return true;
    }

    /**
     * Tente une prise : {@code piece} saute par-dessus {@code piecePrise} pour
     * atterrir sur {@code destination}.
     *
     * @param piece       pièce qui prend (doit appartenir au joueur courant)
     * @param piecePrise  pièce adverse à capturer
     * @param destination case d'arrivée (doit être vide et noire)
     * @return {@code true} si la prise a été effectuée
     */
    public boolean jouerPrise(Piece piece, Piece piecePrise, Case destination) {
        if (etat != EtatPartie.EN_COURS) return false;
        if (piece.getCouleur() != joueurCourant.getCouleur()) return false;
        if (piecePrise.getCouleur() == joueurCourant.getCouleur()) return false;
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;

        if (!estPriseValide(piece, piecePrise, destination)) return false;

        plateau.deplacerPiece(piece, destination);
        plateau.supprimerPiece(piecePrise);
        verifierPromotion(piece);
        passerLeTour();
        verifierFinDePartie();
        return true;
    }

    // -------------------------------------------------------------------------
    // Validation des mouvements
    // -------------------------------------------------------------------------

    /**
     * Vérifie qu'un déplacement simple est géométriquement valide.
     * Un pion ne peut avancer que d'une diagonale vers l'avant.
     * Une dame peut avancer d'autant de cases qu'elle veut en diagonale (simplifié ici à 1).
     */
    public boolean estDeplacementValide(Piece piece, Case destination) {
        int dl = destination.getLigne()   - piece.getPosition().getLigne();
        int dc = destination.getColonne() - piece.getPosition().getColonne();

        if (Math.abs(dl) != Math.abs(dc)) return false; // doit rester en diagonale

        if (piece instanceof Pion) {
            if (Math.abs(dl) != 1) return false;
            // Les blancs montent (lignes décroissantes), les noirs descendent
            if (piece.getCouleur() == Couleur.BLANC && dl >= 0) return false;
            if (piece.getCouleur() == Couleur.NOIR  && dl <= 0) return false;
        }
        // Pour une dame : n'importe quelle diagonale libre (sans saut de pièce)
        // La vérification du chemin libre est laissée à la couche supérieure
        return true;
    }

    /**
     * Vérifie qu'une prise est géométriquement valide :
     * la pièce sautée doit être exactement entre la position de départ et la destination.
     */
    public boolean estPriseValide(Piece piece, Piece piecePrise, Case destination) {
        int lDepart = piece.getPosition().getLigne();
        int cDepart = piece.getPosition().getColonne();
        int lArrivee = destination.getLigne();
        int cArrivee = destination.getColonne();
        int lPrise = piecePrise.getPosition().getLigne();
        int cPrise = piecePrise.getPosition().getColonne();

        // La destination doit être à 2 cases en diagonale
        if (Math.abs(lArrivee - lDepart) != 2) return false;
        if (Math.abs(cArrivee - cDepart) != 2) return false;

        // La pièce sautée doit être au milieu
        int lMilieu = (lDepart + lArrivee) / 2;
        int cMilieu = (cDepart + cArrivee) / 2;
        return lPrise == lMilieu && cPrise == cMilieu;
    }

    // -------------------------------------------------------------------------
    // Promotion
    // -------------------------------------------------------------------------

    /**
     * Promeut automatiquement un pion en dame s'il atteint la dernière rangée adverse.
     */
    private void verifierPromotion(Piece piece) {
        if (!(piece instanceof Pion)) return;
        int ligne = piece.getPosition().getLigne();
        boolean promouvoir =
                (piece.getCouleur() == Couleur.BLANC && ligne == 0) ||
                (piece.getCouleur() == Couleur.NOIR  && ligne == Plateau.NB_LIGNES - 1);
        if (promouvoir) {
            plateau.promouvoirPion((Pion) piece);
        }
    }

    // -------------------------------------------------------------------------
    // Gestion du tour
    // -------------------------------------------------------------------------

    /** Passe le tour au joueur adverse. */
    private void passerLeTour() {
        joueurCourant = (joueurCourant == joueurBlanc) ? joueurNoir : joueurBlanc;
    }

    // -------------------------------------------------------------------------
    // Fin de partie
    // -------------------------------------------------------------------------

    /**
     * Vérifie si la partie est terminée :
     * <ul>
     *   <li>Un joueur n'a plus de pièces → l'autre gagne.</li>
     *   <li>Le joueur courant n'a plus aucun mouvement possible → il perd.</li>
     * </ul>
     */
    public void verifierFinDePartie() {
        if (plateau.getNoires().isEmpty()) {
            etat = EtatPartie.BLANC_GAGNE;
            return;
        }
        if (plateau.getBlanches().isEmpty()) {
            etat = EtatPartie.NOIR_GAGNE;
            return;
        }
        if (!aDesMovementsPossibles(joueurCourant)) {
            // Le joueur courant est bloqué → il perd
            etat = (joueurCourant.getCouleur() == Couleur.BLANC)
                    ? EtatPartie.NOIR_GAGNE
                    : EtatPartie.BLANC_GAGNE;
        }
    }

    /**
     * Retourne {@code true} si le joueur donné possède au moins un mouvement légal
     * (déplacement simple ou prise).
     */
    public boolean aDesMovementsPossibles(Joueur joueur) {
        List<Piece> pieces = (joueur.getCouleur() == Couleur.BLANC)
                ? new ArrayList<>(plateau.getBlanches())
                : new ArrayList<>(plateau.getNoires());

        Case[][] cases = plateau.getCases();

        for (Piece piece : pieces) {
            for (int i = 0; i < Plateau.NB_LIGNES; i++) {
                for (int j = 0; j < Plateau.NB_COLONNES; j++) {
                    Case dest = cases[i][j];
                    if (!dest.estVide()) continue;
                    if (dest.getCouleur() != Couleur.NOIR) continue;
                    if (estDeplacementValide(piece, dest)) return true;

                    // Vérifie s'il existe une prise possible vers cette case
                    int dl = i - piece.getPosition().getLigne();
                    int dc = j - piece.getPosition().getColonne();
                    if (Math.abs(dl) == 2 && Math.abs(dc) == 2) {
                        int lm = (piece.getPosition().getLigne() + i) / 2;
                        int cm = (piece.getPosition().getColonne() + j) / 2;
                        Case milieu = cases[lm][cm];
                        if (!milieu.estVide() && milieu.getPiece().getCouleur() != joueur.getCouleur()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Plateau getPlateau()           { return plateau; }
    public Joueur  getJoueurBlanc()       { return joueurBlanc; }
    public Joueur  getJoueurNoir()        { return joueurNoir; }
    public Joueur  getJoueurCourant()     { return joueurCourant; }
    public EtatPartie getEtat()           { return etat; }

    /**
     * Retourne le joueur gagnant, ou {@code null} si la partie n'est pas terminée
     * ou est nulle.
     */
    public Joueur getGagnant() {
        if (etat == EtatPartie.BLANC_GAGNE) return joueurBlanc;
        if (etat == EtatPartie.NOIR_GAGNE)  return joueurNoir;
        return null;
    }

    @Override
    public String toString() {
        return "Arbitre [état=" + etat
                + ", tour=" + (joueurCourant != null ? joueurCourant.getNom() : "—")
                + ", blanches=" + plateau.getBlanches().size()
                + ", noires=" + plateau.getNoires().size() + "]";
    }
}
