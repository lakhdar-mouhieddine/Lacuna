package lacuna.network;

public final class OnlineMove {
    private final int firstFlowerIndex;
    private final int secondFlowerIndex;
    private final double pawnX;
    private final double pawnY;

    public OnlineMove(int firstFlowerIndex, int secondFlowerIndex, double pawnX, double pawnY) {
        this.firstFlowerIndex = firstFlowerIndex;
        this.secondFlowerIndex = secondFlowerIndex;
        this.pawnX = pawnX;
        this.pawnY = pawnY;
    }

    public int getFirstFlowerIndex() {
        return firstFlowerIndex;
    }

    public int getSecondFlowerIndex() {
        return secondFlowerIndex;
    }

    public double getPawnX() {
        return pawnX;
    }

    public double getPawnY() {
        return pawnY;
    }
}
