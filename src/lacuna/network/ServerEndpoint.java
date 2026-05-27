package lacuna.network;

import java.util.Locale;
import java.util.Objects;

public final class ServerEndpoint {
    private final String host;
    private final int port;

    public ServerEndpoint(String host, int port) {
        String cleanHost = host == null ? "" : host.trim();
        if (cleanHost.isEmpty() || containsWhitespace(cleanHost)) {
            throw new IllegalArgumentException("Invalid server address.");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid server port.");
        }

        this.host = cleanHost;
        this.port = port;
    }

    public static ServerEndpoint fromText(String host, String port) {
        String cleanPort = port == null ? "" : port.trim();
        try {
            return new ServerEndpoint(host, Integer.parseInt(cleanPort));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid server port.", e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String displayName() {
        return host + ":" + port;
    }

    private static boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerEndpoint other)) {
            return false;
        }
        return port == other.port && host.equalsIgnoreCase(other.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host.toLowerCase(Locale.ROOT), port);
    }

    @Override
    public String toString() {
        return displayName();
    }
}
