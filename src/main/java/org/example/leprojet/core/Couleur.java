package org.example.leprojet.core;

/**
 * Couleur d'une case ou d'une pièce du jeu de dames.
 */
public enum Couleur {
    BLANC,
    NOIR;

    @Override
    public String toString() {
        return name();
    }
}

