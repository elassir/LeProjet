package org.example.leprojet;

import org.example.leprojet.core.Case;
import org.example.leprojet.core.Couleur;
import org.example.leprojet.core.Dame;
import org.example.leprojet.core.Piece;
import org.example.leprojet.core.Plateau;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculateur de mouvements pour le jeu de dames (10×10, règles françaises).
 * <p>
 * Gère les déplacements simples et les prises pour les pions ET les dames.
 * Les dames se déplacent et capturent sur toute la diagonale.
 */
public class MoveCalculator {

    private MoveCalculator() {
        // Classe utilitaire, pas d'instanciation
    }

    // ── Directions diagonales ──────────────────────────────────────────

    private static final int[][] DIRECTIONS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

    // =====================================================================
    //  Déplacements simples
    // =====================================================================

    /**
     * Retourne la liste des cases où la pièce peut se déplacer (sans prise).
     *
     * @param piece piece source
     * @param plateau plateau de jeu
     * @return destinations de deplacement simple
     */
    public static List<Case> getDeplacementsPossibles(Piece piece, Plateau plateau) {
        if (piece instanceof Dame) {
            return getDeplacementsDame(piece, plateau);
        } else {
            return getDeplacementsPion(piece, plateau);
        }
    }

    private static List<Case> getDeplacementsPion(Piece piece, Plateau plateau) {
        List<Case> deplacements = new ArrayList<>();
        Case[][] cases = plateau.getCases();
        int l = piece.getPosition().getLigne();
        int c = piece.getPosition().getColonne();

        // Un pion avance d'une case en diagonale
        // BLANC monte (dl = -1), NOIR descend (dl = +1)
        int sens = (piece.getCouleur() == Couleur.BLANC) ? -1 : 1;
        int[][] dirs = {{sens, -1}, {sens, 1}};

        for (int[] dir : dirs) {
            int nl = l + dir[0];
            int nc = c + dir[1];
            if (!estDansLePlateau(nl, nc)) continue;

            Case dest = cases[nl][nc];
            if (dest.estVide() && dest.getCouleur() == Couleur.NOIR) {
                deplacements.add(dest);
            }
        }
        return deplacements;
    }

    private static List<Case> getDeplacementsDame(Piece piece, Plateau plateau) {
        List<Case> deplacements = new ArrayList<>();
        Case[][] cases = plateau.getCases();
        int l = piece.getPosition().getLigne();
        int c = piece.getPosition().getColonne();

        // Une dame peut se déplacer sur toute la diagonale (cases vides)
        for (int[] dir : DIRECTIONS) {
            int nl = l + dir[0];
            int nc = c + dir[1];
            while (estDansLePlateau(nl, nc)) {
                Case dest = cases[nl][nc];
                if (!dest.estVide()) break; // bloquée par une pièce
                deplacements.add(dest);
                nl += dir[0];
                nc += dir[1];
            }
        }
        return deplacements;
    }

    // =====================================================================
    //  Prises
    // =====================================================================

    /**
     * Retourne la liste des cases de destination pour une prise valide.
     * Gère les pions (distance 2) et les dames (prises longues).
     *
     * @param piece piece source
     * @param plateau plateau de jeu
     * @return destinations de prise possibles
     */
    public static List<Case> getPrisesPossibles(Piece piece, Plateau plateau) {
        if (piece instanceof Dame) {
            return getPrisesDame(piece, plateau);
        } else {
            return getPrisesPion(piece, plateau);
        }
    }

    private static List<Case> getPrisesPion(Piece piece, Plateau plateau) {
        List<Case> prises = new ArrayList<>();
        Case[][] cases = plateau.getCases();
        int l = piece.getPosition().getLigne();
        int c = piece.getPosition().getColonne();

        // Un pion peut capturer dans les 4 directions diagonales (distance 2)
        for (int[] dir : DIRECTIONS) {
            int lMil = l + dir[0];
            int cMil = c + dir[1];
            int lArr = l + 2 * dir[0];
            int cArr = c + 2 * dir[1];

            if (!estDansLePlateau(lArr, cArr)) continue;

            Case milieu = cases[lMil][cMil];
            Case dest = cases[lArr][cArr];

            if (milieu.estVide()) continue;
            if (milieu.getPiece().getCouleur() == piece.getCouleur()) continue;
            if (!dest.estVide()) continue;

            prises.add(dest);
        }
        return prises;
    }

    private static List<Case> getPrisesDame(Piece piece, Plateau plateau) {
        List<Case> prises = new ArrayList<>();
        Case[][] cases = plateau.getCases();
        int l = piece.getPosition().getLigne();
        int c = piece.getPosition().getColonne();

        // Une dame parcourt chaque diagonale, cherche UNE pièce adverse,
        // puis peut atterrir sur n'importe quelle case vide après.
        for (int[] dir : DIRECTIONS) {
            int nl = l + dir[0];
            int nc = c + dir[1];
            Piece pieceTrouvee = null;

            while (estDansLePlateau(nl, nc)) {
                Case courante = cases[nl][nc];

                if (!courante.estVide()) {
                    Piece occupant = courante.getPiece();
                    if (occupant.getCouleur() == piece.getCouleur()) {
                        break; // pièce alliée → direction bloquée
                    }
                    if (pieceTrouvee != null) {
                        break; // 2e pièce adverse → direction bloquée
                    }
                    pieceTrouvee = occupant;
                } else {
                    // Case vide
                    if (pieceTrouvee != null) {
                        // Case vide après une pièce adverse → destination de prise valide
                        prises.add(courante);
                    }
                }

                nl += dir[0];
                nc += dir[1];
            }
        }
        return prises;
    }

    // =====================================================================
    //  Validation de coups
    // =====================================================================

    /**
     * Vérifie qu'un déplacement simple est géométriquement valide.
     *
     * @param piece piece source
     * @param destination case d'arrivee
     * @param plateau plateau de jeu
     * @return true si le deplacement est valide
     */
    public static boolean estDeplacementValide(Piece piece, Case destination, Plateau plateau) {
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;

        if (piece instanceof Dame) {
            return getDeplacementsDame(piece, plateau).contains(destination);
        }

        // Pion : distance 1 en diagonale, dans le bon sens
        int dl = destination.getLigne() - piece.getPosition().getLigne();
        int dc = destination.getColonne() - piece.getPosition().getColonne();

        if (Math.abs(dl) != 1 || Math.abs(dc) != 1) return false;
        if (piece.getCouleur() == Couleur.BLANC && dl >= 0) return false;
        if (piece.getCouleur() == Couleur.NOIR && dl <= 0) return false;

        return true;
    }

    /**
     * Vérifie qu'une prise est géométriquement valide.
     * Pour un pion : distance 2, pièce adverse au milieu.
     * Pour une dame : prise longue sur la diagonale.
     *
     * @param piece piece qui capture
     * @param piecePrise piece adverse capturee
     * @param destination case d'arrivee
     * @param plateau plateau de jeu
     * @return true si la prise est valide
     */
    public static boolean estPriseValide(Piece piece, Piece piecePrise, Case destination, Plateau plateau) {
        if (!destination.estVide()) return false;
        if (destination.getCouleur() != Couleur.NOIR) return false;
        if (piecePrise.getCouleur() == piece.getCouleur()) return false;

        if (piece instanceof Dame) {
            return estPriseValideDame(piece, piecePrise, destination, plateau);
        } else {
            return estPriseValidePion(piece, piecePrise, destination);
        }
    }

    private static boolean estPriseValidePion(Piece piece, Piece piecePrise, Case destination) {
        int lDep = piece.getPosition().getLigne();
        int cDep = piece.getPosition().getColonne();
        int lArr = destination.getLigne();
        int cArr = destination.getColonne();
        int lPrise = piecePrise.getPosition().getLigne();
        int cPrise = piecePrise.getPosition().getColonne();

        if (Math.abs(lArr - lDep) != 2 || Math.abs(cArr - cDep) != 2) return false;

        int lMilieu = (lDep + lArr) / 2;
        int cMilieu = (cDep + cArr) / 2;
        return lPrise == lMilieu && cPrise == cMilieu;
    }

    private static boolean estPriseValideDame(Piece piece, Piece piecePrise, Case destination, Plateau plateau) {
        Case[][] cases = plateau.getCases();
        int lDep = piece.getPosition().getLigne();
        int cDep = piece.getPosition().getColonne();
        int lArr = destination.getLigne();
        int cArr = destination.getColonne();

        // Vérifier que c'est une diagonale
        int dl = lArr - lDep;
        int dc = cArr - cDep;
        if (Math.abs(dl) != Math.abs(dc)) return false;

        int stepL = Integer.signum(dl);
        int stepC = Integer.signum(dc);

        // Parcourir la diagonale entre départ et arrivée
        // On doit trouver exactement la piecePrise et aucune autre pièce
        boolean trouve = false;
        int l = lDep + stepL;
        int c = cDep + stepC;

        while (l != lArr || c != cArr) {
            Case courante = cases[l][c];
            if (!courante.estVide()) {
                if (courante.getPiece() == piecePrise) {
                    trouve = true;
                } else {
                    return false; // autre pièce sur le chemin
                }
            }
            l += stepL;
            c += stepC;
        }

        return trouve;
    }

    // =====================================================================
    //  Trouver la pièce prise entre deux positions (pour le serveur)
    // =====================================================================

    /**
     * Trouve la pièce adverse entre la position de départ et la destination.
     * Utilisé quand on reçoit un coup du réseau (lDep, cDep, lArr, cArr).
     *
     * @param piece piece source
     * @param destination case d'arrivee
     * @param plateau plateau de jeu
     * @return la pièce prise, ou null si c'est un déplacement simple.
     */
    public static Piece trouverPiecePrise(Piece piece, Case destination, Plateau plateau) {
        Case[][] cases = plateau.getCases();
        int lDep = piece.getPosition().getLigne();
        int cDep = piece.getPosition().getColonne();
        int lArr = destination.getLigne();
        int cArr = destination.getColonne();

        int dl = lArr - lDep;
        int dc = cArr - cDep;
        if (Math.abs(dl) != Math.abs(dc)) return null;
        if (Math.abs(dl) < 2) return null; // pas une prise

        int stepL = Integer.signum(dl);
        int stepC = Integer.signum(dc);

        int l = lDep + stepL;
        int c = cDep + stepC;

        while (l != lArr || c != cArr) {
            Case courante = cases[l][c];
            if (!courante.estVide()) {
                Piece occupant = courante.getPiece();
                if (occupant.getCouleur() != piece.getCouleur()) {
                    return occupant;
                }
            }
            l += stepL;
            c += stepC;
        }
        return null;
    }

    // =====================================================================
    //  Utilitaire : vérifier si le joueur a une prise
    // =====================================================================

    /**
     * Retourne true si au moins une des pièces de la liste a une prise possible.
     *
     * @param pieces pieces a analyser
     * @param plateau plateau de jeu
     * @return true si une prise existe
     */
    public static boolean aUnePrisePossible(List<Piece> pieces, Plateau plateau) {
        for (Piece piece : pieces) {
            if (!getPrisesPossibles(piece, plateau).isEmpty()) return true;
        }
        return false;
    }

    /**
     * Retourne les pièces qui disposent d'au moins une prise légale.
     *
     * @param pieces pieces a analyser
     * @param plateau plateau de jeu
     * @return pieces pouvant effectuer une prise
     */
    public static List<Piece> getPiecesAvecPrisePossible(List<Piece> pieces, Plateau plateau) {
        List<Piece> resultat = new ArrayList<>();
        for (Piece piece : pieces) {
            if (!getPrisesPossibles(piece, plateau).isEmpty()) {
                resultat.add(piece);
            }
        }
        return resultat;
    }

    /**
     * Retourne true si au moins une des pièces de la liste a un mouvement (prise ou déplacement).
     *
     * @param pieces pieces a analyser
     * @param plateau plateau de jeu
     * @return true si un mouvement legal existe
     */
    public static boolean aUnMouvementPossible(List<Piece> pieces, Plateau plateau) {
        for (Piece piece : pieces) {
            if (!getPrisesPossibles(piece, plateau).isEmpty()) return true;
            if (!getDeplacementsPossibles(piece, plateau).isEmpty()) return true;
        }
        return false;
    }

    // ── Utilitaire interne ─────────────────────────────────────────────

    private static boolean estDansLePlateau(int l, int c) {
        return l >= 0 && l < Plateau.NB_LIGNES && c >= 0 && c < Plateau.NB_COLONNES;
    }
}
