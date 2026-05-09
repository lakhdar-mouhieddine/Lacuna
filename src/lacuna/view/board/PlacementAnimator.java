package lacuna.view.board;

import lacuna.model.Flower;
import lacuna.model.Pawn;

import javax.swing.JComponent;
import javax.swing.Timer;

public final class PlacementAnimator {
    private static final int DURATION_MS = 320;
    private static final int TICK_MS = 25;

    private final JComponent component;
    private Timer timer;
    private PlacementAnimation animation;
    private long startedAt;
    private Runnable onFinished;

    public PlacementAnimator(JComponent component) {
        this.component = component;
    }

    public void start(Pawn pawn, Flower firstFlower, Flower secondFlower, Runnable onFinished) {
        stop();
        animation = new PlacementAnimation(pawn, firstFlower, secondFlower);
        this.onFinished = onFinished;
        startedAt = System.currentTimeMillis();
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
        animation = null;
        onFinished = null;
    }

    public boolean active() {
        return animation != null;
    }

    public boolean isPawn(Pawn pawn) {
        return animation != null && animation.pawn() == pawn;
    }

    public PlacementAnimation animation() {
        return animation;
    }

    public float progress() {
        long elapsed = System.currentTimeMillis() - startedAt;
        return Math.max(0f, Math.min(1f, elapsed / (float) DURATION_MS));
    }

    private void tick() {
        if (System.currentTimeMillis() - startedAt < DURATION_MS) {
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

    public record PlacementAnimation(Pawn pawn, Flower firstFlower, Flower secondFlower) {
    }
}
