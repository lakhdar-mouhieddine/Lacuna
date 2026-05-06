package lacuna.controller;

import lacuna.model.Flower;
import lacuna.model.FlowerColor;
import lacuna.model.GameModel;
import lacuna.model.Player;
import lacuna.view.GameResultDialog;
import lacuna.view.MainFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameController {
    private final GameModel model;
    private final MainFrame mainFrame;

    public GameController(GameModel model, MainFrame mainFrame) {
        this.model = model;
        this.mainFrame = mainFrame;
    }

    public boolean onPlacementValid(Flower f1, Flower f2, double posX, double posY) {
        boolean success = model.placerPionEtCapturer(f1, f2, posX, posY);
        return success;
    }

    public void onPlacementAnimationFinished() {
        if (model.getPhase() == GameModel.GamePhase.RESOLVING) {
            mainFrame.jouerAnimationResolution(this::afficherResultat);
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
