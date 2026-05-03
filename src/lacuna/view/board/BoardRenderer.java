package lacuna.view.board;

import lacuna.model.Flower;
import lacuna.model.GameModel;
import lacuna.model.Pawn;
import lacuna.model.Player;
import lacuna.view.GameAssets;
import lacuna.view.Theme;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

public final class BoardRenderer {
    private static final BasicStroke RESOLUTION_LINE_STROKE =
        new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke RESOLUTION_PULSE_STROKE =
        new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final GameModel model;
    private final BoardGeometry geometry;
    private final JComponent component;
    private final WordSplashAnimator wordSplash;
    private final FlowerIntroAnimator flowerIntro;
    private final PlacementAnimator placement;
    private final ResolutionAnimator resolution;

    public BoardRenderer(
        GameModel model,
        BoardGeometry geometry,
        JComponent component,
        WordSplashAnimator wordSplash,
        FlowerIntroAnimator flowerIntro,
        PlacementAnimator placement,
        ResolutionAnimator resolution
    ) {
        this.model = model;
        this.geometry = geometry;
        this.component = component;
        this.wordSplash = wordSplash;
        this.flowerIntro = flowerIntro;
        this.placement = placement;
        this.resolution = resolution;
    }

    public void paint(Graphics2D g2, List<FlowerPair> candidates, int selectedIndex, Flower hoveredFlower, Point mousePoint) {
        drawBoard(g2);
        if (model.getPhase() == GameModel.GamePhase.PLACING) {
            drawSelectionGuide(g2, candidates, selectedIndex, hoveredFlower, mousePoint);
        }
        drawElements(g2, null, hoveredFlower);
        drawPlacementFlowerAnimation(g2);
        drawResolutionEffect(g2);
        wordSplash.paint(g2);
    }

    private void drawBoard(Graphics2D g2) {
        BufferedImage board = GameAssets.board();
        Rectangle bounds = geometry.boardBounds();

        if (board == null) {
            g2.setColor(new Color(19, 23, 30));
            g2.fillRect(0, 0, component.getWidth(), component.getHeight());
            return;
        }

        g2.drawImage(board, bounds.x, bounds.y, bounds.width, bounds.height, null);
    }

    private void drawSelectionGuide(Graphics2D g2, List<FlowerPair> candidates, int selectedIndex, Flower hoveredFlower, Point mousePoint) {
        if (mousePoint == null) return;

        // pion fantôme au niveau du curseur
        int pawnSize = geometry.pawnSize();
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        GameAssets.drawFit(g2, GameAssets.pawn(model.getJoueurCourant().getIndex()), mousePoint.x, mousePoint.y - pawnSize / 8, pawnSize);
        g2.setComposite(oldComp);

        if (candidates.isEmpty()) return;

        for (int i = 0; i < candidates.size(); i++) {
            FlowerPair pair = candidates.get(i);
            boolean isSelected = (i == selectedIndex);
            Color pairColor = Theme.getColor(pair.f1().getColor());
            
            int x1 = geometry.toScreenX(pair.f1().getX());
            int y1 = geometry.toScreenY(pair.f1().getY());
            int x2 = geometry.toScreenX(pair.f2().getX());
            int y2 = geometry.toScreenY(pair.f2().getY());

            if (isSelected) {
                g2.setColor(withAlpha(pairColor, 0.85f));
                g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else {
                g2.setColor(withAlpha(pairColor, 0.35f));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5f}, 0));
            }
            
            g2.drawLine(x1, y1, x2, y2);
            
            if (isSelected) {
                int midX = (x1 + x2) / 2;
                int midY = (y1 + y2) / 2;
                drawGlow(g2, midX, midY, (int)(pawnSize * 1.2), withAlpha(pairColor, 0.25f));
            }
        }
    }

    private void drawElements(Graphics2D g2, Flower selectedFlower, Flower hoveredFlower) {
        java.util.List<ElementOnBoard> elements = new java.util.ArrayList<>();

        for (Flower flower : model.getFleurs()) {
            if (flower.isOnBoard()) {
                elements.add(new ElementOnBoard(flower, flower.getY()));
            }
        }
        for (Player player : model.getJoueurs()) {
            for (Pawn pawn : player.getPawns()) {
                if (pawn.isPlaced()) {
                    elements.add(new ElementOnBoard(pawn, pawn.getY()));
                }
            }
        }

        elements.sort((left, right) -> Double.compare(left.y(), right.y()));
        for (ElementOnBoard element : elements) {
            drawElement(g2, element.item(), selectedFlower, hoveredFlower);
        }
    }

    private void drawElement(Graphics2D g2, Object item, Flower selectedFlower, Flower hoveredFlower) {
        if (item instanceof Flower flower) {
            if (shouldDrawFlowerIntro()) {
                drawFlowerIntro(g2, flower);
            } else if (flowerIntro.flowersVisible() || model.getPhase() != GameModel.GamePhase.PLACING) {
                drawFlower(g2, flower, selectedFlower, hoveredFlower);
            }
            return;
        }

        if (item instanceof Pawn pawn) {
            if (placement.isPawn(pawn)) {
                drawPlacementPawn(g2, pawn);
            } else {
                drawPawn(g2, pawn);
            }
        }
    }

    private boolean shouldDrawFlowerIntro() {
        return model.getPhase() == GameModel.GamePhase.PLACING && flowerIntro.active();
    }

    private void drawFlower(Graphics2D g2, Flower flower, Flower selectedFlower, Flower hoveredFlower) {
        int x = geometry.toScreenX(flower.getX());
        int y = geometry.toScreenY(flower.getY());
        int size = geometry.flowerSize();

        if (flower == selectedFlower) {
            drawGlow(g2, x, y, size + 16, new Color(255, 255, 255, 145));
        }

        GameAssets.drawFit(g2, GameAssets.flower(flower.getColor()), x, y, size);
    }

    private void drawFlowerIntro(Graphics2D g2, Flower flower) {
        float progress = flowerIntro.progressFor(flower);
        if (progress <= 0f) {
            return;
        }

        int x = geometry.toScreenX(flower.getX());
        int y = geometry.toScreenY(flower.getY());
        int size = geometry.flowerSize();
        float eased = AnimationMath.smooth(progress);
        int drawSize = Math.max(1, Math.round(size * (0.42f + 0.58f * eased)));

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, eased));
        drawGlow(g2, x, y, Math.round(drawSize * 1.45f), new Color(255, 255, 255, Math.round(34 * eased)));
        GameAssets.drawFit(g2, GameAssets.flower(flower.getColor()), x, y, drawSize);
        g2.setComposite(oldComposite);
    }

    private void drawPawn(Graphics2D g2, Pawn pawn) {
        int x = geometry.toScreenX(pawn.getX());
        int y = geometry.toScreenY(pawn.getY());
        int size = geometry.pawnSize();

        GameAssets.drawFit(g2, GameAssets.pawn(pawn.getOwner().getIndex()), x, y - size / 8, size);
    }

    private void drawPlacementPawn(Graphics2D g2, Pawn pawn) {
        float progress = placement.progress();
        float eased = Math.min(1.08f, AnimationMath.easeOutBack(Math.min(1f, progress / 0.82f)));
        float alpha = Math.max(0f, Math.min(1f, progress / 0.45f));
        int baseSize = geometry.pawnSize();
        int size = Math.max(1, Math.round(baseSize * (0.58f + 0.42f * eased)));
        int x = geometry.toScreenX(pawn.getX());
        int y = geometry.toScreenY(pawn.getY()) - size / 8;

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawGlow(g2, x, y, Math.round(size * 1.35f), placementGlow(pawn, alpha));
        GameAssets.drawFit(g2, GameAssets.pawn(pawn.getOwner().getIndex()), x, y, size);
        g2.setComposite(oldComposite);
    }

    private Color placementGlow(Pawn pawn, float alpha) {
        Color base = BoardColors.playerGlow(pawn);
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.round(50 * alpha));
    }

    private void drawPlacementFlowerAnimation(Graphics2D g2) {
        PlacementAnimator.PlacementAnimation animation = placement.animation();
        if (animation == null) {
            return;
        }

        float progress = placement.progress();
        drawPlacementDisappearingFlower(g2, animation.firstFlower(), progress);
        drawPlacementDisappearingFlower(g2, animation.secondFlower(), progress);
    }

    private void drawPlacementDisappearingFlower(Graphics2D g2, Flower flower, float progress) {
        float eased = AnimationMath.smooth(progress);
        float alpha = Math.max(0f, 1f - eased);
        int x = geometry.toScreenX(flower.getX());
        int y = geometry.toScreenY(flower.getY());
        int size = Math.max(1, Math.round(geometry.flowerSize() * (1f - 0.58f * eased)));

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawGlow(g2, x, y, Math.round(size * 1.45f), new Color(255, 255, 255, Math.round(34 * alpha)));
        GameAssets.drawFit(g2, GameAssets.flower(flower.getColor()), x, y, size);
        g2.setComposite(oldComposite);
    }

    private void drawResolutionEffect(Graphics2D g2) {
        ResolutionAnimator.ResolutionStep step = resolution.currentStep();
        if (wordSplash.active() || model.getPhase() != GameModel.GamePhase.RESOLVING || step == null) {
            return;
        }

        float progress = resolution.progress();
        Color color = step.color();
        float lineFade = progress < 0.78f ? 1f : Math.max(0f, (1f - progress) / 0.22f);
        g2.setStroke(RESOLUTION_LINE_STROKE);
        g2.setColor(withAlpha(color, 0.46f * lineFade));
        g2.drawLine(step.pawnX(), step.pawnY(), step.flowerX(), step.flowerY());

        float pulseProgress = Math.min(1f, progress / 0.72f);
        int flowerSize = geometry.flowerSize();
        int radius = flowerSize / 2 + Math.round(flowerSize * 0.65f * pulseProgress);
        float pulseFade = Math.max(0f, 1f - progress * 0.55f);
        g2.setStroke(RESOLUTION_PULSE_STROKE);
        g2.setColor(withAlpha(color, 0.78f * pulseFade));
        g2.drawOval(step.flowerX() - radius, step.flowerY() - radius, radius * 2, radius * 2);

        if (step.captured()) {
            drawFadingFlower(g2, step, resolution.vanishProgress());
        }
    }

    private void drawFadingFlower(Graphics2D g2, ResolutionAnimator.ResolutionStep step, float progress) {
        Composite oldComposite = g2.getComposite();
        float alpha = Math.max(0f, 1f - progress);
        int size = Math.max(1, Math.round(geometry.flowerSize() * (1f - 0.35f * progress)));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        GameAssets.drawFit(g2, GameAssets.flower(step.flower().getColor()), step.flowerX(), step.flowerY(), size);
        g2.setComposite(oldComposite);
    }

    private void drawGlow(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.fillOval(x - size / 2, y - size / 2, size, size);
    }

    private Color withAlpha(Color color, float alpha) {
        int value = Math.max(0, Math.min(255, Math.round(alpha * 255)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), value);
    }

    private record ElementOnBoard(Object item, double y) {
    }
}
