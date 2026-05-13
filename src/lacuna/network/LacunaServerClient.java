package lacuna.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LacunaServerClient {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 38400;
    public static final int STATUS_TIMEOUT_MS = 700;
    public static final int STATUS_CHECK_MS = 2000;

    public boolean ping() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), STATUS_TIMEOUT_MS);
            socket.setSoTimeout(STATUS_TIMEOUT_MS);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("PING");
            return "pong".equals(in.readLine());
        } catch (IOException e) {
            return false;
        }
    }
}
