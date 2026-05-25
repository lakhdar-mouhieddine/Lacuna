package lacuna.network;

public final class OnlineGameMessage {
    public enum Type {
        PLAYER_NAME,
        BOARD,
        BOARD_ACCEPTED,
        BOARD_REJECTED,
        MOVE
    }

    private final Type type;
    private final String playerName;
    private final OnlineBoardSnapshot board;
    private final OnlineMove move;
    private final String reason;

    private OnlineGameMessage(Type type, String playerName, OnlineBoardSnapshot board, OnlineMove move, String reason) {
        this.type = type;
        this.playerName = playerName;
        this.board = board;
        this.move = move;
        this.reason = reason;
    }

    static OnlineGameMessage playerName(String playerName) {
        return new OnlineGameMessage(Type.PLAYER_NAME, playerName, null, null, "");
    }

    static OnlineGameMessage board(OnlineBoardSnapshot board) {
        return new OnlineGameMessage(Type.BOARD, "", board, null, "");
    }

    static OnlineGameMessage boardAccepted() {
        return new OnlineGameMessage(Type.BOARD_ACCEPTED, "", null, null, "");
    }

    static OnlineGameMessage boardRejected(String reason) {
        return new OnlineGameMessage(Type.BOARD_REJECTED, "", null, null, reason);
    }

    static OnlineGameMessage move(OnlineMove move) {
        return new OnlineGameMessage(Type.MOVE, "", null, move, "");
    }

    public Type getType() {
        return type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public OnlineBoardSnapshot getBoard() {
        return board;
    }

    public OnlineMove getMove() {
        return move;
    }

    public String getReason() {
        return reason;
    }
}
