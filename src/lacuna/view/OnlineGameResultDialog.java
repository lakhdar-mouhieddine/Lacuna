package lacuna.view;

import lacuna.model.FlowerColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.BooleanSupplier;

public final class OnlineGameResultDialog extends JDialog {
    private static final int REVEAL_ANIMATION_MS = 180;

    public enum Choice {
        REPLAY,
        WAIT_FOR_PLAYER,
        MAIN_MENU
    }

    private final BooleanSupplier rematchRequester;
    private Choice choice = Choice.MAIN_MENU;
    private RoundedDialogPanel card;
    private JLabel statusLabel;
    private DialogButton replayButton;
    private DialogButton waitButton;
    private Timer revealTimer;
    private long revealStartedAt;
    private boolean localRematchRequested;
    private boolean peerRematchRequested;
    private boolean peerLeft;

    public OnlineGameResultDialog(JFrame owner, String title, GameResultDialog.PlayerFlowers[] results,
            String winnerText, BooleanSupplier rematchRequester) {
        super(owner, title, true);
        this.rematchRequester = rematchRequester;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent(title, results, winnerText));
        pack();
        setLocationRelativeTo(owner);
    }

    public Choice showDialog() {
        startRevealAnimation();
        setVisible(true);
        return choice;
    }

    public void markPeerRematchRequested() {
        peerRematchRequested = true;
        if (localRematchRequested) {
            closeWith(Choice.REPLAY);
            return;
        }
        showStatus("L'autre joueur veut rejouer.");
    }

    public void markPeerLeft() {
        peerLeft = true;
        replayButton.setEnabled(false);
        replayButton.setText("Rejouer");
        waitButton.setVisible(true);
        showStatus("L'autre joueur a quitte la partie.");
        card.revalidate();
        card.repaint();
    }

    private JComponent createContent(String title, GameResultDialog.PlayerFlowers[] results, String winnerText) {
        card = new RoundedDialogPanel();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(BorderFactory.createEmptyBorder(26, 32, 28, 32));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = label(title, 26, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(16));

        for (GameResultDialog.PlayerFlowers result : results) {
            textPanel.add(playerFlowers(result));
            textPanel.add(Box.createVerticalStrut(12));
        }

        textPanel.add(Box.createVerticalStrut(4));
        JLabel winner = label(winnerText, 20, new Color(235, 203, 93));
        winner.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(winner);

        statusLabel = label(" ", 14, new Color(218, 218, 222));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(statusLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        replayButton = new DialogButton("Rejouer", true);
        replayButton.addActionListener((ActionEvent e) -> requestRematch());
        waitButton = new DialogButton("Attendre un joueur", false);
        waitButton.setVisible(false);
        waitButton.addActionListener((ActionEvent e) -> closeWith(Choice.WAIT_FOR_PLAYER));
        DialogButton menu = new DialogButton("Menu principal", false);
        menu.addActionListener((ActionEvent e) -> closeWith(Choice.MAIN_MENU));

        buttons.add(replayButton);
        buttons.add(waitButton);
        buttons.add(menu);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JComponent playerFlowers(GameResultDialog.PlayerFlowers result) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel player = label(result.playerName() + " remporte", 16, new Color(215, 213, 222));
        player.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(player);
        panel.add(Box.createVerticalStrut(6));

        JPanel flowers = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        flowers.setOpaque(false);
        if (result.colors().isEmpty()) {
            flowers.add(label("aucune fleur", 14, new Color(150, 148, 158)));
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

    private void requestRematch() {
        if (peerLeft) {
            return;
        }
        if (!localRematchRequested) {
            localRematchRequested = rematchRequester.getAsBoolean();
        }
        if (!localRematchRequested) {
            markPeerLeft();
            return;
        }
        if (peerRematchRequested) {
            closeWith(Choice.REPLAY);
            return;
        }
        replayButton.setText("En attente...");
        replayButton.setEnabled(false);
        showStatus("Demande de revanche envoyee.");
    }

    private void showStatus(String text) {
        statusLabel.setText(text);
        statusLabel.setForeground(new Color(218, 218, 222));
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

    private static final class RoundedDialogPanel extends JPanel {
        private float revealProgress = 1f;

        RoundedDialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(610, 390));
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
            setPreferredSize(new Dimension(primary ? 140 : 180, 44));
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        @Override
        public Color getForeground() {
            return isEnabled() ? Color.WHITE : new Color(170, 168, 178);
        }

        @Override
        public Cursor getCursor() {
            return isEnabled() ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor();
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
