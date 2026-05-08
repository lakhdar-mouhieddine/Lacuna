package lacuna.view.board;

public final class AnimationMath {
    private AnimationMath() {
    }

    public static float smooth(float value) {
        return value * value * (3f - 2f * value);
    }

    public static float easeOutBack(float value) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float t = value - 1f;
        return 1f + c3 * t * t * t + c1 * t * t;
    }
}
