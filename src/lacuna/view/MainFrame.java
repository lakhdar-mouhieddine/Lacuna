package lacuna.view;

import lacuna.model.GameModel;
import lacuna.controller.GameController;
import lacuna.view.menu.MainMenuPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final Integer TURN_GLOW_LAYER = JLayeredPane.DEFAULT_LAYER + 50;

    private BoardPanel plateau;
    private PlayerPanel panelTop;
    private PlayerPanel panelBottom;
    private GameController controleur;

    public MainFrame() {
        super("Lacuna");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 620));

        afficherMenu();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void afficherMenu() {
        MainMenuPanel menu = new MainMenuPanel(this::demarrerPartieLocale, this::demarrerPartieAvecIA);

        setContentPane(menu);
        revalidate();
        repaint();
    }

    private void demarrerPartieLocale(String nom1, String nom2) {
        construireInterface(nom1, nom2, -1);
    }

    private void demarrerPartieAvecIA(String nomJoueur, String niveau) {
        construireInterface(nomJoueur, "IA (Facile)", 1);
    }

    private void construireInterface(String nom1, String nom2, int aiPlayerIndex) {
        GameModel modele = new GameModel(nom1, nom2);

        panelTop = new PlayerPanel(modele, modele.getJoueurs()[0], true);
        panelBottom = new PlayerPanel(modele, modele.getJoueurs()[1], false);
        plateau = new BoardPanel(modele);
        TurnGlowPanel turnGlow = new TurnGlowPanel(modele);

        controleur = new GameController(modele, this, plateau, aiPlayerIndex);
        plateau.setController(controleur);

        JPanel hud = new JPanel(new BorderLayout());
        hud.setOpaque(false);
        hud.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        hud.add(panelTop, BorderLayout.WEST);
        hud.add(panelBottom, BorderLayout.EAST);

        JLayeredPane gameRoot = new JLayeredPane() {
            @Override
            public void doLayout() {
                Dimension size = getSize();
                plateau.setBounds(0, 0, size.width, size.height);
                turnGlow.setBounds(0, 0, size.width, size.height);
                hud.setBounds(0, 0, size.width, size.height);
            }
        };
        gameRoot.setBackground(new Color(15, 16, 22));
        gameRoot.setOpaque(true);
        gameRoot.add(plateau, JLayeredPane.DEFAULT_LAYER);
        gameRoot.add(turnGlow, TURN_GLOW_LAYER);
        gameRoot.add(hud, JLayeredPane.PALETTE_LAYER);
        
        setContentPane(gameRoot);
        revalidate();
        repaint();
    }

    public void relancerPartie() {
        afficherMenu();
    }

    public void jouerAnimationResolution(Runnable apresAnimation) {
        plateau.jouerAnimationResolution(apresAnimation);
    }

    public void retourMenuPrincipal() {
        afficherMenu();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignore) {
            }
            new MainFrame();
        });
    }
}
