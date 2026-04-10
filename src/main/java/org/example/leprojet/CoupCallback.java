package org.example.leprojet;

/**
 * Callback fonctionnel invoqué par le DamierView lorsqu'un joueur
 * tente un coup (clic de sélection + clic de destination).
 * <p>
 * En mode local  : le callback applique le coup via l'arbitre directement.
 * En mode réseau : le callback envoie le coup au serveur sans l'appliquer.
 */
@FunctionalInterface
public interface CoupCallback {

    /**
     * Appelé lorsque le joueur a cliqué un coup complet.
     *
     * @param lDep ligne de la pièce sélectionnée
     * @param cDep colonne de la pièce sélectionnée
     * @param lArr ligne de la case destination
     * @param cArr colonne de la case destination
     */
    void onCoup(int lDep, int cDep, int lArr, int cArr);
}
