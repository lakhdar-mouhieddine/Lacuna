package lacuna.view.board;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class WordSplashAnimator {
    private static final int TICK_MS = 25;

    private final JComponent component;
    private Timer timer;
    private BufferedImage image;
    private long startedAt;
    private int durationMs;
    private Runnable onFinished;

    public WordSplashAnimator(JComponent component) {
        this.component = component;
    }

    public void start(BufferedImage image, int durationMs, Runnable onFinished) {
        stop();
        if (image == null || durationMs <= 0) {
            if (onFinished != null) {
                SwingUtilities.invokeLater(onFinished);
            }
            return;
        }

        this.image = image;
        this.durationMs = durationMs;
        this.onFinished = onFinished;
        this.startedAt = System.currentTimeMillis();
        timer = new Timer(TICK_MS, e -> tick());
        timer.setCoalesce(true);
        timer.start();
        component.repaint();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        image = null;
        onFinished = null;
    }

    public boolean active() {
        return image != null;
    }

    public void paint(Graphics2D g2) {
        BufferedImage currentImage = image;
        if (currentImage == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startedAt;
        float progress = Math.max(0f, Math.min(1f, elapsed / (float) durationMs));
        float impact = AnimationMath.easeOutBack(Math.min(1f, progress / 0.32f));
        float alpha = progress < 0.82f ? 1f : Math.max(0f, (1f - progress) / 0.18f);

        double baseScale = Math.min(
            component.getWidth() * 0.86 / currentImage.getWidth(),
            component.getHeight() * 0.76 / currentImage.getHeight()
        );
        double shakeFade = 1.0 - Math.min(1.0, progress / 0.72);
        double shakeX = (Math.sin(elapsed * 0.09) * 13.0 + Math.sin(elapsed * 0.23) * 6.0) * shakeFade;
        double shakeY = (Math.cos(elapsed * 0.12) * 7.0) * shakeFade;
        double scale = baseScale * (0.72 + 0.32 * impact);
        double angle = Math.toRadians(-4.5 + Math.sin(elapsed * 0.16) * 2.6 * shakeFade);

        int drawW = (int) Math.round(currentImage.getWidth() * scale);
        int drawH = (int) Math.round(currentImage.getHeight() * scale);
        int centerX = component.getWidth() / 2 + (int) Math.round(shakeX);
        int centerY = component.getHeight() / 2 + (int) Math.round(shakeY);

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.rotate(angle, centerX, centerY);
        g2.drawImage(currentImage, centerX - drawW / 2, centerY - drawH / 2, drawW, drawH, null);
        g2.rotate(-angle, centerX, centerY);
        g2.setComposite(oldComposite);
    }

    private void tick() {
        if (System.currentTimeMillis() - startedAt < durationMs) {
            component.repaint();
            return;
        }

        Runnable finished = onFinished;
        stop();
        component.repaint();
        if (finished != null) {
            finished.run();
        }
    }
}
