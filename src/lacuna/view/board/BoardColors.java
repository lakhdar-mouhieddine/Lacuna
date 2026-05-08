package lacuna.view.board;

import lacuna.model.Pawn;

import java.awt.Color;

public final class BoardColors {
    private static final Color PLAYER_ONE_GLOW = new Color(132, 220, 255);
    private static final Color PLAYER_TWO_GLOW = new Color(235, 195, 72);

    private BoardColors() {
    }

    public static Color playerGlow(Pawn pawn) {
        if (pawn.getOwner().getIndex() == 0) {
            return PLAYER_ONE_GLOW;
        }
        return PLAYER_TWO_GLOW;
    }
}
