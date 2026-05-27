package lacuna.network;

import lacuna.model.FlowerColor;

public final class OnlineFlowerSnapshot {
    private final FlowerColor color;
    private final double x;
    private final double y;

    public OnlineFlowerSnapshot(FlowerColor color, double x, double y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public FlowerColor getColor() {
        return color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
