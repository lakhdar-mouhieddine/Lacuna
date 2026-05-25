package lacuna.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OnlineBoardSnapshot {
    private final List<OnlineFlowerSnapshot> flowers;
    private final int startingCapturedFlowerIndex;

    public OnlineBoardSnapshot(List<OnlineFlowerSnapshot> flowers, int startingCapturedFlowerIndex) {
        this.flowers = Collections.unmodifiableList(new ArrayList<>(flowers));
        this.startingCapturedFlowerIndex = startingCapturedFlowerIndex;
    }

    public List<OnlineFlowerSnapshot> getFlowers() {
        return flowers;
    }

    public int getStartingCapturedFlowerIndex() {
        return startingCapturedFlowerIndex;
    }
}
