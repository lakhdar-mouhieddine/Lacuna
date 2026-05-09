package lacuna.view.board;

import lacuna.model.Flower;
import lacuna.model.GameModel;

import javax.swing.JComponent;
import javax.swing.Timer;

public final class FlowerIntroAnimator {
    private static final int STAGGER_MS = 14;
    private static final int FADE_MS = 360;
    private static final int TICK_MS = 25;

    private final GameModel model;
    private final JComponent component;
    private Timer timer;
    private long startedAt;
    private boolean active;
    private boolean flowersVisible;

    public FlowerIntroAnimator(GameModel model, JComponent component) {
        this.model = model;
        this.component = component;
    }

    public void start() {
        if (model.getPhase() != GameModel.GamePhase.PLACING) {
            flowersVisible = true;
            return;
        }

        stop();
        active = true;
        flowersVisible = false;
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
        active = false;
    }

    public boolean active() {
        return active;
    }

    public boolean flowersVisible() {
        return flowersVisible;
    }

    public boolean blocksInput() {
        return active || !flowersVisible;
    }

    public float progressFor(Flower flower) {
        int delay = indexOf(flower) * STAGGER_MS;
        long elapsed = System.currentTimeMillis() - startedAt - delay;
        return Math.max(0f, Math.min(1f, elapsed / (float) FADE_MS));
    }

    private void tick() {
        if (System.currentTimeMillis() - startedAt >= totalMs()) {
            active = false;
            flowersVisible = true;
            stopTimer();
            component.repaint();
            return;
        }

        component.repaint();
    }

    private int totalMs() {
        int visibleFlowers = 0;
        for (Flower flower : model.getFleurs()) {
            if (flower.isOnBoard()) {
                visibleFlowers++;
            }
        }
        return Math.max(0, (visibleFlowers - 1) * STAGGER_MS) + FADE_MS;
    }

    private int indexOf(Flower target) {
        int index = 0;
        for (Flower flower : model.getFleurs()) {
            if (!flower.isOnBoard()) {
                continue;
            }
            if (flower == target) {
                return index;
            }
            index++;
        }
        return index;
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
