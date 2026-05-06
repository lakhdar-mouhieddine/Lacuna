package lacuna.model;

public class Flower {
    private final FlowerColor color;
    private double x, y;
    private Player owner;

    public Flower(FlowerColor color, double x, double y) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.owner = null;
    }

    public FlowerColor getColor() { return color; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
    public boolean isOnBoard() { return owner == null; }
    
    public double distanceTo(double px, double py) {
        double dx = this.x - px;
        double dy = this.y - py;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
