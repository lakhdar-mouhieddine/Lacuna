package lacuna;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final Color color;
    private final List<Pawn> pawns;
    private final List<Flower> capturedFlowers;
    private int pawnsPlaced;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.pawns = new ArrayList<>();
        this.capturedFlowers = new ArrayList<>();
        this.pawnsPlaced = 0;
    }

    public String getName() { return name; }
    public Color getColor() { return color; }
    public List<Pawn> getPawns() { return pawns; }
    public List<Flower> getCapturedFlowers() { return capturedFlowers; }

    public void addPawn(Pawn p) { pawns.add(p); }
    public void captureFlower(Flower f) {
        if (!capturedFlowers.contains(f)) {
            f.setOwner(this);
            capturedFlowers.add(f);
        }
    }

    public int getPawnsPlaced() { return pawnsPlaced; }
    public void incrementPawnsPlaced() { pawnsPlaced++; }
    public boolean hasFinishedPlacing() { return pawnsPlaced >= 6; }

    public int getScoreForColor(int colorIndex) {
        int count = 0;
        for (Flower f : capturedFlowers) {
            if (f.getColorIndex() == colorIndex) count++;
        }
        return count;
    }
    
    public int getTotalScore() {
        return capturedFlowers.size();
    }

    @Override
    public String toString() { return name; }
}
