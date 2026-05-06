package lacuna.view;

import lacuna.model.GameModel;
import lacuna.model.Player;
import lacuna.model.FlowerColor;
import lacuna.model.ModelListener;

import javax.swing.*;
import java.awt.*;

public class PlayerPanel extends JPanel implements ModelListener {
    private final GameModel model;
    private final Player player;
    private final Color playerColor;
    
    private final JLabel labelMain;
    private final JPanel panelCouleurs;

    public PlayerPanel(GameModel model, Player player, boolean isTop) {
        this.model = model;
        this.player = player;
        this.playerColor = Theme.getPlayerColor(player.getIndex());
        
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(15, 17, 30));
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(
                isTop ? 0 : 3, 0, 
                isTop ? 3 : 0, 0, 
                playerColor
            ),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        labelMain = new JLabel();
        labelMain.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelMain.setForeground(Color.WHITE);

        panelCouleurs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelCouleurs.setOpaque(false);

        add(labelMain, BorderLayout.WEST);
        add(panelCouleurs, BorderLayout.EAST);

        model.addModelListener(this);
        rafraichir();
    }

    @Override
    public void onModelUpdated() {
        rafraichir();
    }

    private void rafraichir() {
        int restants = GameModel.PAWNS_PER_PLAYER - player.getPawnsPlaced();
        
        String texteInfo = player.getName() 
            + "   |   Total: " + player.getTotalScore() 
            + "   |   Pions: " + restants;
            
        if (model.getPhase() == GameModel.GamePhase.PLACING && model.getJoueurCourant() == player) {
            texteInfo = "A VOTRE TOUR :  " + texteInfo;
            labelMain.setForeground(playerColor);
        } else if (model.getPhase() == GameModel.GamePhase.FINISHED && model.getVainqueur() == player) {
            texteInfo = "VAINQUEUR :  " + texteInfo;
            labelMain.setForeground(Color.YELLOW);
        } else {
            labelMain.setForeground(Color.LIGHT_GRAY);
        }
        labelMain.setText(texteInfo);

        panelCouleurs.removeAll();
        for (FlowerColor fc : FlowerColor.values()) {
            int score = player.getScoreForColor(fc);
            Color c = Theme.getColor(fc);
            JLabel lblCouleur = new JLabel(Theme.getName(fc) + ": " + score);
            lblCouleur.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblCouleur.setForeground(c);
            lblCouleur.setOpaque(true);
            lblCouleur.setBackground(new Color(25, 28, 48));
            lblCouleur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c.darker(), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            panelCouleurs.add(lblCouleur);
        }
        panelCouleurs.revalidate();
        panelCouleurs.repaint();
    }
}
