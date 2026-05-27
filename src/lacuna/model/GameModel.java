package lacuna.model;

import lacuna.network.OnlineBoardSnapshot;
import lacuna.network.OnlineFlowerSnapshot;

import java.util.*;
import java.awt.geom.Line2D;

public class GameModel implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final int NUM_COLORS = FlowerColor.values().length;
    public static final int FLOWERS_PER_COLOR = 7;
    public static final int NUM_FLOWERS = NUM_COLORS * FLOWERS_PER_COLOR;
    public static final int PAWNS_PER_PLAYER = 6;
    
    private static final double MAX_RADIUS_X = 0.76;
    private static final double MAX_RADIUS_Y = 0.88;
    private static final double MIN_FLOWER_SPACING = 0.12;
    private static final double VALIDATION_EPSILON = 1e-9;
    public static final double HITBOX_RADIUS = 0.05;

    private final List<Flower> flowers;
    private final Player[] players;
    private int currentPlayerIndex;
    private GamePhase phase;
    private long seed;

    private transient List<ModelListener> listeners = new ArrayList<>();
    private final java.util.Deque<MoveRecord> moveHistory = new java.util.ArrayDeque<>();
    private transient java.util.Deque<MoveRecord> redoHistory = new java.util.ArrayDeque<>();

    public enum GamePhase {
        PLACING,
        RESOLVING,
        FINISHED
    }

    public GameModel(String name1, String name2) {
        this(name1, name2, 0, new Random().nextLong());
    }

    public GameModel(String name1, String name2, long seed) {
        this(name1, name2, 0, seed);
    }

    public GameModel(String name1, String name2, int startingPlayerIndex, long seed) {
        this(name1, name2, startingPlayerIndex, seed, false);
    }

    public GameModel(String name1, String name2, int startingPlayerIndex, long seed, boolean deferStart) {
        flowers = new ArrayList<>(NUM_FLOWERS);
        players = new Player[]{
            new Player(name1, 0),
            new Player(name2, 1)
        };
        currentPlayerIndex = startingPlayerIndex;
        phase = GamePhase.PLACING;
        this.seed = seed;

        Random rng = new Random(seed);
        genererFleursTapis(rng);
        creerPions();
        if (!deferStart) {
            assignRandomStartingFlower(rng, startingPlayerIndex);
        }
    }

    public void setStartingPlayer(int index) {
        this.currentPlayerIndex = index;
        if (flowers.isEmpty()) return;
        Random rng = new Random();
        Flower startingFlower = flowers.get(rng.nextInt(flowers.size()));
        players[index].captureFlower(startingFlower);
        notifyListeners();
    }

    public boolean estNouvellePartieNonCommencee() {
        return players[0].getCapturedFlowers().isEmpty() && players[1].getCapturedFlowers().isEmpty();
    }

    private GameModel(String name1, String name2, OnlineBoardSnapshot snapshot) {
        String validationError = validateOnlineBoardSnapshot(snapshot);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        flowers = new ArrayList<>(NUM_FLOWERS);
        players = new Player[]{
            new Player(name1, 0),
            new Player(name2, 1)
        };
        currentPlayerIndex = 0;
        phase = GamePhase.PLACING;

        for (OnlineFlowerSnapshot flower : snapshot.getFlowers()) {
            flowers.add(new Flower(flower.getColor(), flower.getX(), flower.getY()));
        }

        creerPions();
        players[0].captureFlower(flowers.get(snapshot.getStartingCapturedFlowerIndex()));
    }

    public static GameModel fromOnlineBoardSnapshot(String name1, String name2, OnlineBoardSnapshot snapshot) {
        return new GameModel(name1, name2, snapshot);
    }

    public void addModelListener(ModelListener l) { listeners.add(l); }
    public void removeModelListener(ModelListener l) { listeners.remove(l); }
    private void notifyListeners() {
        for (ModelListener l : listeners) {
            l.onModelUpdated();
        }
    }

    private void genererFleursTapis(Random rng) {
        List<FlowerColor> poolCouleurs = new ArrayList<>(NUM_FLOWERS);
        for (FlowerColor c : FlowerColor.values()) {
            for (int i = 0; i < FLOWERS_PER_COLOR; i++) poolCouleurs.add(c);
        }
        Collections.shuffle(poolCouleurs, rng);

        for (int i = 0; i < NUM_FLOWERS; i++) {
            double x = 0, y = 0;
            boolean valid = false;
            int tentatives = 0;
            while (!valid && tentatives < 500) {
                double angle = rng.nextDouble() * 2 * Math.PI;
                double r = Math.sqrt(rng.nextDouble());
                x = r * Math.cos(angle) * MAX_RADIUS_X;
                y = r * Math.sin(angle) * MAX_RADIUS_Y;
                
                valid = true;
                for (Flower f : flowers) {
                    if (f.distanceTo(x, y) < MIN_FLOWER_SPACING) {
                        valid = false;
                        break;
                    }
                }
                tentatives++;
            }
            flowers.add(new Flower(poolCouleurs.get(i), x, y));
        }
    }

    private void creerPions() {
        for (Player p : players) {
            for (int i = 0; i < PAWNS_PER_PLAYER; i++) p.addPawn(new Pawn(p));
        }
    }

    private void assignRandomStartingFlower(Random rng, int startingPlayerIndex) {
        if (flowers.isEmpty()) return;
        Flower startingFlower = flowers.get(rng.nextInt(flowers.size()));
        players[startingPlayerIndex].captureFlower(startingFlower);
    }

    public OnlineBoardSnapshot toOnlineBoardSnapshot() {
        List<OnlineFlowerSnapshot> snapshots = new ArrayList<>();
        for (Flower flower : flowers) {
            snapshots.add(new OnlineFlowerSnapshot(flower.getColor(), flower.getX(), flower.getY()));
        }

        int startingCapturedFlowerIndex = -1;
        for (Flower flower : players[0].getCapturedFlowers()) {
            int index = flowers.indexOf(flower);
            if (index >= 0) {
                startingCapturedFlowerIndex = index;
                break;
            }
        }

        return new OnlineBoardSnapshot(snapshots, startingCapturedFlowerIndex);
    }

    public static String validateOnlineBoardSnapshot(OnlineBoardSnapshot snapshot) {
        if (snapshot == null) {
            return "Plateau absent.";
        }

        List<OnlineFlowerSnapshot> snapshotFlowers = snapshot.getFlowers();
        if (snapshotFlowers.size() != NUM_FLOWERS) {
            return "Nombre de fleurs invalide.";
        }

        int startingIndex = snapshot.getStartingCapturedFlowerIndex();
        if (startingIndex < 0 || startingIndex >= NUM_FLOWERS) {
            return "Fleur de depart invalide.";
        }

        EnumMap<FlowerColor, Integer> colorCounts = new EnumMap<>(FlowerColor.class);
        for (FlowerColor color : FlowerColor.values()) {
            colorCounts.put(color, 0);
        }

        for (int i = 0; i < snapshotFlowers.size(); i++) {
            OnlineFlowerSnapshot flower = snapshotFlowers.get(i);
            if (flower == null || flower.getColor() == null) {
                return "Fleur invalide.";
            }
            if (!Double.isFinite(flower.getX()) || !Double.isFinite(flower.getY())) {
                return "Coordonnees invalides.";
            }
            if (!isInsideBoardEllipse(flower.getX(), flower.getY())) {
                return "Fleur hors du plateau.";
            }

            colorCounts.put(flower.getColor(), colorCounts.get(flower.getColor()) + 1);

            for (int j = 0; j < i; j++) {
                OnlineFlowerSnapshot other = snapshotFlowers.get(j);
                double dx = flower.getX() - other.getX();
                double dy = flower.getY() - other.getY();
                if (Math.sqrt(dx * dx + dy * dy) + VALIDATION_EPSILON < MIN_FLOWER_SPACING) {
                    return "Fleurs trop proches.";
                }
            }
        }

        for (FlowerColor color : FlowerColor.values()) {
            if (colorCounts.get(color) != FLOWERS_PER_COLOR) {
                return "Distribution des couleurs invalide.";
            }
        }

        return null;
    }

    private static boolean isInsideBoardEllipse(double x, double y) {
        double normalizedX = x / MAX_RADIUS_X;
        double normalizedY = y / MAX_RADIUS_Y;
        return normalizedX * normalizedX + normalizedY * normalizedY <= 1.0 + VALIDATION_EPSILON;
    }

    public boolean estLigneValide(Flower f1, Flower f2) {
        if (f1 == f2) return false;
        if (f1.getColor() != f2.getColor()) return false;
        if (!f1.isOnBoard() || !f2.isOnBoard()) return false;

        Line2D.Double ligne = new Line2D.Double(f1.getX(), f1.getY(), f2.getX(), f2.getY());

        for (Flower f : flowers) {
            if (!f.isOnBoard() || f == f1 || f == f2) continue;
            if (ligne.ptSegDist(f.getX(), f.getY()) < HITBOX_RADIUS) return false;
        }

        for (Player p : players) {
            for (Pawn pawn : p.getPawns()) {
                if (pawn.isPlaced()) {
                    if (ligne.ptSegDist(pawn.getX(), pawn.getY()) < HITBOX_RADIUS) return false;
                }
            }
        }
        return true;
    }

    public boolean placerPionEtCapturer(Flower f1, Flower f2, double px, double py) {
        if (phase != GamePhase.PLACING) return false;
        if (!estLigneValide(f1, f2)) return false;

        Player courant = getJoueurCourant();
        Pawn pionLibre = null;
        for (Pawn pw : courant.getPawns()) {
            if (!pw.isPlaced()) { pionLibre = pw; break; }
        }
        if (pionLibre == null) return false;

        moveHistory.push(new MoveRecord(courant, pionLibre, f1, f2, px, py));
        redoHistory.clear();

        pionLibre.place(px, py);
        courant.incrementPawnsPlaced();
        courant.captureFlower(f1);
        courant.captureFlower(f2);

        avancerTour();
        notifyListeners();
        return true;
    }

    public boolean annulerDernierCoup() {
        if (moveHistory.isEmpty() || phase != GamePhase.PLACING) return false;

        MoveRecord move = moveHistory.pop();
        redoHistory.push(move);
        move.player.uncaptureFlower(move.flower1);
        move.player.uncaptureFlower(move.flower2);
        move.pawn.unplace();
        move.player.decrementPawnsPlaced();
        currentPlayerIndex = move.player.getIndex();

        notifyListeners();
        return true;
    }

    public boolean peutAnnuler() {
        return !moveHistory.isEmpty() && phase == GamePhase.PLACING;
    }

    public boolean refaireDernierCoup() {
        if (redoHistory.isEmpty() || phase != GamePhase.PLACING) return false;

        MoveRecord move = redoHistory.pop();
        moveHistory.push(move);
        move.pawn.place(move.pawnX, move.pawnY);
        move.player.incrementPawnsPlaced();
        move.player.captureFlower(move.flower1);
        move.player.captureFlower(move.flower2);

        avancerTour();
        notifyListeners();
        return true;
    }

    public boolean peutRefaire() {
        return !redoHistory.isEmpty() && phase == GamePhase.PLACING;
    }

    private void avancerTour() {
        boolean tousFinis = true;
        for (Player p : players) {
            if (!p.hasFinishedPlacing()) tousFinis = false;
        }

        if (tousFinis) {
            phase = GamePhase.RESOLVING;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        }
    }

    public void resoudreProximite() {
        for (Flower f : flowers) {
            if (!f.isOnBoard()) continue;

            Pawn plusProche = trouverPionPlusProche(f);

            if (plusProche != null) {
                plusProche.getOwner().captureFlower(f);
            }
        }

        phase = GamePhase.FINISHED;
        notifyListeners();
    }

    public Pawn trouverPionPlusProche(Flower fleur) {
        Pawn plusProche = null;
        double distMin = Double.MAX_VALUE;

        for (Player player : players) {
            for (Pawn pion : player.getPawns()) {
                if (!pion.isPlaced()) continue;
                double distance = pion.distanceTo(fleur);
                if (distance < distMin) {
                    distMin = distance;
                    plusProche = pion;
                }
            }
        }

        return plusProche;
    }

    public void capturerFleurResolution(Flower fleur, Pawn pion) {
        if (phase != GamePhase.RESOLVING || fleur == null || pion == null || !fleur.isOnBoard()) {
            return;
        }

        pion.getOwner().captureFlower(fleur);
        notifyListeners();
    }

    public void terminerResolution() {
        if (phase != GamePhase.RESOLVING) {
            return;
        }

        phase = GamePhase.FINISHED;
        notifyListeners();
    }

    public Map<FlowerColor, Integer> calculerMajoritesCouleurs() {
        Map<FlowerColor, Integer> resultats = new EnumMap<>(FlowerColor.class);
        for (FlowerColor c : FlowerColor.values()) {
            int scoreJ1 = players[0].getScoreForColor(c);
            int scoreJ2 = players[1].getScoreForColor(c);
            if (scoreJ1 > scoreJ2) resultats.put(c, 0);
            else if (scoreJ2 > scoreJ1) resultats.put(c, 1);
        }
        return resultats;
    }

    public Player getVainqueur() {
        if (phase != GamePhase.FINISHED) return null;
        Map<FlowerColor, Integer> majorites = calculerMajoritesCouleurs();
        int couleursJ1 = 0, couleursJ2 = 0;
        for (int m : majorites.values()) {
            if (m == 0) couleursJ1++;
            if (m == 1) couleursJ2++;
        }
        if (couleursJ1 >= 4) return players[0];
        if (couleursJ2 >= 4) return players[1];
        return null;
    }

    public List<Flower> getFleurs() { return Collections.unmodifiableList(flowers); }
    public Player[] getJoueurs() { return players; }
    public Player getJoueurCourant() { return players[currentPlayerIndex]; }
    public GamePhase getPhase() { return phase; }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new ArrayList<>();
        redoHistory = new java.util.ArrayDeque<>();
    }

    private static final class MoveRecord implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        final Player player;
        final Pawn pawn;
        final Flower flower1;
        final Flower flower2;
        final double pawnX;
        final double pawnY;

        MoveRecord(Player player, Pawn pawn, Flower flower1, Flower flower2, double pawnX, double pawnY) {
            this.player = player;
            this.pawn = pawn;
            this.flower1 = flower1;
            this.flower2 = flower2;
            this.pawnX = pawnX;
            this.pawnY = pawnY;
        }
    }
}
