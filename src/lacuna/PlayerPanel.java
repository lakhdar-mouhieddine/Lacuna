package lacuna;

import javax.swing.*;
import java.awt.*;

public class PlayerPanel extends JPanel {
    private final GameModel model;
    private final Player player;
    
    private final JLabel labelMain;
    private final JPanel panelCouleurs;

    public PlayerPanel(GameModel model, Player player, boolean isTop) {
        this.model = model;
        this.player = player;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(15, 17, 30));
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(
                isTop ? 0 : 3, 0, 
                isTop ? 3 : 0, 0, 
                player.getColor()
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

        rafraichir();
    }

    public void rafraichir() {
        int restants = GameModel.PAWNS_PER_PLAYER - player.getPawnsPlaced();
        
        String texteInfo = player.getName() 
            + "   |   Total: " + player.getTotalScore() 
            + "   |   Pions: " + restants;
            
        if (model.getPhase() == GameModel.GamePhase.PLACING && model.getJoueurCourant() == player) {
            texteInfo = "A VOTRE TOUR :  " + texteInfo;
            labelMain.setForeground(player.getColor());
        } else if (model.getPhase() == GameModel.GamePhase.FINISHED && model.getVainqueur() == player) {
            texteInfo = "VAINQUEUR :  " + texteInfo;
            labelMain.setForeground(Color.YELLOW);
        } else {
            labelMain.setForeground(Color.LIGHT_GRAY);
        }
        labelMain.setText(texteInfo);

        panelCouleurs.removeAll();
        for (int i = 0; i < Flower.COLORS.length; i++) {
            int score = player.getScoreForColor(i);
            JLabel lblCouleur = new JLabel(Flower.COLOR_NAMES[i] + ": " + score);
            lblCouleur.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblCouleur.setForeground(Flower.COLORS[i]);
            lblCouleur.setOpaque(true);
            lblCouleur.setBackground(new Color(25, 28, 48));
            lblCouleur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Flower.COLORS[i].darker(), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            panelCouleurs.add(lblCouleur);
        }
        panelCouleurs.revalidate();
        panelCouleurs.repaint();
    }
}
