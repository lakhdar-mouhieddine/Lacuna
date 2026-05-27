package lacuna.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OnlineBoardSnapshot {
    private final List<OnlineFlowerSnapshot> flowers;
    private final int startingCapturedFlowerIndex;
    private final int startingPlayerIndex;

    public OnlineBoardSnapshot(List<OnlineFlowerSnapshot> flowers, int startingCapturedFlowerIndex, int startingPlayerIndex) {
        this.flowers = Collections.unmodifiableList(new ArrayList<>(flowers));
        this.startingCapturedFlowerIndex = startingCapturedFlowerIndex;
        this.startingPlayerIndex = startingPlayerIndex;
    }

    public OnlineBoardSnapshot(List<OnlineFlowerSnapshot> flowers, int startingCapturedFlowerIndex) {
        this(flowers, startingCapturedFlowerIndex, 0);
    }

    public List<OnlineFlowerSnapshot> getFlowers() {
        return flowers;
    }

    public int getStartingCapturedFlowerIndex() {
        return startingCapturedFlowerIndex;
    }

    public int getStartingPlayerIndex() {
        return startingPlayerIndex;
    }
}
