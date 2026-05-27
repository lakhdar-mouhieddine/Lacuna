package lacuna.network;

public final class OnlineGameMessage {
    public enum Type {
        PLAYER_NAME,
        BOARD,
        BOARD_ACCEPTED,
        BOARD_REJECTED,
        MOVE,
        REMATCH_REQUEST,
        PEER_LEFT,
        STARTING_PLAYER
    }

    private final Type type;
    private final String playerName;
    private final OnlineBoardSnapshot board;
    private final OnlineMove move;
    private final String reason;
    private final int startingPlayerIndex;

    private OnlineGameMessage(Type type, String playerName, OnlineBoardSnapshot board, OnlineMove move, String reason, int startingPlayerIndex) {
        this.type = type;
        this.playerName = playerName;
        this.board = board;
        this.move = move;
        this.reason = reason;
        this.startingPlayerIndex = startingPlayerIndex;
    }

    static OnlineGameMessage playerName(String playerName) {
        return new OnlineGameMessage(Type.PLAYER_NAME, playerName, null, null, "", -1);
    }

    static OnlineGameMessage board(OnlineBoardSnapshot board) {
        return new OnlineGameMessage(Type.BOARD, "", board, null, "", -1);
    }

    static OnlineGameMessage boardAccepted() {
        return new OnlineGameMessage(Type.BOARD_ACCEPTED, "", null, null, "", -1);
    }

    static OnlineGameMessage boardRejected(String reason) {
        return new OnlineGameMessage(Type.BOARD_REJECTED, "", null, null, reason, -1);
    }

    static OnlineGameMessage move(OnlineMove move) {
        return new OnlineGameMessage(Type.MOVE, "", null, move, "", -1);
    }

    static OnlineGameMessage rematchRequest() {
        return new OnlineGameMessage(Type.REMATCH_REQUEST, "", null, null, "", -1);
    }

    public static OnlineGameMessage peerLeft() {
        return new OnlineGameMessage(Type.PEER_LEFT, "", null, null, "", -1);
    }

    static OnlineGameMessage startingPlayer(int startingPlayerIndex) {
        return new OnlineGameMessage(Type.STARTING_PLAYER, "", null, null, "", startingPlayerIndex);
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

    public int getStartingPlayerIndex() {
        return startingPlayerIndex;
    }
}
