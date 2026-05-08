package lacuna.view.menu;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class MenuAssets {
    private MenuAssets() {
    }

    static BufferedImage load(String name) {
        List<Path> candidates = List.of(
            Path.of("assets", name),
            Path.of("lacuna", "assets", name),
            Path.of("..", "lacuna", "assets", name)
        );

        for (Path path : candidates) {
            if (Files.exists(path)) {
                try {
                    return ImageIO.read(path.toFile());
                } catch (IOException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    static void prepare(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    static void drawFit(Graphics2D g2, BufferedImage image, int x, int y, int width, int height) {
        if (image == null || width <= 0 || height <= 0) {
            return;
        }

        double scale = Math.min(width / (double) image.getWidth(), height / (double) image.getHeight());
        int drawW = (int) Math.round(image.getWidth() * scale);
        int drawH = (int) Math.round(image.getHeight() * scale);
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH) / 2;
        g2.drawImage(image, drawX, drawY, drawW, drawH, null);
    }
}
