package lacuna.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class PlayerNamesDialog extends JDialog {
    private final JTextField playerOne = textField("Joueur 1");
    private final JTextField playerTwo = textField("Joueur 2");
    private String[] names;

    private PlayerNamesDialog(JFrame owner) {
        super(owner, "Nouvelle partie", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent());
        pack();
        setLocationRelativeTo(owner);
    }

    public static String[] show(JFrame owner) {
        PlayerNamesDialog dialog = new PlayerNamesDialog(owner);
        dialog.setVisible(true);
        return dialog.names;
    }

    private JComponent createContent() {
        JPanel card = new RoundedDialogPanel();
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createEmptyBorder(26, 32, 28, 32));

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        JLabel title = label("Nouvelle partie", 24, Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.add(title);
        fields.add(Box.createVerticalStrut(20));
        addField(fields, "Nom du joueur 1", playerOne);
        fields.add(Box.createVerticalStrut(14));
        addField(fields, "Nom du joueur 2", playerTwo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        JButton menu = new DialogButton("Menu principal", false);
        menu.addActionListener((ActionEvent e) -> dispose());
        JButton play = new DialogButton("Jouer", true);
        play.addActionListener((ActionEvent e) -> {
            names = new String[]{
                valueOrDefault(playerOne.getText(), "Joueur 1"),
                valueOrDefault(playerTwo.getText(), "Joueur 2")
            };
            dispose();
        });
        buttons.add(menu);
        buttons.add(play);

        card.add(fields, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private static void addField(JPanel parent, String title, JTextField field) {
        JLabel label = label(title, 14, new Color(178, 176, 184));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
        parent.add(Box.createVerticalStrut(8));
        parent.add(field);
    }

    private static JTextField textField(String text) {
        JTextField field = new JTextField(text);
        field.setPreferredSize(new Dimension(330, 44));
        field.setMaximumSize(new Dimension(360, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(new Font("Segoe UI", Font.BOLD, 16));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setSelectionColor(new Color(111, 0, 214));
        field.setSelectedTextColor(Color.WHITE);
        field.setBackground(new Color(43, 42, 49));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(68, 67, 76), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        return label;
    }

    private static String valueOrDefault(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static final class RoundedDialogPanel extends JPanel {
        RoundedDialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(430, 330));
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
            setPreferredSize(new Dimension(primary ? 115 : 160, 44));
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
