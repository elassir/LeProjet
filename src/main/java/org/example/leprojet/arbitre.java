package org.example.leprojet;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbitre du jeu de dames (10×10, règles françaises).
 * <p>
 * Responsabilités :
 * <ul>
 *   <li>initialiser la partie (joueurs + plateau)</li>
 *   <li>gérer le tour courant</li>
 *   <li>valider un déplacement simple et une prise (via {@link MoveCalculator})</li>
 *   <li>prise obligatoire : si une prise est possible, le déplacement simple est interdit</li>
 *   <li>prise en chaîne : après une prise, si la même pièce peut encore manger, elle doit continuer</li>
 *   <li>promouvoir un pion en dame</li>
 *   <li>détecter la fin de partie</li>
 * </ul>
 */
public class arbitre {

    private final Plateau plateau;
    private final Joueur joueurBlanc;
    private final Joueur joueurNoir;

    /** Joueur dont c'est le tour. */
    private Joueur joueurCourant;

    /** État global de la partie. */
    private EtatPartie etat;

    /**
     * Pièce en cours de chaîne de prises.
     * Non-null uniquement lorsque la pièce vient de manger et peut encore manger.
     */
    private Piece pieceEnChaine;

    // -------------------------------------------------------------------------
    // Construction / initialisation
    // -------------------------------------------------------------------------

    public arbitre(String nomBlanc, String nomNoir) {
        this.plateau = new Plateau();
        this.joueurBlanc = new Joueur(1, nomBlanc, Couleur.BLANC);
        this.joueurNoir = new Joueur(2, nomNoir, Couleur.NOIR);
        this.etat = EtatPartie.EN_ATTENTE;
        this.pieceEnChaine = null;
    }

    /**
     * Initialise le plateau, place les pions et démarre la partie.
     * Les noirs jouent toujours en premier (règle française).
     */
    public void initialiserPartie() {
        plateau.initPions();
        joueurCourant = joueurNoir;
        etat = EtatPartie.EN_COURS;
        pieceEnChaine = null;
    }

    // -------------------------------------------------------------------------
    // Tour / déplacement
    // -------------------------------------------------------------------------

    /**
     * Tente un déplacement simple (sans prise).
     * Interdit si le joueur courant a au moins une prise possible.
     * Interdit si on est en chaîne de prises.
     */
    public boolean jouerDeplacement(Piece piece, Case destination) {
        if (etat != EtatPartie.EN_COURS) return false;
        if (piece.getCouleur() != joueurCourant.getCouleur()) return false;
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;
        if (pieceEnChaine != null) return false;
        if (joueurCourantAPrisePossible()) return false;
        if (!MoveCalculator.estDeplacementValide(piece, destination, plateau)) return false;

        plateau.deplacerPiece(piece, destination);
        verifierPromotion(piece);
        passerLeTour();
        verifierFinDePartie();
        return true;
    }

    /**
     * Tente une prise : piece saute par-dessus piecePrise pour atterrir sur destination.
     * Après la prise, si la même pièce peut encore manger, le tour ne passe pas.
     */
    public boolean jouerPrise(Piece piece, Piece piecePrise, Case destination) {
        if (etat != EtatPartie.EN_COURS) return false;
        if (piece.getCouleur() != joueurCourant.getCouleur()) return false;
        if (piecePrise.getCouleur() == joueurCourant.getCouleur()) return false;
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;
        if (pieceEnChaine != null && piece != pieceEnChaine) return false;
        if (!MoveCalculator.estPriseValide(piece, piecePrise, destination, plateau)) return false;

        plateau.deplacerPiece(piece, destination);
        plateau.supprimerPiece(piecePrise);
        verifierPromotion(piece);

        Piece pieceSurCase = destination.getPiece();
        List<Case> prisesSuivantes = MoveCalculator.getPrisesPossibles(pieceSurCase, plateau);

        if (!prisesSuivantes.isEmpty()) {
            pieceEnChaine = pieceSurCase;
            System.out.println("[ARBITRE] Chaîne de prises : " + pieceSurCase
                    + " peut encore manger vers " + prisesSuivantes.size() + " case(s)");
        } else {
            pieceEnChaine = null;
            passerLeTour();
            verifierFinDePartie();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Prise obligatoire & chaîne de prises
    // -------------------------------------------------------------------------

    public boolean joueurCourantAPrisePossible() {
        List<Piece> pieces = getPiecesJoueurCourant();
        return MoveCalculator.aUnePrisePossible(pieces, plateau);
    }

    public List<Case> getPrisesPossiblesPour(Piece piece) {
        if (piece == null) return new ArrayList<>();
        return MoveCalculator.getPrisesPossibles(piece, plateau);
    }

    public boolean isEnChaineDePrise() {
        return pieceEnChaine != null;
    }

    public Piece getPieceEnChaine() {
        return pieceEnChaine;
    }

    // -------------------------------------------------------------------------
    // Validation des mouvements (délègue à MoveCalculator)
    // -------------------------------------------------------------------------

    public boolean estDeplacementValide(Piece piece, Case destination) {
        return MoveCalculator.estDeplacementValide(piece, destination, plateau);
    }

    public boolean estPriseValide(Piece piece, Piece piecePrise, Case destination) {
        return MoveCalculator.estPriseValide(piece, piecePrise, destination, plateau);
    }

    // -------------------------------------------------------------------------
    // Promotion
    // -------------------------------------------------------------------------

    private void verifierPromotion(Piece piece) {
        if (!(piece instanceof Pion)) return;
        int ligne = piece.getPosition().getLigne();
        boolean promouvoir =
                (piece.getCouleur() == Couleur.BLANC && ligne == 0)
                        || (piece.getCouleur() == Couleur.NOIR && ligne == Plateau.NB_LIGNES - 1);
        if (promouvoir) {
            plateau.promouvoirPion((Pion) piece);
        }
    }

    // -------------------------------------------------------------------------
    // Gestion du tour
    // -------------------------------------------------------------------------

    private void passerLeTour() {
        joueurCourant = (joueurCourant == joueurBlanc) ? joueurNoir : joueurBlanc;
    }

    // -------------------------------------------------------------------------
    // Fin de partie
    // -------------------------------------------------------------------------

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
            etat = (joueurCourant.getCouleur() == Couleur.BLANC)
                    ? EtatPartie.NOIR_GAGNE
                    : EtatPartie.BLANC_GAGNE;
        }
    }

    public boolean aDesMovementsPossibles(Joueur joueur) {
        List<Piece> pieces = (joueur.getCouleur() == Couleur.BLANC)
                ? new ArrayList<>(plateau.getBlanches())
                : new ArrayList<>(plateau.getNoires());
        return MoveCalculator.aUnMouvementPossible(pieces, plateau);
    }

    // -------------------------------------------------------------------------
    // Utilitaires internes
    // -------------------------------------------------------------------------

    private List<Piece> getPiecesJoueurCourant() {
        return (joueurCourant.getCouleur() == Couleur.BLANC)
                ? new ArrayList<>(plateau.getBlanches())
                : new ArrayList<>(plateau.getNoires());
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Plateau getPlateau()          { return plateau; }
    public Joueur getJoueurBlanc()       { return joueurBlanc; }
    public Joueur getJoueurNoir()        { return joueurNoir; }
    public Joueur getJoueurCourant()     { return joueurCourant; }
    public EtatPartie getEtat()          { return etat; }

    public Joueur getGagnant() {
        if (etat == EtatPartie.BLANC_GAGNE) return joueurBlanc;
        if (etat == EtatPartie.NOIR_GAGNE) return joueurNoir;
        return null;
    }

    @Override
    public String toString() {
        return "Arbitre [état=" + etat
                + ", tour=" + (joueurCourant != null ? joueurCourant.getNom() : "—")
                + ", blanches=" + plateau.getBlanches().size()
                + ", noires=" + plateau.getNoires().size()
                + ", enChaine=" + (pieceEnChaine != null) + "]";
    }
}
