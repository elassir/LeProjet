package org.example.leprojet;

import org.example.leprojet.core.Case;
import org.example.leprojet.core.Couleur;
import org.example.leprojet.core.EtatPartie;
import org.example.leprojet.core.JoueurPartie;
import org.example.leprojet.core.Piece;
import org.example.leprojet.core.Pion;
import org.example.leprojet.core.Plateau;

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
public class Arbitre {

    private final Plateau plateau;
    private final JoueurPartie joueurBlanc;
    private final JoueurPartie joueurNoir;

    /** Joueur dont c'est le tour. */
    private JoueurPartie joueurCourant;

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

    /**
     * Construit un arbitre avec deux joueurs et un plateau vide.
     *
     * @param nomBlanc nom du joueur blanc
     * @param nomNoir nom du joueur noir
     */
    public Arbitre(String nomBlanc, String nomNoir) {
        this.plateau = new Plateau();
        this.joueurBlanc = new JoueurPartie(1, nomBlanc, Couleur.BLANC);
        this.joueurNoir = new JoueurPartie(2, nomNoir, Couleur.NOIR);
        this.etat = EtatPartie.EN_ATTENTE;
        this.pieceEnChaine = null;
    }

    /**
     * Initialise le plateau, place les pions et démarre la partie.
     * Les blancs jouent en premier.
     */
    public void initialiserPartie() {
        plateau.initPions();
        joueurCourant = joueurBlanc;
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
     *
     * @param piece pièce à déplacer
     * @param destination case cible
     * @return true si le déplacement est validé et appliqué
     */
    public boolean jouerDeplacement(Piece piece, Case destination) {
        if (etat != EtatPartie.EN_COURS) return false;
        if (piece.getCouleur() != joueurCourant.getCouleur()) return false;
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;
        if (pieceEnChaine != null) return false;
        if (joueurCourantAPrisePossible()) return false;
        if (!MoveCalculator.estDeplacementValide(piece, destination, plateau)) return false;

        int lDep = piece.getPosition().getLigne();
        int cDep = piece.getPosition().getColonne();
        plateau.deplacerPiece(piece, destination);
        System.out.println("[MOUVEMENT][VALIDE] "
                + "dep=(" + lDep + "," + cDep + ")"
                + " arr=(" + destination.getLigne() + "," + destination.getColonne() + ")");
        verifierPromotion(piece);
        passerLeTour();
        verifierFinDePartie();
        return true;
    }

    /**
     * Tente une prise : piece saute par-dessus piecePrise pour atterrir sur destination.
     * Après la prise, si la même pièce peut encore manger, le tour ne passe pas.
     *
     * @param piece pièce qui effectue la prise
     * @param piecePrise pièce adverse capturée
     * @param destination case d'arrivée
     * @return true si la prise est validée et appliquée
     */
    public boolean jouerPrise(Piece piece, Piece piecePrise, Case destination) {
        if (etat != EtatPartie.EN_COURS) return false;
        if (piece.getCouleur() != joueurCourant.getCouleur()) return false;
        if (piecePrise.getCouleur() == joueurCourant.getCouleur()) return false;
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;
        if (pieceEnChaine != null && piece != pieceEnChaine) return false;
        if (!MoveCalculator.estPriseValide(piece, piecePrise, destination, plateau)) return false;

        int lDep = piece.getPosition().getLigne();
        int cDep = piece.getPosition().getColonne();
        plateau.deplacerPiece(piece, destination);
        plateau.supprimerPiece(piecePrise);
        System.out.println("[MOUVEMENT][VALIDE] dep=(" + lDep + "," + cDep + ") arr=("
                + destination.getLigne() + "," + destination.getColonne() + ") [PRISE]");
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

    /**
     * Indique si le joueur courant dispose d'au moins une prise légale.
     *
     * @return true si une prise est possible pour le joueur courant
     */
    public boolean joueurCourantAPrisePossible() {
        List<Piece> pieces = getPiecesJoueurCourant();
        return MoveCalculator.aUnePrisePossible(pieces, plateau);
    }

    /**
     * Liste les destinations de prise possibles pour une pièce donnée.
     *
     * @param piece pièce testée
     * @return liste des cases de destination pour une prise
     */
    public List<Case> getPrisesPossiblesPour(Piece piece) {
        if (piece == null) return new ArrayList<>();
        return MoveCalculator.getPrisesPossibles(piece, plateau);
    }

    /**
     * Retourne les pièces du joueur courant qui sont autorisées à jouer
     * lorsqu'une prise obligatoire existe.
     *
     * @return liste des pièces devant être mises en évidence
     */
    public List<Piece> getPiecesAvecPriseObligatoire() {
        if (joueurCourant == null) return new ArrayList<>();
        return getPiecesAvecPriseObligatoire(joueurCourant.getCouleur());
    }

    /**
     * Retourne les pièces de la couleur donnée disposant d'au moins une prise.
     * En chaîne de prises, seule la pièce imposée est retournée.
     *
     * @param couleur couleur à analyser
     * @return pièces ayant une prise obligatoire
     */
    public List<Piece> getPiecesAvecPriseObligatoire(Couleur couleur) {
        List<Piece> resultat = new ArrayList<>();
        if (etat != EtatPartie.EN_COURS || couleur == null) return resultat;

        if (pieceEnChaine != null) {
            if (pieceEnChaine.getCouleur() == couleur) {
                resultat.add(pieceEnChaine);
            }
            return resultat;
        }

        List<Piece> pieces = (couleur == Couleur.BLANC)
                ? new ArrayList<>(plateau.getBlanches())
                : new ArrayList<>(plateau.getNoires());
        return MoveCalculator.getPiecesAvecPrisePossible(pieces, plateau);
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

    /**
     * Vérifie et met à jour l'état final de la partie.
     *
     * L'état passe à BLANC_GAGNE ou NOIR_GAGNE si un joueur n'a plus de pièces
     * ou ne peut plus jouer.
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
            etat = (joueurCourant.getCouleur() == Couleur.BLANC)
                    ? EtatPartie.NOIR_GAGNE
                    : EtatPartie.BLANC_GAGNE;
        }
    }

    public boolean aDesMovementsPossibles(JoueurPartie joueur) {
        List<Piece> pieces = (joueur.getCouleur() == Couleur.BLANC)
                ? new ArrayList<>(plateau.getBlanches())
                : new ArrayList<>(plateau.getNoires());
        return MoveCalculator.aUnMouvementPossible(pieces, plateau);
    }

    /**
     * Termine immédiatement la partie suite à un abandon.
     *
     * @param couleurAbandonne couleur du joueur qui abandonne
     */
    public void abandonner(Couleur couleurAbandonne) {
        if (etat != EtatPartie.EN_COURS) return;
        etat = (couleurAbandonne == Couleur.BLANC) ? EtatPartie.NOIR_GAGNE : EtatPartie.BLANC_GAGNE;
        pieceEnChaine = null;
        System.out.println("[ARBITRE] Abandon reçu: " + couleurAbandonne + " -> " + getGagnant() + " gagne");
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
    public JoueurPartie getJoueurBlanc()       { return joueurBlanc; }
    public JoueurPartie getJoueurNoir()        { return joueurNoir; }
    public JoueurPartie getJoueurCourant()     { return joueurCourant; }
    public EtatPartie getEtat()          { return etat; }

    public JoueurPartie getGagnant() {
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
