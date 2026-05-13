package lacuna.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public final class OnlineSessionConnection implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String playerName;
    private final String sessionId;

    private boolean closed;

    OnlineSessionConnection(Socket socket, BufferedReader in, PrintWriter out, String playerName, String sessionId) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.playerName = playerName;
        this.sessionId = sessionId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public PeerJoinResult waitForPeerJoin() {
        try {
            String line = in.readLine();
            if (line == null) {
                return PeerJoinResult.connectionClosed();
            }

            if (line.startsWith("JOINED")) {
                String peerName = "";
                if (line.length() > "JOINED".length()) {
                    peerName = line.substring("JOINED".length()).trim();
                }
                return PeerJoinResult.joined(peerName);
            }
        } catch (IOException e) {
            return PeerJoinResult.connectionClosed();
        }

        return PeerJoinResult.connectionClosed();
    }

    public synchronized void leave() {
        close();
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        out.println("LEAVE");
        out.flush();
        out.close();

        try {
            in.close();
        } catch (IOException ignore) {
        }

        try {
            socket.close();
        } catch (IOException ignore) {
        }
    }
}
