package lacuna.view;

import lacuna.controller.GameController;
import lacuna.model.Flower;
import lacuna.model.GameModel;
import lacuna.model.ModelListener;
import lacuna.model.Pawn;
import lacuna.model.Player;
import lacuna.view.board.BoardGeometry;
import lacuna.view.board.BoardRenderer;
import lacuna.view.board.FlowerIntroAnimator;
import lacuna.view.board.PlacementAnimator;
import lacuna.view.board.ResolutionAnimator;
import lacuna.view.board.ToastMessage;
import lacuna.view.board.WordSplashAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class BoardPanel extends JPanel implements ModelListener {
    private static final int LETS_PLAY_SPLASH_MS = 1150;
    private static final int HOLD_UP_SPLASH_MS = 1200;
    private static final int GAME_OVER_SPLASH_MS = 950;

    private final GameModel model;
    private final BoardGeometry geometry;
    private final WordSplashAnimator wordSplash;
    private final FlowerIntroAnimator flowerIntro;
    private final PlacementAnimator placement;
    private final ResolutionAnimator resolution;
    private final ToastMessage toast;
    private final BoardRenderer renderer;

    private GameController controller;
    private Flower hoveredFlower;
    private Flower firstSelectedFlower;
    private Point mousePoint;
    private Runnable onResolutionFinished;

    public BoardPanel(GameModel model) {
        this.model = model;
        this.geometry = new BoardGeometry(this);
        this.wordSplash = new WordSplashAnimator(this);
        this.flowerIntro = new FlowerIntroAnimator(model, this);
        this.placement = new PlacementAnimator(this);
        this.resolution = new ResolutionAnimator(model, geometry, this);
        this.toast = new ToastMessage(this);
        this.renderer = new BoardRenderer(model, geometry, this, wordSplash, flowerIntro, placement, resolution);

        this.model.addModelListener(this);
        setBackground(new Color(20, 22, 28));
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                updateHover(e.getX(), e.getY());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY(), e.getButton());
            }
        });
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        wordSplash.start(GameAssets.letsPlay(), LETS_PLAY_SPLASH_MS, flowerIntro::start);
    }

    @Override
    public void removeNotify() {
        flowerIntro.stop();
        placement.stop();
        resolution.clear();
        wordSplash.stop();
        toast.stop();
        super.removeNotify();
    }

    public void jouerAnimationResolution(Runnable onFinished) {
        clearSelection();
        onResolutionFinished = onFinished;
        resolution.prepare();

        if (!resolution.hasSteps()) {
            finishResolutionAnimation();
            return;
        }

        wordSplash.start(GameAssets.holdUp(), HOLD_UP_SPLASH_MS,
            () -> resolution.start(() -> wordSplash.start(
                GameAssets.gameOver(),
                GAME_OVER_SPLASH_MS,
                this::finishResolutionAnimation
            ))
        );
    }

    @Override
    public void onModelUpdated() {
        if (model.getPhase() != GameModel.GamePhase.PLACING) {
            clearSelection();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        GameAssets.prepare(g2);
        renderer.paint(g2, firstSelectedFlower, hoveredFlower, mousePoint);
        toast.paint(g2);
        g2.dispose();
    }

    private void updateHover(int x, int y) {
        if (isInputBlocked() || model.getPhase() != GameModel.GamePhase.PLACING) {
            return;
        }

        Flower flower = flowerAt(x, y);
        if (flower != hoveredFlower) {
            hoveredFlower = flower;
            repaint();
        } else if (firstSelectedFlower != null) {
            repaint();
        }
    }

    private void handleClick(int x, int y, int button) {
        if (isInputBlocked() || model.getPhase() != GameModel.GamePhase.PLACING || controller == null) {
            return;
        }

        if (button == MouseEvent.BUTTON3) {
            firstSelectedFlower = null;
            repaint();
            return;
        }

        if (firstSelectedFlower == null) {
            Flower clicked = flowerAt(x, y);
            if (clicked != null) {
                firstSelectedFlower = clicked;
                repaint();
            }
            return;
        }

        Flower clickedFlower = flowerAt(x, y);
        if (clickedFlower != null) {
            placePawnIfValid(clickedFlower);
        } else {
            toast.show("Choisis une deuxieme fleur de meme couleur.");
        }
        repaint();
    }

    private void placePawnIfValid(Flower clickedFlower) {
        if (clickedFlower == firstSelectedFlower) {
            firstSelectedFlower = null;
            return;
        }

        if (clickedFlower.getColor() != firstSelectedFlower.getColor()) {
            toast.show("Les deux fleurs doivent avoir la meme couleur.");
            return;
        }

        Flower firstFlower = firstSelectedFlower;
        double modelX = (firstFlower.getX() + clickedFlower.getX()) / 2.0;
        double modelY = (firstFlower.getY() + clickedFlower.getY()) / 2.0;
        if (model.estLigneValide(firstFlower, clickedFlower)) {
            Pawn placedPawn = nextUnplacedPawn(model.getJoueurCourant());
            boolean success = controller.onPlacementValid(firstFlower, clickedFlower, modelX, modelY);
            firstSelectedFlower = null;
            if (success && placedPawn != null) {
                placement.start(placedPawn, firstFlower, clickedFlower, this::finishPlacementAnimation);
            }
        } else {
            toast.show("La ligne traverse une autre piece.");
        }
    }

    private void finishPlacementAnimation() {
        if (controller != null) {
            controller.onPlacementAnimationFinished();
        }
    }

    private void finishResolutionAnimation() {
        resolution.clear();
        wordSplash.stop();
        model.terminerResolution();

        Runnable finished = onResolutionFinished;
        onResolutionFinished = null;
        if (finished != null) {
            finished.run();
        }
    }

    private Flower flowerAt(int x, int y) {
        double modelX = geometry.toModelX(x);
        double modelY = geometry.toModelY(y);
        double threshold = (geometry.flowerSize() / 2.0 + 5) / geometry.pixelScale();

        for (Flower flower : model.getFleurs()) {
            if (flower.isOnBoard() && flower.distanceTo(modelX, modelY) <= threshold) {
                return flower;
            }
        }
        return null;
    }

    private void clearSelection() {
        firstSelectedFlower = null;
        hoveredFlower = null;
        mousePoint = null;
    }

    private boolean isInputBlocked() {
        return wordSplash.active() || flowerIntro.blocksInput() || placement.active();
    }

    private Pawn nextUnplacedPawn(Player player) {
        for (Pawn pawn : player.getPawns()) {
            if (!pawn.isPlaced()) {
                return pawn;
            }
        }
        return null;
    }
}
