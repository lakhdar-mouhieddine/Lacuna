package lacuna.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class StartingPlayerDialog extends JDialog {
    private int selectedPlayerIndex = -1;

    private StartingPlayerDialog(JFrame owner, String name1, String name2) {
        super(owner, "Qui commence ?", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent(name1, name2));
        pack();
        setLocationRelativeTo(owner);
    }

    public static int show(JFrame owner, String name1, String name2) {
        StartingPlayerDialog dialog = new StartingPlayerDialog(owner, name1, name2);
        dialog.setVisible(true);
        return dialog.selectedPlayerIndex;
    }

    private JComponent createContent(String name1, String name2) {
        JPanel card = new RoundedDialogPanel();
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = label("Qui commence ?", 24, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        
        JLabel subtitle = label("Choisissez le joueur qui débute", 13, new Color(178, 176, 184));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subtitle);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        JButton btnPlayer1 = new DialogButton(name1, false);
        btnPlayer1.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPlayer1.addActionListener((ActionEvent e) -> {
            selectedPlayerIndex = 0;
            dispose();
        });

        JButton btnRandom = new DialogButton("Aléatoire", true);
        btnRandom.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRandom.addActionListener((ActionEvent e) -> {
            selectedPlayerIndex = new java.util.Random().nextInt(2);
            dispose();
        });

        JButton btnPlayer2 = new DialogButton(name2, false);
        btnPlayer2.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPlayer2.addActionListener((ActionEvent e) -> {
            selectedPlayerIndex = 1;
            dispose();
        });

        buttons.add(btnPlayer1);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(btnRandom);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(btnPlayer2);

        card.add(header, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        return label;
    }

    private static final class RoundedDialogPanel extends JPanel {
        RoundedDialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(380, 290));
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

    private static final class DialogButton extends JButton {
        private final boolean primary;

        DialogButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setPreferredSize(new Dimension(316, 44));
            setMaximumSize(new Dimension(316, 44));
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
