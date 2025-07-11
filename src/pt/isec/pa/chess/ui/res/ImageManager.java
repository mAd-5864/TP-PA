package pt.isec.pa.chess.ui.res;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {
    private ImageManager() { }

    private static final Map<String, Image> images = new HashMap<>();
    private static final String PIECES_PATH = "/pt/isec/pa/chess/ui/res/images/pieces/";
    private static final String BACKGROUND_PATH = "/pt/isec/pa/chess/ui/res/images/background/";

    public static Image getImage(String filename) {
        // Usa o nome para detetar se é uma peça ou background
        String path;
        if (filename.startsWith("light_square") || filename.startsWith("dark_square")|| filename.startsWith("background")) {
            path = BACKGROUND_PATH + filename;
        } else {
            path = PIECES_PATH + filename;
        }

        Image image = images.get(path);

        if (image == null) {
            try (InputStream is = ImageManager.class.getResourceAsStream(path)) {
                if (is == null) {
                    System.err.println("Imagem não encontrada: " + path);
                    return null;
                }
                image = new Image(is);
                images.put(path, image);
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem: " + path);
                e.printStackTrace();
                return null;
            }
        }

        return image;
    }

    public static void clearCache() {
        images.clear();
    }
}
