package org.example.leprojet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayInputStream;
import java.util.Random;

/**
 * Gestionnaire d'effets sonores pour le jeu de dames.
 * <p>
 * Génère des sons synthétiques imitant le style chess.com :
 * <ul>
 *   <li><b>Move</b> : claquement sec de pièce posée sur bois ("toc")</li>
 *   <li><b>Capture</b> : impact sourd + crack ("TOCK")</li>
 *   <li><b>Promotion</b> : son ascendant brillant</li>
 *   <li><b>Victory</b> : fanfare courte</li>
 * </ul>
 */
public class SoundManager {

    private static boolean enabled = true;
    private static final float SAMPLE_RATE = 44100f;
    private static final Random RNG = new Random();

    private SoundManager() {
    }

    public static void setEnabled(boolean enabled) {
        SoundManager.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // ── Sons publics ───────────────────────────────────────────────────

    /**
     * "Toc" court et sec — pièce posée sur le damier (style chess.com move).
     */
    public static void playMove() {
        playAsync(() -> jouerSon(genererSonMove()));
    }

    /**
     * Impact sourd + crack — pièce capturée (style chess.com capture).
     */
    public static void playCapture() {
        playAsync(() -> jouerSon(genererSonCapture()));
    }

    /**
     * Son ascendant brillant — promotion en dame.
     */
    public static void playPromotion() {
        playAsync(() -> jouerSon(genererSonPromotion()));
    }

    /**
     * Fanfare courte — victoire.
     */
    public static void playVictory() {
        playAsync(() -> jouerSon(genererSonVictory()));
    }

    // ── Génération : Move ("toc" bois) ─────────────────────────────────

    private static byte[] genererSonMove() {
        int durée = ms(75);
        byte[] buf = new byte[durée];

        for (int i = 0; i < durée; i++) {
            double t = i / SAMPLE_RATE;
            // "Toc" : bruit filtré + tonale basse, decay exponentiel rapide
            double decay = Math.exp(-t * 80);
            double tonale = Math.sin(2 * Math.PI * 1200 * t) * 0.4;
            double bruit = (RNG.nextDouble() * 2 - 1) * 0.6;
            // Filtre passe-bas simplifié sur le bruit
            double signal = (tonale + bruit * decay * 0.5) * decay;
            buf[i] = clamp(signal * 110);
        }
        return buf;
    }

    // ── Génération : Capture ("TOCK" grave + crack) ────────────────────

    private static byte[] genererSonCapture() {
        int durée = ms(130);
        byte[] buf = new byte[durée];

        for (int i = 0; i < durée; i++) {
            double t = i / SAMPLE_RATE;

            // Phase 1 : impact grave "THUD" (0-40ms)
            double decayImpact = Math.exp(-t * 50);
            double thud = Math.sin(2 * Math.PI * 200 * t) * decayImpact * 0.6;

            // Phase 2 : crack bois aigü (0-20ms)
            double decayCrack = Math.exp(-t * 150);
            double crack = (RNG.nextDouble() * 2 - 1) * decayCrack * 0.5;

            // Phase 3 : tonale médium
            double decayTone = Math.exp(-t * 60);
            double tone = Math.sin(2 * Math.PI * 800 * t) * decayTone * 0.3;

            double signal = thud + crack + tone;
            buf[i] = clamp(signal * 120);
        }
        return buf;
    }

    // ── Génération : Promotion (arpège ascendant) ──────────────────────

    private static byte[] genererSonPromotion() {
        int durée = ms(280);
        byte[] buf = new byte[durée];

        double[] notes = {523.25, 659.25, 783.99, 1046.50}; // C5, E5, G5, C6
        int noteDurée = durée / notes.length;

        for (int i = 0; i < durée; i++) {
            int noteIdx = Math.min(i / noteDurée, notes.length - 1);
            double freq = notes[noteIdx];
            double t = i / SAMPLE_RATE;
            double localT = (i - noteIdx * noteDurée) / SAMPLE_RATE;
            double envelope = Math.exp(-localT * 12) * Math.min(localT * 500, 1.0);

            double signal = Math.sin(2 * Math.PI * freq * t) * envelope;
            // Ajouter harmonique douce
            signal += Math.sin(2 * Math.PI * freq * 2 * t) * envelope * 0.2;
            buf[i] = clamp(signal * 80);
        }
        return buf;
    }

    // ── Génération : Victory (fanfare) ─────────────────────────────────

    private static byte[] genererSonVictory() {
        int durée = ms(500);
        byte[] buf = new byte[durée];

        // Trois accords joyeux successifs
        double[][] accords = {
                {523.25, 659.25, 783.99},  // C maj
                {587.33, 739.99, 880.00},  // D maj
                {659.25, 830.61, 987.77},  // E maj
        };
        int accordDurée = durée / accords.length;

        for (int i = 0; i < durée; i++) {
            int accIdx = Math.min(i / accordDurée, accords.length - 1);
            double localT = (i - accIdx * accordDurée) / SAMPLE_RATE;
            double envelope = Math.exp(-localT * 5) * Math.min(localT * 300, 1.0);

            double signal = 0;
            for (double freq : accords[accIdx]) {
                double t = i / SAMPLE_RATE;
                signal += Math.sin(2 * Math.PI * freq * t) * envelope;
            }
            signal /= accords[accIdx].length;
            buf[i] = clamp(signal * 80);
        }
        return buf;
    }

    // ── Lecture audio ──────────────────────────────────────────────────

    private static void jouerSon(byte[] samples) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(samples), format, samples.length);

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            // Son non critique
        }
    }

    private static void playAsync(Runnable task) {
        if (!enabled) return;
        Thread t = new Thread(task, "sound");
        t.setDaemon(true);
        t.start();
    }

    // ── Utilitaires ────────────────────────────────────────────────────

    /** Convertit des millisecondes en nombre d'échantillons. */
    private static int ms(int milliseconds) {
        return (int) (SAMPLE_RATE * milliseconds / 1000);
    }

    /** Clamp un double en byte signé [-128, 127]. */
    private static byte clamp(double value) {
        return (byte) Math.max(-127, Math.min(127, (int) value));
    }
}
