package lacuna.view.board;

import lacuna.model.Flower;
import lacuna.model.GameModel;
import lacuna.model.Pawn;
import lacuna.model.Player;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResolutionAnimator {
    private static final int STEP_MS = 360;
    private static final int CAPTURE_MS = 220;
    private static final int TICK_MS = 25;

    private final GameModel model;
    private final BoardGeometry geometry;
    private final JComponent component;
    private List<ResolutionStep> steps = List.of();
    private Timer timer;
    private int stepIndex;
    private long stepStartedAt;
    private Runnable onFinished;

    public ResolutionAnimator(GameModel model, BoardGeometry geometry, JComponent component) {
        this.model = model;
        this.geometry = geometry;
        this.component = component;
    }

    public void prepare() {
        stop();
        stepIndex = 0;
        steps = buildSteps();
    }

    public boolean hasSteps() {
        return !steps.isEmpty();
    }

    public void start(Runnable onFinished) {
        stopTimer();
        this.onFinished = onFinished;
        stepStartedAt = System.currentTimeMillis();
        timer = new Timer(TICK_MS, e -> tick());
        timer.setCoalesce(true);
        timer.start();
        component.repaint();
    }

    public void clear() {
        stop();
        steps = List.of();
        stepIndex = 0;
    }

    public void stop() {
        stopTimer();
        onFinished = null;
    }

    public ResolutionStep currentStep() {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            return null;
        }
        return steps.get(stepIndex);
    }

    public float progress() {
        long elapsed = System.currentTimeMillis() - stepStartedAt;
        return Math.max(0f, Math.min(1f, elapsed / (float) STEP_MS));
    }

    public float vanishProgress() {
        long elapsed = System.currentTimeMillis() - stepStartedAt;
        return Math.max(0f, Math.min(1f, (elapsed - CAPTURE_MS) / (float) (STEP_MS - CAPTURE_MS)));
    }

    private void tick() {
        long now = System.currentTimeMillis();
        ResolutionStep step = currentStep();
        if (step == null) {
            finish();
            return;
        }

        long elapsed = now - stepStartedAt;
        if (!step.captured() && elapsed >= CAPTURE_MS) {
            step.capture();
            model.capturerFleurResolution(step.flower(), step.pawn());
        }

        if (elapsed >= STEP_MS) {
            stepIndex++;
            if (stepIndex >= steps.size()) {
                finish();
                return;
            }
            stepStartedAt = now;
        }

        component.repaint();
    }

    private void finish() {
        Runnable finished = onFinished;
        stop();
        component.repaint();
        if (finished != null) {
            finished.run();
        }
    }

    private List<ResolutionStep> buildSteps() {
        Map<Pawn, List<Flower>> flowersByPawn = new LinkedHashMap<>();
        for (Player player : model.getJoueurs()) {
            for (Pawn pawn : player.getPawns()) {
                if (pawn.isPlaced()) {
                    flowersByPawn.put(pawn, new ArrayList<>());
                }
            }
        }

        for (Flower flower : model.getFleurs()) {
            if (!flower.isOnBoard()) {
                continue;
            }

            Pawn closestPawn = model.trouverPionPlusProche(flower);
            if (closestPawn != null) {
                flowersByPawn.computeIfAbsent(closestPawn, ignored -> new ArrayList<>()).add(flower);
            }
        }

        List<ResolutionStep> builtSteps = new ArrayList<>();
        for (Map.Entry<Pawn, List<Flower>> entry : flowersByPawn.entrySet()) {
            Pawn pawn = entry.getKey();
            List<Flower> flowers = entry.getValue();
            flowers.sort((left, right) -> Double.compare(pawn.distanceTo(left), pawn.distanceTo(right)));

            for (Flower flower : flowers) {
                int pawnX = geometry.toScreenX(pawn.getX());
                int pawnY = geometry.toScreenY(pawn.getY()) - geometry.pawnSize() / 8;
                builtSteps.add(new ResolutionStep(
                    flower,
                    pawn,
                    geometry.toScreenX(flower.getX()),
                    geometry.toScreenY(flower.getY()),
                    pawnX,
                    pawnY,
                    BoardColors.playerGlow(pawn)
                ));
            }
        }
        return builtSteps;
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public static final class ResolutionStep {
        private final Flower flower;
        private final Pawn pawn;
        private final int flowerX;
        private final int flowerY;
        private final int pawnX;
        private final int pawnY;
        private final Color color;
        private boolean captured;

        private ResolutionStep(Flower flower, Pawn pawn, int flowerX, int flowerY, int pawnX, int pawnY, Color color) {
            this.flower = flower;
            this.pawn = pawn;
            this.flowerX = flowerX;
            this.flowerY = flowerY;
            this.pawnX = pawnX;
            this.pawnY = pawnY;
            this.color = color;
        }

        public Flower flower() {
            return flower;
        }

        public Pawn pawn() {
            return pawn;
        }

        public int flowerX() {
            return flowerX;
        }

        public int flowerY() {
            return flowerY;
        }

        public int pawnX() {
            return pawnX;
        }

        public int pawnY() {
            return pawnY;
        }

        public Color color() {
            return color;
        }

        public boolean captured() {
            return captured;
        }

        private void capture() {
            captured = true;
        }
    }
}
