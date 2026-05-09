package lacuna.view.menu;

import javax.swing.*;
import java.awt.*;

final class LabelPill extends JPanel {
    LabelPill(JLabel label) {
        setOpaque(false);
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        MenuAssets.prepare(g2);
        g2.setColor(new Color(65, 65, 75, 210));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
