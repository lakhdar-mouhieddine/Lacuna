package lacuna.controller;

import lacuna.model.Flower;
import lacuna.model.FlowerColor;
import lacuna.model.GameModel;
import lacuna.model.Pawn;
import lacuna.model.Player;
import lacuna.model.AI.AIMove;
import lacuna.model.AI.Minimax;
import lacuna.view.BoardPanel;
import lacuna.view.GameResultDialog;
import lacuna.view.MainFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameController {
    private static final int AI_THINK_DELAY_MS = 900;
    private static final int EASY_DEPTH = 1;

    private final GameModel model;
    private final MainFrame mainFrame;
    private final BoardPanel boardPanel;
    private final int aiPlayerIndex;

    private boolean aiThinking = false;

    public GameController(GameModel model, MainFrame mainFrame, BoardPanel boardPanel, int aiPlayerIndex) {
        this.model = model;
        this.mainFrame = mainFrame;
        this.boardPanel = boardPanel;
        this.aiPlayerIndex = aiPlayerIndex;
    }

    public boolean isAiMode() {
        return aiPlayerIndex >= 0;
    }

    public boolean isAiTurn() {
        return isAiMode() && model.getJoueurCourant().getIndex() == aiPlayerIndex;
    }

    public boolean isAiThinking() {
        return aiThinking;
    }

    public boolean onPlacementValid(Flower f1, Flower f2, double posX, double posY) {
        boolean success = model.placerPionEtCapturer(f1, f2, posX, posY);
        return success;
    }

    public void onPlacementAnimationFinished() {
        if (model.getPhase() == GameModel.GamePhase.RESOLVING) {
            mainFrame.jouerAnimationResolution(this::afficherResultat);
        } else if (model.getPhase() == GameModel.GamePhase.PLACING && isAiTurn()) {
            schedulerCoupIA();
        }
    }

    public void declencherCoupIASiNecessaire() {
        if (model.getPhase() == GameModel.GamePhase.PLACING && isAiTurn() && !aiThinking) {
            schedulerCoupIA();
        }
    }

    private void schedulerCoupIA() {
        if (aiThinking) return;
        aiThinking = true;

        Timer timer = new Timer(AI_THINK_DELAY_MS, e -> {
            SwingWorker<AIMove, Void> worker = new SwingWorker<>() {
                @Override
                protected AIMove doInBackground() {
                    Minimax minimax = new Minimax(model, aiPlayerIndex, EASY_DEPTH, 2000);
                    return minimax.findBestMove();
                }

                @Override
                protected void done() {
                    aiThinking = false;
                    try {
                        AIMove move = get();
                        if (move != null) {
                            jouerCoupIA(move);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void jouerCoupIA(AIMove move) {
        List<Flower> fleurs = model.getFleurs();
        Flower f1 = fleurs.get(move.flower1Index);
        Flower f2 = fleurs.get(move.flower2Index);

        if (!model.estLigneValide(f1, f2)) return;

        Pawn pionLibre = null;
        for (Pawn pw : model.getJoueurCourant().getPawns()) {
            if (!pw.isPlaced()) { pionLibre = pw; break; }
        }

        boolean success = model.placerPionEtCapturer(f1, f2, move.pawnX, move.pawnY);
        if (success && pionLibre != null) {
            boardPanel.jouerAnimationPionIA(pionLibre, f1, f2, this::onPlacementAnimationFinished);
        }
    }

    private void afficherResultat() {
        Player vainqueur = model.getVainqueur();
        Map<FlowerColor, Integer> majorites = model.calculerMajoritesCouleurs();
        List<FlowerColor> j1Fleurs = new ArrayList<>();
        List<FlowerColor> j2Fleurs = new ArrayList<>();

        for (Map.Entry<FlowerColor, Integer> majorite : majorites.entrySet()) {
            if (majorite.getValue() == 0) j1Fleurs.add(majorite.getKey());
            if (majorite.getValue() == 1) j2Fleurs.add(majorite.getKey());
        }

        GameResultDialog.Choice choix = GameResultDialog.show(
            mainFrame,
            "Résultats",
            new GameResultDialog.PlayerFlowers[]{
                new GameResultDialog.PlayerFlowers(model.getJoueurs()[0].getName(), j1Fleurs),
                new GameResultDialog.PlayerFlowers(model.getJoueurs()[1].getName(), j2Fleurs)
            },
            vainqueur != null ? "Vainqueur : " + vainqueur.getName() : "Egalite"
        );

        if (choix == GameResultDialog.Choice.REPLAY) {
            SwingUtilities.invokeLater(mainFrame::relancerPartie);
        } else {
            SwingUtilities.invokeLater(mainFrame::retourMenuPrincipal);
        }
    }
}
