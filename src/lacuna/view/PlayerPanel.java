package lacuna.view;

import lacuna.model.FlowerColor;
import lacuna.model.GameModel;
import lacuna.model.ModelListener;
import lacuna.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerPanel extends JPanel implements ModelListener {
    private static final int PANEL_WIDTH = 130;
    private static final int ICON_SIZE = 22;
    private static final int SCORE_ITEM_WIDTH = 100;
    private static final int SCORE_ITEM_HEIGHT = 34;
    private static final Color WIN_AURA = new Color(74, 218, 126);
    private static final Color LOSE_AURA = new Color(239, 80, 88);
    private static final Color TIE_AURA = new Color(174, 174, 184);

    private final GameModel model;
    private final Player player;
    private final JLabel nameLabel;
    private final JLabel pawnLabel;
    private final JPanel scoreTab;
    private final java.util.Map<FlowerColor, Integer> previousScores = new java.util.EnumMap<>(FlowerColor.class);
    private final java.util.Map<FlowerColor, Integer> bonusValues = new java.util.EnumMap<>(FlowerColor.class);
    private Timer bonusTimer;
    private long bonusStartedAt;
    private Timer auraTimer;
    private long auraStartedAt;
    private float auraPulse;

    public PlayerPanel(GameModel model, Player player, boolean isLeft) {
        this.model = model;
        this.player = player;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(PANEL_WIDTH, 100));
        setBorder(BorderFactory.createEmptyBorder(0, isLeft ? 4 : 0, 0, isLeft ? 0 : 4));

        nameLabel = createPlainLabel();
        pawnLabel = createPlainLabel();
        scoreTab = new RoundedTabPanel();
        scoreTab.setLayout(new GridLayout(0, 1, 0, 6));
        scoreTab.setOpaque(false);

        JPanel playerTab = new RoundedTabPanel();
        playerTab.setLayout(new GridLayout(0, 1, 0, 4));
        playerTab.setOpaque(false);
        playerTab.add(nameLabel);
        playerTab.add(pawnLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(playerTab, gbc);

        gbc.gridy = 1;
        add(scoreTab, gbc);

        model.addModelListener(this);
        rafraichir();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startAuraTimer();
    }

    @Override
    public void removeNotify() {
        stopAuraTimer();
        super.removeNotify();
    }

    @Override
    public void onModelUpdated() {
        rafraichir();
    }

    private void rafraichir() {
        for (FlowerColor color : FlowerColor.values()) {
            int current = player.getScoreForColor(color);
            int prev = previousScores.getOrDefault(color, 0);
            if (current > prev) {
                bonusValues.put(color, current - prev);
                startBonusTimer();
            }
            previousScores.put(color, current);
        }

        int remainingPawns = GameModel.PAWNS_PER_PLAYER - player.getPawnsPlaced();

        nameLabel.setText(player.getName());
        nameLabel.setForeground(Color.WHITE);
        pawnLabel.setText(String.valueOf(remainingPawns));
        pawnLabel.setIcon(GameAssets.icon(GameAssets.pawn(player.getIndex()), ICON_SIZE));
        pawnLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        pawnLabel.setIconTextGap(6);

        scoreTab.removeAll();
        for (FlowerColor color : FlowerColor.values()) {
            scoreTab.add(createScoreItem(color));
        }

        scoreTab.revalidate();
        scoreTab.repaint();
        repaint();
    }

    private JLabel createPlainLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JComponent createScoreItem(FlowerColor color) {
        int playerScore = player.getScoreForColor(color);
        int opponentScore = opponent().getScoreForColor(color);
        return new ScoreItem(
            String.valueOf(playerScore),
            GameAssets.flower(color),
            scoreAura(playerScore, opponentScore),
            playerScore,
            bonusValues.getOrDefault(color, 0)
        );
    }

    private Player opponent() {
        Player[] players = model.getJoueurs();
        return players[player.getIndex() == 0 ? 1 : 0];
    }

    private Color scoreAura(int playerScore, int opponentScore) {
        if (playerScore > opponentScore) {
            return WIN_AURA;
        }
        if (playerScore < opponentScore) {
            return LOSE_AURA;
        }
        return TIE_AURA;
    }

    private void startAuraTimer() {
        if (auraTimer != null) {
            return;
        }

        auraStartedAt = System.currentTimeMillis();
        auraTimer = new Timer(70, e -> tickAura());
        auraTimer.setCoalesce(true);
        auraTimer.start();
    }

    private void tickAura() {
        double phase = (System.currentTimeMillis() - auraStartedAt) / 760.0;
        auraPulse = (float) ((Math.sin(phase) + 1.0) / 2.0);
        scoreTab.repaint();
    }

    private void stopAuraTimer() {
        if (auraTimer != null) {
            auraTimer.stop();
            auraTimer = null;
        }
    }

    private void startBonusTimer() {
        bonusStartedAt = System.currentTimeMillis();
        if (bonusTimer == null) {
            bonusTimer = new Timer(50, e -> {
                if (System.currentTimeMillis() - bonusStartedAt > 2000) {
                    bonusValues.clear();
                    bonusTimer.stop();
                    bonusTimer = null;
                }
                scoreTab.repaint();
            });
            bonusTimer.start();
        }
    }

    private boolean shouldShowAura() {
        return model.getPhase() != GameModel.GamePhase.PLACING;
    }

    private static final class RoundedTabPanel extends JPanel {
        RoundedTabPanel() {
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GameAssets.prepare(g2);
            g2.setColor(new Color(88, 87, 94, 230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(new Color(255, 255, 255, 34));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private final class ScoreItem extends JComponent {
        private final String score;
        private final BufferedImage image;
        private final Color aura;
        private final int count;
        private final int bonus;

        ScoreItem(String score, BufferedImage image, Color aura, int count, int bonus) {
            this.score = score;
            this.image = image;
            this.aura = aura;
            this.count = count;
            this.bonus = bonus;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(SCORE_ITEM_WIDTH, SCORE_ITEM_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GameAssets.prepare(g2);

            FontMetrics metrics = g2.getFontMetrics(getFont());
            int iconX = getWidth() - ICON_SIZE - 7;
            int iconCenterX = iconX + ICON_SIZE / 2;
            int centerY = getHeight() / 2;
            int textWidth = metrics.stringWidth(score);
            int textX = Math.max(0, iconX - 8 - textWidth);
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

            if (shouldShowAura() && image != null) {
                drawAura(g2, iconCenterX, centerY);
            }

            g2.setFont(getFont());
            g2.setColor(getForeground());
            g2.drawString(score, textX, textY);
            
            if (image != null) {
                GameAssets.drawFit(g2, image, iconCenterX, centerY, ICON_SIZE);
            }

            if (bonus > 0) {
                long elapsed = System.currentTimeMillis() - bonusStartedAt;
                if (elapsed < 2000) {
                    float bAlpha = Math.max(0, 1f - elapsed / 2000f);
                    float bOffset = elapsed / 50f;
                    g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                    g2.setColor(new Color(100, 255, 150, Math.round(bAlpha * 255)));
                    String msg = "+" + bonus;
                    g2.drawString(msg, iconCenterX + ICON_SIZE / 2 + 2, centerY - Math.round(bOffset));
                }
            }

            g2.dispose();
        }

        private void drawAura(Graphics2D g2, int centerX, int centerY) {
            float intensity = 0.78f + auraPulse * 0.18f;
            int pulseSize = Math.round(auraPulse * 2f);
            drawAuraCircle(g2, centerX, centerY, 34 + pulseSize, Math.round(24 * intensity));
            drawAuraCircle(g2, centerX, centerY, 28 + pulseSize, Math.round(34 * intensity));
            drawAuraCircle(g2, centerX, centerY, 22 + pulseSize, Math.round(44 * intensity));
        }

        private void drawAuraCircle(Graphics2D g2, int centerX, int centerY, int size, int alpha) {
            g2.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), alpha));
            g2.fillOval(centerX - size / 2, centerY - size / 2, size, size);
        }
    }
}
