package lacuna.view;

import lacuna.model.FlowerColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public final class GameResultDialog extends JDialog {
    private static final int REVEAL_ANIMATION_MS = 180;

    public enum Choice {
        REPLAY,
        MAIN_MENU
    }

    public record PlayerFlowers(String playerName, List<FlowerColor> colors) {
    }

    private Choice choice = Choice.MAIN_MENU;
    private RoundedDialogPanel card;
    private Timer revealTimer;
    private long revealStartedAt;

    private int winnerIndex = -1;
    private List<FlowerColor> winnerColors;

    private GameResultDialog(JFrame owner, String title, PlayerFlowers[] results, String winnerText) {
        super(owner, title, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent(title, results, winnerText));
        pack();
        setLocationRelativeTo(owner);
    }

    public static Choice show(JFrame owner, String title, PlayerFlowers[] results, String winnerText) {
        GameResultDialog dialog = new GameResultDialog(owner, title, results, winnerText);
        dialog.startRevealAnimation();
        dialog.setVisible(true);
        return dialog.choice;
    }

    private JComponent createContent(String title, PlayerFlowers[] results, String winnerText) {
        card = new RoundedDialogPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 32, 14, 32));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        this.winnerIndex = -1;
        this.winnerColors = null;
        if (winnerText != null && winnerText.startsWith("Vainqueur : ")) {
            String winnerName = winnerText.substring("Vainqueur : ".length());
            for (int i = 0; i < results.length; i++) {
                if (results[i].playerName().equals(winnerName)) {
                    this.winnerIndex = i;
                    this.winnerColors = results[i].colors();
                    break;
                }
            }
        }

        if (winnerIndex != -1) {
            JLabel titleLabel = label(title, 26, Color.WHITE);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(14));

            JLabel winner = label(winnerText, 22, new Color(235, 203, 93));
            winner.setAlignmentX(Component.CENTER_ALIGNMENT);
            textPanel.add(winner);

            textPanel.add(Box.createVerticalStrut(158));

            int loserIndex = 1 - winnerIndex;
            textPanel.add(playerFlowers(results[loserIndex], loserIndex));
        } else {
            JLabel titleLabel = label(title, 26, Color.WHITE);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(16));

            for (int i = 0; i < results.length; i++) {
                textPanel.add(playerFlowers(results[i], i));
                textPanel.add(Box.createVerticalStrut(12));
            }

            textPanel.add(Box.createVerticalStrut(4));
            JLabel winner = label(winnerText, 20, new Color(235, 203, 93));
            winner.setAlignmentX(Component.CENTER_ALIGNMENT);
            textPanel.add(winner);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        JButton replay = new DialogButton("Rejouer", true);
        replay.addActionListener((ActionEvent e) -> closeWith(Choice.REPLAY));
        JButton menu = new DialogButton("Menu principal", false);
        menu.addActionListener((ActionEvent e) -> closeWith(Choice.MAIN_MENU));

        buttons.add(replay);
        buttons.add(menu);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JComponent playerFlowers(PlayerFlowers result, int playerIndex) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String textStr = result.playerName() + " remporte";
        JLabel player = label(textStr, 16, new Color(215, 213, 222));
        player.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(player);
        panel.add(Box.createVerticalStrut(6));

        JPanel flowers = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        flowers.setOpaque(false);
        if (result.colors().isEmpty()) {
            JLabel none = label("aucune fleur", 14, new Color(150, 148, 158));
            flowers.add(none);
        } else {
            for (FlowerColor color : result.colors()) {
                JLabel icon = new JLabel(GameAssets.icon(GameAssets.flower(color), 30));
                icon.setToolTipText(flowerName(color));
                flowers.add(icon);
            }
        }
        panel.add(flowers);
        return panel;
    }

    private void closeWith(Choice choice) {
        this.choice = choice;
        stopRevealAnimation();
        dispose();
    }

    private void startRevealAnimation() {
        card.setRevealProgress(0f);
        revealStartedAt = System.currentTimeMillis();
        revealTimer = new Timer(16, e -> tickRevealAnimation());
        revealTimer.setCoalesce(true);
        revealTimer.start();
    }

    private void tickRevealAnimation() {
        float progress = Math.max(0f, Math.min(1f,
            (System.currentTimeMillis() - revealStartedAt) / (float) REVEAL_ANIMATION_MS
        ));
        card.setRevealProgress(progress);
        if (progress >= 1f) {
            stopRevealAnimation();
        }
    }

    private void stopRevealAnimation() {
        if (revealTimer != null) {
            revealTimer.stop();
            revealTimer = null;
        }
        if (card != null) {
            card.setRevealProgress(1f);
        }
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        return label;
    }

    private static String flowerName(FlowerColor color) {
        return switch (color) {
            case BLUE -> "Bleu";
            case GREEN -> "Vert";
            case ORANGE -> "Orange";
            case RED -> "Rouge";
            case PINK -> "Rose";
            case PURPLE -> "Violet";
            case CYAN -> "Cyan";
        };
    }

    private final class RoundedDialogPanel extends JPanel {
        private float revealProgress = 1f;

        RoundedDialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(540, 420));
        }

        private void setRevealProgress(float revealProgress) {
            this.revealProgress = revealProgress;
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            float eased = smooth(revealProgress);
            float alpha = Math.max(0f, Math.min(1f, eased));
            double scale = 0.94 + 0.06 * eased;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.translate(centerX, centerY);
            g2.scale(scale, scale);
            g2.translate(-centerX, -centerY);
            super.paint(g2);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GameAssets.prepare(g2);
            g2.setColor(new Color(24, 23, 29, 244));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            g2.setColor(new Color(255, 255, 255, 28));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 26, 26);

            if (winnerIndex != -1 && winnerColors != null) {
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 - 30;

                int pawnSize = 60;
                GameAssets.drawFit(g2, GameAssets.pawn(winnerIndex), cx, cy - 2, pawnSize);

                int N = winnerColors.size();
                int flowerSize = 30;
                double radius = 50.0;

                for (int j = 0; j < N; j++) {
                    FlowerColor color = winnerColors.get(j);
                    double angle = j * 2.0 * Math.PI / (N == 0 ? 1 : N);
                    int tx = cx + (int) (radius * Math.cos(angle));
                    int ty = cy + (int) (radius * Math.sin(angle)) - 2;

                    GameAssets.drawFit(g2, GameAssets.flower(color), tx, ty, flowerSize);
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static float smooth(float value) {
        return value * value * (3f - 2f * value);
    }

    private static final class DialogButton extends JButton {
        private final boolean primary;

        DialogButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setPreferredSize(new Dimension(primary ? 140 : 170, 44));
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GameAssets.prepare(g2);
            Color start = primary ? new Color(102, 0, 202) : new Color(61, 60, 70);
            Color end = primary ? new Color(128, 0, 214) : new Color(80, 79, 90);
            if (!isEnabled()) {
                start = end = new Color(78, 76, 86);
            }
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), 0, end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
