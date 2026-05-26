package lacuna.view.menu;

import lacuna.network.LacunaServerClient;
import lacuna.network.LacunaServerConfig;
import lacuna.network.ServerEndpoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.CancellationException;

final class ServerSettingsDialog extends JDialog {
    private JTextField hostField;
    private JTextField portField;
    private JLabel currentServerLabel;
    private JLabel feedbackLabel;
    private JButton testButton;
    private JButton useButton;
    private JButton officialButton;
    private JButton cancelButton;
    private SwingWorker<Boolean, Void> testWorker;
    private ServerEndpoint pendingEndpoint;
    private boolean changed;

    private ServerSettingsDialog(Window owner) {
        super(owner, "Serveur en ligne", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createContent());
        pack();
        setLocationRelativeTo(owner);
    }

    static boolean showDialog(Component parent) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        ServerSettingsDialog dialog = new ServerSettingsDialog(owner);
        dialog.setVisible(true);
        return dialog.changed;
    }

    private JComponent createContent() {
        JPanel card = new RoundedSettingsPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setPreferredSize(new Dimension(480, 385));

        JLabel title = MenuTheme.label("Serveur en ligne", 24, MenuTheme.TEXT);
        currentServerLabel = MenuTheme.label("", 13, MenuTheme.MUTED_TEXT);
        updateCurrentServerLabel();

        ServerEndpoint current = LacunaServerConfig.currentEndpoint();
        hostField = MenuTheme.textField(current.getHost());
        portField = MenuTheme.textField(String.valueOf(current.getPort()));
        feedbackLabel = MenuTheme.label("", 12, MenuTheme.MUTED_TEXT);
        feedbackLabel.setVisible(false);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(currentServerLabel);
        card.add(Box.createVerticalStrut(18));
        addLabeledField(card, "Adresse", hostField);
        card.add(Box.createVerticalStrut(14));
        addLabeledField(card, "Port", portField);
        card.add(Box.createVerticalStrut(12));
        card.add(feedbackLabel);
        card.add(Box.createVerticalGlue());
        card.add(createButtons());

        return card;
    }

    private void addLabeledField(JPanel card, String labelText, JComponent field) {
        card.add(MenuTheme.label(labelText, 14, MenuTheme.MUTED_TEXT));
        card.add(Box.createVerticalStrut(7));
        card.add(field);
    }

    private JComponent createButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(420, 42));

        officialButton = new DialogButton("Serveur officiel", ButtonStyle.SECONDARY, 132);
        officialButton.addActionListener((ActionEvent e) -> useOfficialServer());

        testButton = new DialogButton("Tester", ButtonStyle.SECONDARY, 82);
        testButton.addActionListener((ActionEvent e) -> testCustomServer(false));

        useButton = new DialogButton("Utiliser", ButtonStyle.PRIMARY, 88);
        useButton.addActionListener((ActionEvent e) -> testCustomServer(true));

        cancelButton = new DialogButton("Annuler", ButtonStyle.SECONDARY, 82);
        cancelButton.addActionListener((ActionEvent e) -> close());

        buttons.add(officialButton);
        buttons.add(testButton);
        buttons.add(useButton);
        buttons.add(cancelButton);
        return buttons;
    }

    private void testCustomServer(boolean applyOnSuccess) {
        if (testWorker != null) {
            return;
        }

        try {
            pendingEndpoint = ServerEndpoint.fromText(hostField.getText(), portField.getText());
        } catch (IllegalArgumentException e) {
            fallbackToOfficial("Adresse ou port invalide.");
            return;
        }

        setTesting(true);
        setFeedback("Test de connexion...", MenuTheme.MUTED_TEXT);
        testWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return new LacunaServerClient(pendingEndpoint).ping();
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        return;
                    }
                    if (get()) {
                        handleTestSuccess(applyOnSuccess);
                    } else {
                        fallbackToOfficial("Serveur inaccessible.");
                    }
                } catch (CancellationException ignored) {
                } catch (Exception e) {
                    fallbackToOfficial("Serveur inaccessible.");
                } finally {
                    testWorker = null;
                    setTesting(false);
                }
            }
        };
        testWorker.execute();
    }

    private void handleTestSuccess(boolean applyOnSuccess) {
        if (!applyOnSuccess) {
            setFeedback("Serveur joignable. Cliquez sur Utiliser.", new Color(104, 214, 132));
            return;
        }

        LacunaServerConfig.useCustom(pendingEndpoint);
        changed = true;
        close();
    }

    private void useOfficialServer() {
        if (!LacunaServerConfig.isOfficial()) {
            changed = true;
        }
        LacunaServerConfig.useOfficial();
        close();
    }

    private void fallbackToOfficial(String reason) {
        if (!LacunaServerConfig.isOfficial()) {
            changed = true;
        }
        LacunaServerConfig.useOfficial();
        ServerEndpoint official = LacunaServerConfig.currentEndpoint();
        hostField.setText(official.getHost());
        portField.setText(String.valueOf(official.getPort()));
        updateCurrentServerLabel();
        setFeedback(reason + " Retour au serveur officiel.", new Color(235, 86, 99));
    }

    private void updateCurrentServerLabel() {
        if (currentServerLabel == null) {
            return;
        }

        ServerEndpoint current = LacunaServerConfig.currentEndpoint();
        if (LacunaServerConfig.isOfficial()) {
            currentServerLabel.setText("Serveur actuel : officiel");
        } else {
            currentServerLabel.setText("Serveur actuel : " + current.displayName());
        }
    }

    private void setFeedback(String message, Color color) {
        feedbackLabel.setText(message);
        feedbackLabel.setForeground(color);
        feedbackLabel.setVisible(!message.isEmpty());
    }

    private void setTesting(boolean testing) {
        hostField.setEnabled(!testing);
        portField.setEnabled(!testing);
        testButton.setEnabled(!testing);
        useButton.setEnabled(!testing);
        officialButton.setEnabled(!testing);
    }

    private void close() {
        if (testWorker != null) {
            testWorker.cancel(true);
            testWorker = null;
        }
        dispose();
    }

    private enum ButtonStyle {
        PRIMARY,
        SECONDARY
    }

    private static final class DialogButton extends JButton {
        private final ButtonStyle style;

        DialogButton(String text, ButtonStyle style, int width) {
            super(text);
            this.style = style;
            setPreferredSize(new Dimension(width, 38));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            MenuAssets.prepare(g2);

            Color start;
            Color end;
            if (!isEnabled()) {
                start = new Color(78, 76, 86);
                end = new Color(78, 76, 86);
            } else if (style == ButtonStyle.PRIMARY) {
                start = new Color(102, 0, 202);
                end = new Color(128, 0, 214);
            } else {
                start = new Color(61, 60, 70);
                end = new Color(80, 79, 90);
            }

            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), 0, end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            if (getModel().isRollover() && isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class RoundedSettingsPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            MenuAssets.prepare(g2);
            g2.setColor(new Color(24, 23, 29, 244));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g2.setColor(new Color(255, 255, 255, 28));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
