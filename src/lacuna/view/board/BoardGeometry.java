package lacuna.view.board;

import lacuna.view.GameAssets;

import javax.swing.JComponent;
import java.awt.Rectangle;

public final class BoardGeometry {
    private static final double BOARD_CENTER_X = 0.50;
    private static final double BOARD_CENTER_Y = 0.50;
    private static final double BOARD_RADIUS_X = 0.365;
    private static final double TILT_FACTOR = 0.70;
    private static final double DESIGN_BOARD_WIDTH = 957.0;
    private static final double FLOWER_SCALE = 0.65;

    private final JComponent component;

    public BoardGeometry(JComponent component) {
        this.component = component;
    }

    public Rectangle boardBounds() {
        return GameAssets.coverBounds(GameAssets.board(), component.getWidth(), component.getHeight());
    }

    public int toScreenX(double modelX) {
        Rectangle bounds = boardBounds();
        return (int) Math.round(boardCenterX(bounds) + modelX * pixelScale());
    }

    public int toScreenY(double modelY) {
        Rectangle bounds = boardBounds();
        return (int) Math.round(boardCenterY(bounds) + modelY * pixelScale() * TILT_FACTOR);
    }

    public double toModelX(int screenX) {
        Rectangle bounds = boardBounds();
        return (screenX - boardCenterX(bounds)) / pixelScale();
    }

    public double toModelY(int screenY) {
        Rectangle bounds = boardBounds();
        return (screenY - boardCenterY(bounds)) / (pixelScale() * TILT_FACTOR);
    }

    public int flowerSize() {
        return (int) Math.round(scaledAssetSize(30) * FLOWER_SCALE);
    }

    public int pawnSize() {
        return scaledAssetSize(34);
    }

    public double pixelScale() {
        return boardBounds().width * BOARD_RADIUS_X;
    }

    private int scaledAssetSize(int designSize) {
        Rectangle bounds = boardBounds();
        int size = (int) Math.round(designSize * bounds.width / DESIGN_BOARD_WIDTH);
        return Math.max(20, size);
    }

    private int boardCenterX(Rectangle bounds) {
        return bounds.x + (int) Math.round(bounds.width * BOARD_CENTER_X);
    }

    private int boardCenterY(Rectangle bounds) {
        return bounds.y + (int) Math.round(bounds.height * BOARD_CENTER_Y);
    }
}
