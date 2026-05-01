package lacuna.view;

import lacuna.model.FlowerColor;
import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

public class Theme {
    private static final Map<FlowerColor, Color> FLOWER_COLORS = new EnumMap<>(FlowerColor.class);
    private static final Map<FlowerColor, String> FLOWER_NAMES = new EnumMap<>(FlowerColor.class);

    static {
        FLOWER_COLORS.put(FlowerColor.RED, new Color(220, 50, 50));
        FLOWER_COLORS.put(FlowerColor.ORANGE, new Color(255, 165, 0));
        FLOWER_COLORS.put(FlowerColor.CYAN, new Color(0, 210, 210));
        FLOWER_COLORS.put(FlowerColor.GREEN, new Color(50, 180, 50));
        FLOWER_COLORS.put(FlowerColor.BLUE, new Color(50, 130, 220));
        FLOWER_COLORS.put(FlowerColor.PURPLE, new Color(140, 60, 200));
        FLOWER_COLORS.put(FlowerColor.PINK, new Color(220, 100, 180));

        FLOWER_NAMES.put(FlowerColor.RED, "Rouge");
        FLOWER_NAMES.put(FlowerColor.ORANGE, "Orange");
        FLOWER_NAMES.put(FlowerColor.CYAN, "Cyan");
        FLOWER_NAMES.put(FlowerColor.GREEN, "Vert");
        FLOWER_NAMES.put(FlowerColor.BLUE, "Bleu");
        FLOWER_NAMES.put(FlowerColor.PURPLE, "Violet");
        FLOWER_NAMES.put(FlowerColor.PINK, "Rose");
    }

    public static Color getColor(FlowerColor fc) {
        return FLOWER_COLORS.get(fc);
    }

    public static String getName(FlowerColor fc) {
        return FLOWER_NAMES.get(fc);
    }

    public static Color getPlayerColor(int playerIndex) {
        return playerIndex == 0 ? Color.decode("#E53935") : Color.decode("#1E88E5");
    }
}
