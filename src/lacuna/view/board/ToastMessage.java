package lacuna.view.board;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public final class ToastMessage {
    private static final int DURATION_MS = 2300;
    private static final int FADE_MS = 260;
    private static final int TICK_MS = 25;
    private static final int PADDING_X = 18;
    private static final int PADDING_Y = 10;
    private static final int ARC = 18;

    private final JComponent component;
    private Timer timer;
    private String text;
    private long startedAt;
    private boolean showTop;

    public ToastMessage(JComponent component) {
        this.component = component;
    }

    public void show(String text) {
        show(text, false);
    }

    public void show(String text, boolean top) {
        this.text = text;
        this.showTop = top;
        startedAt = System.currentTimeMillis();

        if (timer == null) {
            timer = new Timer(TICK_MS, e -> tick());
            timer.setCoalesce(true);
            timer.start();
        }
        component.repaint();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        text = null;
    }

    public void paint(Graphics2D g2) {
        if (text == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startedAt;
        float alpha = alphaFor(elapsed);
        if (alpha <= 0f) {
            return;
        }

        Font oldFont = g2.getFont();
        Font font = oldFont.deriveFont(Font.BOLD, 15f);
        g2.setFont(font);

        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int width = textWidth + PADDING_X * 2;
        int height = metrics.getHeight() + PADDING_Y * 2;
        int x = (component.getWidth() - width) / 2;
        int y = showTop ? Math.round(component.getHeight() * 0.12f)
                        : Math.round(component.getHeight() * 0.82f);

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(new Color(218, 218, 222, 230));
        g2.fillRoundRect(x, y, width, height, ARC, ARC);
        g2.setColor(new Color(28, 29, 34, 235));
        g2.drawString(text, x + PADDING_X, y + PADDING_Y + metrics.getAscent());
        g2.setComposite(oldComposite);
        g2.setFont(oldFont);
    }

    private void tick() {
        if (System.currentTimeMillis() - startedAt >= DURATION_MS) {
            stop();
        }
        component.repaint();
    }

    private float alphaFor(long elapsed) {
        if (elapsed < FADE_MS) {
            return AnimationMath.smooth(elapsed / (float) FADE_MS);
        }

        int fadeOutStart = DURATION_MS - FADE_MS;
        if (elapsed > fadeOutStart) {
            float fade = (DURATION_MS - elapsed) / (float) FADE_MS;
            return AnimationMath.smooth(Math.max(0f, Math.min(1f, fade)));
        }

        return 1f;
    }
}
