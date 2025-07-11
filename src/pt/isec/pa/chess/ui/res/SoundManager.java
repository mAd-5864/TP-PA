package pt.isec.pa.chess.ui.res;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import pt.isec.pa.chess.model.ModelLog;
import pt.isec.pa.chess.model.data.pieces.PieceColor;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {
    private static MediaPlayer mp;
    private static boolean soundEnabled = true;

    private SoundManager() {
    }


    public static void playMoveAnnouncement(String piece, PieceColor color, String from, String to, boolean isCapture, boolean isCheck, boolean isCheckmate, boolean isStalemate) {
        if (!soundEnabled) return;

        List<String> sounds = new ArrayList<>();
        sounds.add(pieceName(piece) + ".mp3");

        // posição de origem
        if (from != null && from.length() == 2) {
            sounds.add(String.valueOf(from.charAt(0)) + ".mp3");
            sounds.add(String.valueOf(from.charAt(1)) + ".mp3");
        } else {
            sounds.add("empty.mp3");
        }

        sounds.add("to.mp3");

        // posição de destino
        if (to != null && to.length() == 2) {
            sounds.add(String.valueOf(to.charAt(0)) + ".mp3");
            sounds.add(String.valueOf(to.charAt(1)) + ".mp3");
        } else {
            sounds.add("empty.mp3");
        }

        if (isCapture)
            sounds.add("capture.mp3");

        if(isStalemate){
            sounds.add("stalemate.mp3");
        }

        if (isCheck) {
            if (isCheckmate) {
                sounds.add("checkmate.mp3");
            } else {
                sounds.add("check.mp3");
            }
        }

        playSequence(sounds, 0);
    }

    private static String pieceName(String pieceWithPosition) {
        // Extrai só a primeira letra
        char pieceAbbreviation = pieceWithPosition.toUpperCase().charAt(0);
        return switch (pieceAbbreviation) {
            case 'P' -> "pawn";
            case 'N' -> "knight";
            case 'B' -> "bishop";
            case 'R' -> "rook";
            case 'Q' -> "queen";
            case 'K' -> "king";
            default -> "unknown";
        };
    }

    private static void playSequence(List<String> sounds, int index) {
        if (index >= sounds.size())
            return;

        String filename = sounds.get(index);
        var url = SoundManager.class.getResource("sounds/pt/" + filename);

        if (url == null) {
            Platform.runLater(() -> playSequence(sounds, index + 1));
            return;
        }

        Platform.runLater(() -> {
            try {
                Media media = new Media(url.toExternalForm());
                stop();
                mp = new MediaPlayer(media);

                mp.setOnEndOfMedia(() -> Platform.runLater(() -> playSequence(sounds, index + 1)));
                mp.setOnError(() -> {
                    ModelLog.getInstance().addLog("Erro ao reproduzir: " + filename);
                    Platform.runLater(() -> playSequence(sounds, index + 1));
                });

                mp.play();
            } catch (Exception e) {
                ModelLog.getInstance().addLog("Erro ao carregar media: " + e.getMessage());
                Platform.runLater(() -> playSequence(sounds, index + 1));
            }
        });
    }

    public static boolean isPlaying() {
        return mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public static void stop() {
        if (isPlaying()) {
            mp.stop();
        }
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static boolean toggleSound() {
        soundEnabled = !soundEnabled;
        return soundEnabled;
    }
}
