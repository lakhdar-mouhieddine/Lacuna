package lacuna;

public class Pawn {
    private final Player owner;
    private double x, y;
    private boolean placed;

    public Pawn(Player owner) {
        this.owner = owner;
        this.placed = false;
    }

    public Player getOwner() { return owner; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isPlaced() { return placed; }

    public void place(double x, double y) {
        this.x = x;
        this.y = y;
        this.placed = true;
    }
    
    public double distanceTo(Flower f) {
        return f.distanceTo(this.x, this.y);
    }
}
