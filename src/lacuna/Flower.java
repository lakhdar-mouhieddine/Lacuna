package lacuna;

import java.awt.Color;

public class Flower {
    public static final Color[] COLORS = {
        new Color(220, 50,  50),
        new Color(255, 165,  0),
        new Color(240, 220,  0),
        new Color(50,  180,  50),
        new Color(50,  130, 220),
        new Color(140,  60, 200),
        new Color(220, 100, 180),
    };

    public static final String[] COLOR_NAMES = {
        "Rouge", "Orange", "Jaune", "Vert", "Bleu", "Violet", "Rose"
    };

    private final int colorIndex;
    private double x, y;
    private Player owner;

    public Flower(int colorIndex, double x, double y) {
        this.colorIndex = colorIndex;
        this.x = x;
        this.y = y;
        this.owner = null;
    }

    public int getColorIndex() { return colorIndex; }
    public Color getColor() { return COLORS[colorIndex]; }
    public String getColorName() { return COLOR_NAMES[colorIndex]; }
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
