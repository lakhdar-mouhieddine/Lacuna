package lacuna.view.board;

import lacuna.model.Flower;

public record FlowerPair(Flower f1, Flower f2) {
    public double midpointX() { return (f1.getX() + f2.getX()) / 2.0; }
    public double midpointY() { return (f1.getY() + f2.getY()) / 2.0; }
}
