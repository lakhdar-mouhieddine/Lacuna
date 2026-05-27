package lacuna.network;

import java.io.IOException;

public final class PeerLeftException extends IOException {
    public PeerLeftException() {
        super("Peer left");
    }
}
