package lacuna.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class ReplayGameDialog extends JDialog {
    private static final int REVEAL_ANIMATION_MS = 180;

    private boolean confirmed;
    private RoundedDialogPanel card;
    private Timer revealTimer;
    private long revealStartedAt;

    private ReplayGameDialog(JFrame owner) {
        super(owner, "Recommencer", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent());
        pack();
        setLocationRelativeTo(owner);
    }

    public static boolean confirm(JFrame owner) {
        ReplayGameDialog dialog = new ReplayGameDialog(owner);
        dialog.startRevealAnimation();
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    private JComponent createContent() {
        card = new RoundedDialogPanel();
        card.setLayout(new BorderLayout(0, 22));
        card.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = label("Recommencer la partie ?", 26, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel message = label("La partie en cours sera réinitialisée.", 16, new Color(215, 213, 222));
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(14));
        textPanel.add(message);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        JButton cancel = new DialogButton("Annuler", false);
        cancel.addActionListener((ActionEvent e) -> close(false));
        JButton replay = new DialogButton("Recommencer", true);
        replay.addActionListener((ActionEvent e) -> close(true));

        buttons.add(cancel);
        buttons.add(replay);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void close(boolean confirmed) {
        this.confirmed = confirmed;
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

    private static final class RoundedDialogPanel extends JPanel {
        private float revealProgress = 1f;

        RoundedDialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(470, 230));
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
            setPreferredSize(new Dimension(140, 44));
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
