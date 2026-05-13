package lacuna.view.menu;

import lacuna.network.LacunaServerClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.BiConsumer;

public class MainMenuPanel extends JPanel {
    private static final String MODE_NORMAL = "Normal";
    private static final String MODE_AI = "Contre IA";
    private static final String MODE_ONLINE = "En ligne";
    private static final String ONLINE_FRIEND = "Avec un ami";
    private static final String ONLINE_CREATE = "Creer";
    private static final String ONLINE_PUBLIC = "Partie publique";
    private static final Random RANDOM = new Random();

    private final BufferedImage orangeFlower = MenuAssets.load("main_men_top_orange.png");
    private final BufferedImage blueFlower = MenuAssets.load("main_men_blue.png");
    private final BufferedImage pinkFlower = MenuAssets.load("main_men_pink.png");

    private final BiConsumer<String, String> onLocalPlay;
    private final BiConsumer<String, String> onAiPlay;
    private final SegmentedChoice modeSelect;
    private final JPanel modeFields;
    private final PrimaryButton playButton;
    private final LacunaServerClient serverClient;

    private Timer serverStatusTimer;
    private boolean serverStatusCheckRunning;
    private StatusDot serverStatusDot;
    private JLabel serverStatusLabel;
    private JLabel offlineNoticeLabel;
    private SegmentedChoice onlineActionSelect;
    private JTextField friendSessionCodeField;
    private ToggleSwitch privateSessionSwitch;
    private JButton joinSessionButton;
    private JButton createSessionButton;
    private JButton joinOpenSessionButton;
    private JPanel onlineOptionsPanel;

    public MainMenuPanel(BiConsumer<String, String> onLocalPlay, BiConsumer<String, String> onAiPlay) {
        this.onLocalPlay = onLocalPlay;
        this.onAiPlay = onAiPlay;
        this.modeSelect = new SegmentedChoice(MODE_NORMAL, MODE_AI, MODE_ONLINE);
        this.modeFields = MenuTheme.verticalPanel();
        this.playButton = new PrimaryButton("Jouer Lacuna");
        this.serverClient = new LacunaServerClient();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1100, 680));
        setMinimumSize(new Dimension(920, 620));
        setBackground(MenuTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(40, 58, 22, 58));

        modeSelect.addActionListener((ActionEvent e) -> updateModeFields());

        add(createContent(), BorderLayout.CENTER);

        showNormalFields();
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints art = new GridBagConstraints();
        art.gridx = 0;
        art.gridy = 0;
        art.weightx = 0.52;
        art.weighty = 1;
        art.fill = GridBagConstraints.BOTH;
        content.add(new ProductArtPanel(), art);

        GridBagConstraints controls = new GridBagConstraints();
        controls.gridx = 1;
        controls.gridy = 0;
        controls.weightx = 0.48;
        controls.weighty = 1;
        controls.fill = GridBagConstraints.BOTH;
        controls.insets = new Insets(36, 48, 30, 44);
        content.add(createControls(), controls);

        return content;
    }

    private JComponent createControls() {
        JPanel controls = MenuTheme.verticalPanel();

        controls.add(Box.createVerticalGlue());
        controls.add(MenuTheme.label("Mode de jeu", 16, MenuTheme.MUTED_TEXT));
        controls.add(Box.createVerticalStrut(10));
        controls.add(modeSelect);
        controls.add(Box.createVerticalStrut(26));
        controls.add(modeFields);
        controls.add(Box.createVerticalStrut(28));
        controls.add(playButton);
        controls.add(Box.createVerticalGlue());

        return controls;
    }

    private void updateModeFields() {
        String mode = modeSelect.selectedValue();
        if (MODE_AI.equals(mode)) {
            showAiFields();
        } else if (MODE_ONLINE.equals(mode)) {
            showOnlineFields();
        } else {
            showNormalFields();
        }
    }

    private void showNormalFields() {
        stopServerStatusChecks();
        modeFields.removeAll();

        JTextField playerOne = MenuTheme.textField("Joueur 1");
        JTextField playerTwo = MenuTheme.textField("Joueur 2");
        addLabeledField("Nom du joueur 1", playerOne);
        addLabeledField("Nom du joueur 2", playerTwo);

        playButton.prepare("Jouer Lacuna", true, true);
        playButton.addActionListener((ActionEvent e) -> {
            String nom1 = valueOrDefault(playerOne.getText(), "Joueur 1");
            String nom2 = valueOrDefault(playerTwo.getText(), "Joueur 2");
            onLocalPlay.accept(nom1, nom2);
        });

        refreshFields();
    }

    private void showAiFields() {
        stopServerStatusChecks();
        modeFields.removeAll();

        JTextField playerName = MenuTheme.textField("Votre nom");
        SegmentedChoice levels = new SegmentedChoice("Facile", "Moyen", "Difficile");

        addLabeledField("Votre nom", playerName);
        addLabeledField("Niveau de l'IA", levels);
        modeFields.add(Box.createVerticalStrut(8));
        modeFields.add(MenuTheme.label("Moyen et Difficile arrivent bientot.", 12, MenuTheme.MUTED_TEXT));

        playButton.prepare("Jouer contre l'IA", true, true);
        playButton.addActionListener((ActionEvent e) -> {
            String nom = valueOrDefault(playerName.getText(), "Joueur");
            String niveau = levels.selectedValue();
            if ("Facile".equals(niveau)) {
                onAiPlay.accept(nom, niveau);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Seul le niveau Facile est disponible pour le moment.",
                    "Niveau indisponible", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        refreshFields();
    }

    private void showOnlineFields() {
        modeFields.removeAll();
        JTextField playerName = MenuTheme.textField(randomOnlinePlayerName());
        serverStatusDot = new StatusDot();
        serverStatusLabel = MenuTheme.label("", 14, MenuTheme.MUTED_TEXT);
        onlineActionSelect = new SegmentedChoice(ONLINE_FRIEND, ONLINE_CREATE, ONLINE_PUBLIC);
        onlineOptionsPanel = MenuTheme.verticalPanel();
        offlineNoticeLabel = MenuTheme.label("Impossible de jouer tant que le serveur est hors ligne.", 12, MenuTheme.MUTED_TEXT);

        onlineActionSelect.addActionListener((ActionEvent e) -> updateOnlineOptions());

        modeFields.add(createServerStatusRow());
        modeFields.add(Box.createVerticalStrut(10));
        addLabeledField("Votre nom", playerName);
        modeFields.add(Box.createVerticalStrut(18));
        modeFields.add(MenuTheme.label("Option", 14, MenuTheme.MUTED_TEXT));
        modeFields.add(Box.createVerticalStrut(8));
        modeFields.add(onlineActionSelect);
        modeFields.add(Box.createVerticalStrut(10));
        modeFields.add(onlineOptionsPanel);
        modeFields.add(Box.createVerticalStrut(8));
        modeFields.add(offlineNoticeLabel);

        playButton.prepare("", true, false);
        updateServerStatus(false);
        updateOnlineOptions();
        startServerStatusChecks();
        refreshFields();
    }

    private JPanel createServerStatusRow() {
        JPanel statusRow = new JPanel();
        statusRow.setOpaque(false);
        statusRow.setLayout(new BoxLayout(statusRow, BoxLayout.X_AXIS));
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusRow.setMaximumSize(new Dimension(420, 18));
        statusRow.add(serverStatusDot);
        statusRow.add(Box.createHorizontalStrut(10));
        statusRow.add(serverStatusLabel);
        statusRow.add(Box.createHorizontalGlue());
        return statusRow;
    }

    private void showJoinFriendOptions() {
        onlineOptionsPanel.removeAll();
        JTextField sessionCode = MenuTheme.textField("");
        friendSessionCodeField = sessionCode;
        joinSessionButton = new PrimaryButton("Rejoindre");

        onlineOptionsPanel.add(MenuTheme.label("ID de session", 14, MenuTheme.MUTED_TEXT));
        onlineOptionsPanel.add(Box.createVerticalStrut(8));
        onlineOptionsPanel.add(sessionCode);
        onlineOptionsPanel.add(Box.createVerticalStrut(12));
        onlineOptionsPanel.add(joinSessionButton);
        updateOnlineActionButtons();
        refreshFields();
    }

    private void showPlayOnlineOptions() {
        onlineOptionsPanel.removeAll();
        joinOpenSessionButton = new PrimaryButton("Rejoindre la partie");

        onlineOptionsPanel.add(MenuTheme.label("Sessions ouvertes", 14, MenuTheme.MUTED_TEXT));
        onlineOptionsPanel.add(Box.createVerticalStrut(8));
        onlineOptionsPanel.add(createOpenSessionsPreview());
        onlineOptionsPanel.add(Box.createVerticalStrut(12));
        onlineOptionsPanel.add(joinOpenSessionButton);
        updateOnlineActionButtons();
        refreshFields();
    }

    private void showCreateSessionOptions() {
        onlineOptionsPanel.removeAll();
        ToggleSwitch privateSession = new ToggleSwitch();
        privateSessionSwitch = privateSession;

        createSessionButton = new OrangeButton("Creer");

        onlineOptionsPanel.add(createPrivateSessionRow(privateSession));
        onlineOptionsPanel.add(Box.createVerticalStrut(12));
        onlineOptionsPanel.add(createSessionButton);
        updateOnlineActionButtons();
        refreshFields();
    }

    private JComponent createPrivateSessionRow(ToggleSwitch privateSession) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setMaximumSize(new Dimension(420, 48));
        row.add(privateSession);
        row.add(Box.createHorizontalStrut(16));
        row.add(MenuTheme.label("Session privee", 14, MenuTheme.MUTED_TEXT));
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private void updateOnlineOptions() {
        if (onlineActionSelect == null) {
            return;
        }

        String option = onlineActionSelect.selectedValue();

        if (ONLINE_CREATE.equals(option)) {
            showCreateSessionOptions();
        } else if (ONLINE_PUBLIC.equals(option)) {
            showPlayOnlineOptions();
        } else {
            showJoinFriendOptions();
        }
    }

    private JComponent createOpenSessionsPreview() {
        JPanel preview = MenuTheme.verticalPanel();
        preview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MenuTheme.FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        MenuTheme.size(preview, 380, 92, 420, 92);
        preview.add(MenuTheme.label("Aucune session chargee pour le moment.", 14, MenuTheme.MUTED_TEXT));
        preview.add(Box.createVerticalStrut(6));
        preview.add(MenuTheme.label("La vraie liste des sessions apparaitra ici.", 12, MenuTheme.MUTED_TEXT));
        return preview;
    }

    private void startServerStatusChecks() {
        stopServerStatusChecks();
        checkServerStatus();
        serverStatusTimer = new Timer(LacunaServerClient.STATUS_CHECK_MS, (ActionEvent e) -> checkServerStatus());
        serverStatusTimer.start();
    }

    private void stopServerStatusChecks() {
        if (serverStatusTimer != null) {
            serverStatusTimer.stop();
            serverStatusTimer = null;
        }
        serverStatusCheckRunning = false;
    }

    private void checkServerStatus() {
        if (serverStatusCheckRunning) {
            return;
        }

        serverStatusCheckRunning = true;
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return serverClient.ping();
            }

            @Override
            protected void done() {
                serverStatusCheckRunning = false;
                try {
                    updateServerStatus(get());
                } catch (Exception e) {
                    updateServerStatus(false);
                }
            }
        };
        worker.execute();
    }

    private void updateServerStatus(boolean online) {
        if (serverStatusDot != null) {
            serverStatusDot.setOnline(online);
        }

        if (serverStatusLabel != null) {
            serverStatusLabel.setText(online ? "Serveur : en ligne" : "Serveur : hors ligne");
            serverStatusLabel.setForeground(online ? new Color(104, 214, 132) : new Color(235, 86, 99));
        }

        if (offlineNoticeLabel != null) {
            offlineNoticeLabel.setVisible(!online);
        }

        updateOnlineActionButtons();
        refreshFields();
    }

    private void updateOnlineActionButtons() {
        boolean online = serverStatusDot != null && serverStatusDot.isOnline();
        if (onlineActionSelect != null) {
            onlineActionSelect.setEnabled(online);
        }
        if (friendSessionCodeField != null) {
            friendSessionCodeField.setEnabled(online);
        }
        if (privateSessionSwitch != null) {
            privateSessionSwitch.setEnabled(online);
        }
        if (joinSessionButton != null) {
            joinSessionButton.setEnabled(online);
        }
        if (createSessionButton != null) {
            createSessionButton.setEnabled(online);
        }
        if (joinOpenSessionButton != null) {
            joinOpenSessionButton.setEnabled(online);
        }
    }

    private void addLabeledField(String label, JComponent field) {
        if (modeFields.getComponentCount() > 0) {
            modeFields.add(Box.createVerticalStrut(14));
        }
        modeFields.add(MenuTheme.label(label, 14, MenuTheme.MUTED_TEXT));
        modeFields.add(Box.createVerticalStrut(8));
        modeFields.add(field);
    }

    private void refreshFields() {
        modeFields.revalidate();
        modeFields.repaint();
        revalidate();
        repaint();
    }

    private static String valueOrDefault(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static String randomOnlinePlayerName() {
        return String.format("Joueur%04d", RANDOM.nextInt(10000));
    }

    @Override
    public void removeNotify() {
        stopServerStatusChecks();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        MenuAssets.prepare(g2);

        g2.setColor(MenuTheme.BACKGROUND);
        g2.fillRect(0, 0, getWidth(), getHeight());

        MenuAssets.drawFit(g2, orangeFlower, getWidth() / 2 - 135, -86, 270, 245);
        MenuAssets.drawFit(g2, blueFlower, getWidth() - 270, 98, 168, 96);
        MenuAssets.drawFit(g2, pinkFlower, getWidth() - 190, getHeight() - 154, 165, 112);
        g2.dispose();
    }

}
