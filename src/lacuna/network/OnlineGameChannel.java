package lacuna.network;

import lacuna.model.FlowerColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class OnlineGameChannel {
    private static final String PLAYER_NAME = "PLAYER_NAME";
    private static final String BOARD = "BOARD";
    private static final String BOARD_ACCEPTED = "BOARD_ACCEPTED";
    private static final String BOARD_REJECTED = "BOARD_REJECTED";
    private static final String MOVE = "MOVE";
    private static final String REMATCH_REQUEST = "REMATCH_REQUEST";

    private final OnlineSessionConnection connection;

    public OnlineGameChannel(OnlineSessionConnection connection) {
        this.connection = connection;
    }

    public boolean sendPlayerName(String playerName) {
        List<String> lines = new ArrayList<>();
        lines.add(PLAYER_NAME);
        lines.add(sanitize(playerName));
        return connection.sendData(lines);
    }

    public String receivePlayerName() throws IOException {
        OnlineGameMessage message = receiveMessage();
        if (message.getType() != OnlineGameMessage.Type.PLAYER_NAME) {
            throw new IOException("Expected player name, got " + message.getType());
        }
        return message.getPlayerName();
    }

    public boolean sendBoard(OnlineBoardSnapshot snapshot) {
        List<String> lines = new ArrayList<>();
        lines.add(BOARD);
        lines.add(String.valueOf(snapshot.getFlowers().size()));
        lines.add(String.valueOf(snapshot.getStartingCapturedFlowerIndex()));
        for (OnlineFlowerSnapshot flower : snapshot.getFlowers()) {
            lines.add(flower.getColor().name() + " " + flower.getX() + " " + flower.getY());
        }
        return connection.sendData(lines);
    }

    public OnlineBoardSnapshot receiveBoard() throws IOException {
        OnlineGameMessage message = receiveMessage();
        if (message.getType() != OnlineGameMessage.Type.BOARD) {
            throw new IOException("Expected board, got " + message.getType());
        }
        return message.getBoard();
    }

    public boolean sendBoardAccepted() {
        List<String> lines = new ArrayList<>();
        lines.add(BOARD_ACCEPTED);
        return connection.sendData(lines);
    }

    public boolean sendBoardRejected(String reason) {
        List<String> lines = new ArrayList<>();
        lines.add(BOARD_REJECTED);
        lines.add(sanitize(reason));
        return connection.sendData(lines);
    }

    public boolean sendMove(OnlineMove move) {
        List<String> lines = new ArrayList<>();
        lines.add(MOVE);
        lines.add(String.valueOf(move.getFirstFlowerIndex()));
        lines.add(String.valueOf(move.getSecondFlowerIndex()));
        lines.add(String.valueOf(move.getPawnX()));
        lines.add(String.valueOf(move.getPawnY()));
        return connection.sendData(lines);
    }

    public boolean sendRematchRequest() {
        List<String> lines = new ArrayList<>();
        lines.add(REMATCH_REQUEST);
        return connection.sendData(lines);
    }

    public OnlineGameMessage receiveMessage() throws IOException {
        List<String> lines = connection.receiveData();
        if (lines.isEmpty()) {
            throw new IOException("Empty online game message");
        }

        String type = lines.get(0);
        if (PLAYER_NAME.equals(type)) {
            return OnlineGameMessage.playerName(lines.size() >= 2 ? lines.get(1) : "");
        }
        if (BOARD.equals(type)) {
            return OnlineGameMessage.board(readBoard(lines));
        }
        if (BOARD_ACCEPTED.equals(type)) {
            return OnlineGameMessage.boardAccepted();
        }
        if (BOARD_REJECTED.equals(type)) {
            return OnlineGameMessage.boardRejected(lines.size() >= 2 ? lines.get(1) : "");
        }
        if (MOVE.equals(type)) {
            return OnlineGameMessage.move(readMove(lines));
        }
        if (REMATCH_REQUEST.equals(type)) {
            return OnlineGameMessage.rematchRequest();
        }

        throw new IOException("Unknown online game message: " + type);
    }

    private static OnlineBoardSnapshot readBoard(List<String> lines) throws IOException {
        if (lines.size() < 3) {
            throw new IOException("Incomplete board message");
        }

        int flowerCount = parseInt(lines.get(1), "flower count");
        int startingFlowerIndex = parseInt(lines.get(2), "starting flower index");
        if (lines.size() != 3 + flowerCount) {
            throw new IOException("Board flower count does not match message size");
        }

        List<OnlineFlowerSnapshot> flowers = new ArrayList<>();
        for (int i = 0; i < flowerCount; i++) {
            flowers.add(readFlower(lines.get(3 + i)));
        }

        return new OnlineBoardSnapshot(flowers, startingFlowerIndex);
    }

    private static OnlineFlowerSnapshot readFlower(String line) throws IOException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IOException("Invalid flower line");
        }

        try {
            FlowerColor color = FlowerColor.valueOf(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            return new OnlineFlowerSnapshot(color, x, y);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid flower data", e);
        }
    }

    private static OnlineMove readMove(List<String> lines) throws IOException {
        if (lines.size() != 5) {
            throw new IOException("Invalid move message");
        }

        int firstFlowerIndex = parseInt(lines.get(1), "first flower index");
        int secondFlowerIndex = parseInt(lines.get(2), "second flower index");
        double pawnX = parseDouble(lines.get(3), "pawn x");
        double pawnY = parseDouble(lines.get(4), "pawn y");
        return new OnlineMove(firstFlowerIndex, secondFlowerIndex, pawnX, pawnY);
    }

    private static int parseInt(String value, String name) throws IOException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid " + name, e);
        }
    }

    private static double parseDouble(String value, String name) throws IOException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid " + name, e);
        }
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
