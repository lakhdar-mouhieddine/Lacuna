package lacuna.view.menu;

import javax.swing.*;
import java.awt.*;

final class MenuTheme {
    static final Color BACKGROUND = new Color(24, 23, 29);
    static final Color FIELD_BACKGROUND = new Color(43, 42, 49);
    static final Color TEXT = new Color(245, 244, 248);
    static final Color MUTED_TEXT = new Color(178, 176, 184);
    static final Color FIELD_BORDER = new Color(68, 67, 76);
    static final Color SELECTION_PURPLE = new Color(111, 0, 214);

    private MenuTheme() {
    }

    static JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JTextField textField(String text) {
        JTextField field = new JTextField(text);
        size(field, 380, 46, 420, 46);
        field.setFont(new Font("Segoe UI", Font.BOLD, 16));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setSelectionColor(SELECTION_PURPLE);
        field.setSelectedTextColor(Color.WHITE);
        field.setBackground(FIELD_BACKGROUND);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    static void size(JComponent component, int preferredWidth, int preferredHeight, int maxWidth, int maxHeight) {
        component.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        component.setMaximumSize(new Dimension(maxWidth, maxHeight));
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
