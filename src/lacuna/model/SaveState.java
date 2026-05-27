package lacuna.model;

public class SaveState implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public final GameModel model;
    public final boolean p1IsAi;
    public final int p1AiDepth;
    public final boolean p2IsAi;
    public final int p2AiDepth;
    public final String nom1;
    public final String nom2;

    public SaveState(GameModel model, boolean p1IsAi, int p1AiDepth, boolean p2IsAi, int p2AiDepth, String nom1, String nom2) {
        this.model = model;
        this.p1IsAi = p1IsAi;
        this.p1AiDepth = p1AiDepth;
        this.p2IsAi = p2IsAi;
        this.p2AiDepth = p2AiDepth;
        this.nom1 = nom1;
        this.nom2 = nom2;
    }
}
