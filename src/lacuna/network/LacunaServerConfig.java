package lacuna.network;

import java.util.Objects;

public final class LacunaServerConfig {
    private static final ServerEndpoint OFFICIAL_ENDPOINT = new ServerEndpoint("159.89.24.57", 38400);

    private static ServerEndpoint currentEndpoint = OFFICIAL_ENDPOINT;

    private LacunaServerConfig() {
    }

    public static synchronized ServerEndpoint officialEndpoint() {
        return OFFICIAL_ENDPOINT;
    }

    public static synchronized ServerEndpoint currentEndpoint() {
        return currentEndpoint;
    }

    public static synchronized void useOfficial() {
        currentEndpoint = OFFICIAL_ENDPOINT;
    }

    public static synchronized void useCustom(ServerEndpoint endpoint) {
        currentEndpoint = Objects.requireNonNull(endpoint, "endpoint");
    }

    public static synchronized boolean isOfficial() {
        return OFFICIAL_ENDPOINT.equals(currentEndpoint);
    }
}
