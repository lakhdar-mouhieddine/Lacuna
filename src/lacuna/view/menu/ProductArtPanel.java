package lacuna.view.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

final class ProductArtPanel extends JPanel {
    private final BufferedImage gameBox = MenuAssets.load("main_men_game_box.png");

    ProductArtPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(440, 440));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        MenuAssets.prepare(g2);

        int size = Math.min(getWidth() - 20, getHeight() - 12);
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2 + 8;

        g2.setColor(new Color(0, 0, 0, 95));
        MenuAssets.drawFit(g2, gameBox, x, y, size, size);
        g2.dispose();
    }
}
