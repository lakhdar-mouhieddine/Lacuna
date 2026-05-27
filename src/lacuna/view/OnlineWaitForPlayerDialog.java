package lacuna.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class OnlineWaitForPlayerDialog extends JDialog {
    private static final int REVEAL_ANIMATION_MS = 180;

    public enum Choice {
        JOINED,
        CANCEL
    }

    private Choice choice = Choice.CANCEL;
    private RoundedDialogPanel card;
    private Timer revealTimer;
    private long revealStartedAt;

    public OnlineWaitForPlayerDialog(JFrame owner, String sessionId) {
        super(owner, "En attente", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent(sessionId));
        pack();
        setLocationRelativeTo(owner);
    }

    public Choice showDialog() {
        startRevealAnimation();
        setVisible(true);
        return choice;
    }

    public void closeAsJoined() {
        closeWith(Choice.JOINED);
    }

    public void closeAsCancelled() {
        closeWith(Choice.CANCEL);
    }

    private JComponent createContent(String sessionId) {
        card = new RoundedDialogPanel();
        card.setLayout(new BorderLayout(0, 22));
        card.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = label("En attente d'un joueur...", 26, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel caption = label("Code de session", 14, new Color(184, 182, 194));
        caption.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField code = new JTextField(sessionId);
        code.setEditable(false);
        code.setFocusable(false);
        code.setHorizontalAlignment(JTextField.CENTER);
        code.setMaximumSize(new Dimension(180, 38));
        code.setPreferredSize(new Dimension(180, 38));
        code.setFont(new Font("Segoe UI", Font.BOLD, 18));
        code.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(16));
        textPanel.add(caption);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(code);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttons.setOpaque(false);

        JButton cancel = new DialogButton("Annuler");
        cancel.addActionListener((ActionEvent e) -> closeWith(Choice.CANCEL));
        buttons.add(cancel);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
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

    private static final class RoundedDialogPanel extends JPanel {
        private float revealProgress = 1f;

        RoundedDialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(450, 260));
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
        DialogButton(String text) {
            super(text);
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
            g2.setPaint(new GradientPaint(0, 0, new Color(61, 60, 70), getWidth(), 0, new Color(80, 79, 90)));
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
