package lacuna.controller;

import lacuna.model.GameModel;
import lacuna.model.Flower;
import lacuna.model.Player;
import lacuna.model.FlowerColor;
import lacuna.view.MainFrame;

import javax.swing.*;
import java.util.Map;

public class GameController {
    private final GameModel model;
    private final MainFrame mainFrame;

    public GameController(GameModel model, MainFrame mainFrame) {
        this.model = model;
        this.mainFrame = mainFrame;
    }

    public void onPlacementValid(Flower f1, Flower f2, double posX, double posY) {
        boolean success = model.placerPionEtCapturer(f1, f2, posX, posY);
        if (!success) return;

        if (model.getPhase() == GameModel.GamePhase.RESOLVING) {
            Timer t = new Timer(1000, e -> {
                model.resoudreProximite();
                afficherResultat();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void afficherResultat() {
        Player vainqueur = model.getVainqueur();
        String msg = "Fin de partie !\n\n";
        
        Map<FlowerColor, Integer> majorites = model.calculerMajoritesCouleurs();
        int j1Maj = 0, j2Maj = 0;
        
        for (int m : majorites.values()) {
            if (m == 0) j1Maj++;
            if (m == 1) j2Maj++;
        }

        msg += model.getJoueurs()[0].getName() + " remporte " + j1Maj + " couleurs.\n";
        msg += model.getJoueurs()[1].getName() + " remporte " + j2Maj + " couleurs.\n\n";

        if (vainqueur != null) {
            msg += "Vainqueur : " + vainqueur.getName() + " !";
        } else {
            msg += "Égalité !";
        }

        int choix = JOptionPane.showOptionDialog(mainFrame, msg, "Victoire",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
            new String[]{"Rejouer", "Quitter"}, "Rejouer");

        if (choix == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> {
                mainFrame.relancerPartie();
            });
        } else {
            System.exit(0);
        }
    }
}
