package org.example.leprojet.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Modele du damier 10x10.
 *
 * Cette classe encapsule les cases et les collections de pieces blanches/noires,
 * et fournit les operations de base de mutation du plateau.
 */
public class Plateau {

    public static final int NB_COLONNES = 10;
    public static final int NB_LIGNES = 10;
    private final Case[][] cases;
    private List<Piece> noires;
    private List<Piece> blanches;

    public Plateau() {
        this.cases = new Case[NB_LIGNES][NB_COLONNES];
        for (int i = 0; i < NB_LIGNES; i++) {
            for (int j = 0; j < NB_COLONNES; j++) {
                if (((i + j) % 2) == 0) {
                    this.cases[i][j] = new Case(Couleur.BLANC, i, j);
                } else {
                    this.cases[i][j] = new Case(Couleur.NOIR, i, j);
                }
            }
        }
        this.noires = new ArrayList<>();
        this.blanches = new ArrayList<>();
    }

    /**
     * Retourne la matrice des cases du plateau.
     *
     * @return matrice 10x10 des cases
     */
    public Case[][] getCases() {
        return this.cases;
    }

    /**
     * Retourne la collection des pieces noires presentes sur le plateau.
     *
     * @return liste des pieces noires
     */
    public List<Piece> getNoires() {
        return this.noires;
    }

    /**
     * Retourne la collection des pieces blanches presentes sur le plateau.
     *
     * @return liste des pieces blanches
     */
    public List<Piece> getBlanches() {
        return this.blanches;
    }

    // public void placerPiece(Piece piece, int ligne, int colonne) {
    // this.cases[ligne][colonne].setPiece(piece);
    // }

    /**
     * Place les pions sur leur position initiale selon les regles 10x10.
     */
    public void initPions() {
        for (int i = 0; i < NB_LIGNES; i++) {
            for (int j = 0; j < NB_COLONNES; j++) {
                if ((i <= 3) && (this.cases[i][j].getCouleur() == Couleur.NOIR)) {
                    Piece noire = new Pion(Couleur.NOIR, this.cases[i][j]);
                    this.cases[i][j].setPiece(noire);
                    this.noires.add(noire);
                } else if ((i >= 6) && (this.cases[i][j].getCouleur() == Couleur.NOIR)) {
                    Piece blanche = new Pion(Couleur.BLANC, this.cases[i][j]);
                    this.cases[i][j].setPiece(blanche);
                    this.blanches.add(blanche);
                }
            }
        }
    }

    /**
     * Rehydrate un etat de plateau a partir de listes de pieces.
     *
     * @param blanches pieces blanches a placer
     * @param noires pieces noires a placer
     */
    public void initPions(List<Piece> blanches, List<Piece> noires) {

        for (Piece piece : blanches) {
            Piece newPiece;
            Case c = this.cases[piece.getPosition().getLigne()][piece.getPosition().getColonne()];
            if (piece.getClass().equals(Pion.class)) {
                newPiece = new Pion(Couleur.BLANC, c);
            } else {
                newPiece = new Dame(Couleur.BLANC, c);
            }
            c.setPiece(newPiece);
            this.blanches.add(newPiece);
        }

        for (Piece piece : noires) {
            Piece newPiece;
            Case c = this.cases[piece.getPosition().getLigne()][piece.getPosition().getColonne()];
            if (piece.getClass().equals(Pion.class)) {
                newPiece = new Pion(Couleur.NOIR, c);
            } else {
                newPiece = new Dame(Couleur.NOIR, c);
            }
            c.setPiece(newPiece);
            this.noires.add(newPiece);
        }
    }

    /**
     * Deplace une piece vers une case destination.
     *
     * Preconditions: la piece et la case appartiennent a ce plateau.
     *
     * @param piece piece a deplacer
     * @param nouvellePosition case d'arrivee
     */

    public void deplacerPiece(Piece piece, Case nouvellePosition) {
        piece.getPosition().setPiece(null);
        piece.setPosition(nouvellePosition);
        nouvellePosition.setPiece(piece);
    }

    /**
     * Supprime une piece du plateau et de la collection de sa couleur.
     *
     * Precondition: la piece appartient a ce plateau.
     *
     * @param piece piece a retirer
     */

    public void supprimerPiece(Piece piece) {
        piece.getPosition().setPiece(null);
        if (piece.getCouleur() == Couleur.NOIR) {
            this.noires.remove(piece);
        } else {
            this.blanches.remove(piece);
        }
    }

    /**
     * Cree une copie profonde du plateau et de ses pieces.
     *
     * @return nouveau plateau independant
     */
    @Override
    public Plateau clone() {
        Plateau plateau = new Plateau();

        for (Piece p : this.getNoires()) {
            Piece piece = null;
            Case position = plateau.getCases()[p.getPosition().getLigne()][p.getPosition().getColonne()];
            if (p.getClass().equals(Pion.class)) {
                piece = new Pion(Couleur.NOIR, position);
            } else if (p.getClass().equals(Dame.class)) {
                piece = new Dame(Couleur.NOIR, position);
            }
            if (piece != null) {
                position.setPiece(piece);
                plateau.noires.add(piece);
            }
        }

        for (Piece p : this.getBlanches()) {
            Piece piece = null;
            Case position = plateau.cases[p.getPosition().getLigne()][p.getPosition().getColonne()];
            if (p.getClass().equals(Pion.class)) {
                piece = new Pion(Couleur.BLANC, position);
            } else if (p.getClass().equals(Dame.class)) {
                piece = new Dame(Couleur.BLANC, position);
            }
            if (piece != null) {
                position.setPiece(piece);
                plateau.blanches.add(piece);
            }
        }

        return plateau;
    }

    /**
     * Remplace un pion par une dame sur sa case actuelle.
     *
     * @param pion pion a promouvoir
     * @return dame creee et placee
     */
    public Dame promouvoirPion(Pion pion) {
        Dame dame = new Dame(pion.getCouleur(), pion.getPosition());
        dame.getPosition().setPiece(dame);
        if (pion.getCouleur() == Couleur.BLANC) {
            this.blanches.remove(pion);
            this.blanches.add(dame);
        } else {
            this.noires.remove(pion);
            this.noires.add(dame);
        }
        return dame;
    }

    /**
     * Reordonne les listes internes de pieces selon un ordre de reference.
     *
     * @param blanches ordre de reference des blanches
     * @param noires ordre de reference des noires
     */
    public void trierPieces(List<Piece> blanches, List<Piece> noires) {
        List<Piece> newBlanches = new ArrayList<>();
        List<Piece> newNoires = new ArrayList<>();
        for (Piece p1 : blanches) {
            for (Piece p2 : this.blanches) {
                if ((p2.getPosition().getLigne() == p1.getPosition().getLigne())
                        && (p2.getPosition().getColonne() == p1.getPosition().getColonne())) {
                    newBlanches.add(p2);
                    break;
                }
            }
        }

        for (Piece p1 : noires) {
            for (Piece p2 : this.noires) {
                if ((p2.getPosition().getLigne() == p1.getPosition().getLigne())
                        && (p2.getPosition().getColonne() == p1.getPosition().getColonne())) {
                    newNoires.add(p2);
                    break;
                }
            }
        }

        this.blanches = newBlanches;
        this.noires = newNoires;
    }
}
