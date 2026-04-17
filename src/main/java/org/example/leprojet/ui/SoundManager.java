package org.example.leprojet.ui;

import javafx.scene.media.AudioClip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Map;
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

    private enum SoundId {
        MOVE,
        CAPTURE,
        ENDGAME
    }

    private static boolean enabled = true;
    private static final float SAMPLE_RATE = 44100f;
    private static final double MASTER_GAIN = 0.72;
    private static final Random RNG = new Random();
    private static final Map<SoundId, AudioClip> CLIPS = new EnumMap<>(SoundId.class);
    private static final Map<SoundId, Path> TEMP_FILES = new EnumMap<>(SoundId.class);
    private static volatile boolean initialized;

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
        play(SoundId.MOVE);
    }

    /**
     * Impact sourd + crack — pièce capturée (style chess.com capture).
     */
    public static void playCapture() {
        play(SoundId.CAPTURE);
    }

    /**
     * Son ascendant brillant — promotion en dame.
     */
    public static void playPromotion() {
        play(SoundId.MOVE);
    }

    /**
     * Fanfare courte — victoire.
     */
    public static void playVictory() {
        play(SoundId.ENDGAME);
    }

    /** Son de fin de partie, appelé à l'arrêt d'une session réseau/local. */
    public static void playEndgame() {
        play(SoundId.ENDGAME);
    }

    // ── Génération : Move ("toc" bois) ─────────────────────────────────

    private static byte[] genererSonMove() {
        int durée = ms(75);
        byte[] buf = new byte[durée];

        for (int i = 0; i < durée; i++) {
            double t = i / SAMPLE_RATE;
            // "Toc" : bruit filtré + tonale basse, decay exponentiel rapide
            double decay = Math.exp(-t * 78);
            double tonale = Math.sin(2 * Math.PI * 980 * t) * 0.34;
            double bruit = (RNG.nextDouble() * 2 - 1) * 0.48;
            // Filtre passe-bas simplifié sur le bruit
            double signal = (tonale + bruit * decay * 0.5) * decay;
            buf[i] = clamp(signal * 88);
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
            double decayImpact = Math.exp(-t * 45);
            double thud = Math.sin(2 * Math.PI * 190 * t) * decayImpact * 0.55;

            // Phase 2 : crack bois aigü (0-20ms)
            double decayCrack = Math.exp(-t * 140);
            double crack = (RNG.nextDouble() * 2 - 1) * decayCrack * 0.35;

            // Phase 3 : tonale médium
            double decayTone = Math.exp(-t * 58);
            double tone = Math.sin(2 * Math.PI * 740 * t) * decayTone * 0.24;

            double signal = thud + crack + tone;
            buf[i] = clamp(signal * 92);
        }
        return buf;
    }

    // ── Génération : Promotion (arpège ascendant) ──────────────────────

    private static byte[] genererSonVictory() {
        int durée = ms(420);
        byte[] buf = new byte[durée];

        // Son sec de "fin" : attaque courte + résonance discrète.
        double hitFreq = 640;
        double bodyFreq = 280;

        for (int i = 0; i < durée; i++) {
            double t = i / SAMPLE_RATE;
            double hitEnv = Math.exp(-t * 42);
            double bodyEnv = Math.exp(-t * 9);
            double hit = Math.sin(2 * Math.PI * hitFreq * t) * hitEnv * 0.65;
            double body = Math.sin(2 * Math.PI * bodyFreq * t) * bodyEnv * 0.35;
            double noise = (RNG.nextDouble() * 2 - 1) * Math.exp(-t * 80) * 0.18;
            buf[i] = clamp((hit + body + noise) * 86);
        }
        return buf;
    }

    private static void play(SoundId soundId) {
        if (!enabled) return;
        ensureInitialized();
        AudioClip clip = CLIPS.get(soundId);
        if (clip == null) return;
        clip.stop();
        clip.play(MASTER_GAIN);
    }

    private static synchronized void ensureInitialized() {
        if (initialized) return;
        try {
            chargerClip(SoundId.MOVE, genererSonMove());
            chargerClip(SoundId.CAPTURE, genererSonCapture());
            chargerClip(SoundId.ENDGAME, genererSonVictory());
            initialized = true;
        } catch (Exception e) {
            // Son non critique
        }
    }

    private static void chargerClip(SoundId id, byte[] samples) throws IOException {
        byte[] wav = buildWav(samples, (int) SAMPLE_RATE);
        Path tempWav = Files.createTempFile("leprojet-sound-" + id.name().toLowerCase() + "-", ".wav");
        Files.write(tempWav, wav, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        tempWav.toFile().deleteOnExit();

        AudioClip clip = new AudioClip(tempWav.toUri().toString());
        clip.setCycleCount(1);
        CLIPS.put(id, clip);
        TEMP_FILES.put(id, tempWav);
    }

    public static synchronized void shutdown() {
        for (AudioClip clip : CLIPS.values()) {
            clip.stop();
        }
        CLIPS.clear();

        for (Path p : TEMP_FILES.values()) {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        }
        TEMP_FILES.clear();
        initialized = false;
    }

    private static byte[] buildWav(byte[] pcm8bit, int sampleRate) {
        int dataSize = pcm8bit.length;
        int fileSizeMinus8 = 36 + dataSize;

        byte[] wav = new byte[44 + dataSize];
        int idx = 0;
        idx = putAscii(wav, idx, "RIFF");
        idx = putLeInt(wav, idx, fileSizeMinus8);
        idx = putAscii(wav, idx, "WAVE");
        idx = putAscii(wav, idx, "fmt ");
        idx = putLeInt(wav, idx, 16);
        idx = putLeShort(wav, idx, 1);
        idx = putLeShort(wav, idx, 1);
        idx = putLeInt(wav, idx, sampleRate);
        idx = putLeInt(wav, idx, sampleRate);
        idx = putLeShort(wav, idx, 1);
        idx = putLeShort(wav, idx, 8);
        idx = putAscii(wav, idx, "data");
        idx = putLeInt(wav, idx, dataSize);

        // WAV PCM 8-bit doit être non signé : conversion [-128..127] -> [0..255].
        for (int i = 0; i < dataSize; i++) {
            int signed = pcm8bit[i];
            wav[idx + i] = (byte) (signed + 128);
        }
        return wav;
    }

    private static int putAscii(byte[] out, int offset, String s) {
        for (int i = 0; i < s.length(); i++) {
            out[offset + i] = (byte) s.charAt(i);
        }
        return offset + s.length();
    }

    private static int putLeInt(byte[] out, int offset, int value) {
        out[offset] = (byte) (value & 0xff);
        out[offset + 1] = (byte) ((value >>> 8) & 0xff);
        out[offset + 2] = (byte) ((value >>> 16) & 0xff);
        out[offset + 3] = (byte) ((value >>> 24) & 0xff);
        return offset + 4;
    }

    private static int putLeShort(byte[] out, int offset, int value) {
        out[offset] = (byte) (value & 0xff);
        out[offset + 1] = (byte) ((value >>> 8) & 0xff);
        return offset + 2;
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

