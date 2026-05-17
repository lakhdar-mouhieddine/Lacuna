package lacuna.view;

import lacuna.model.GameModel;
import lacuna.controller.GameController;
import lacuna.network.OnlineSessionConnection;
import lacuna.view.menu.MainMenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static final Integer TURN_GLOW_LAYER = JLayeredPane.DEFAULT_LAYER + 50;

    private BoardPanel plateau;
    private PlayerPanel panelTop;
    private PlayerPanel panelBottom;
    private GameController controleur;
    private String lastNom1;
    private String lastNom2;
    private int lastAiPlayerIndex;
    private int lastAiDepth;
    private OnlineSessionConnection onlineSessionConnection;
    private boolean lastIsHost;
    private boolean isOnlineSession;
    private int onlineGameCount = 0;

    public MainFrame() {
        super("Lacuna");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 620));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fermerSessionEnLigneActuelle();
            }
        });

        afficherMenu();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void afficherMenu() {
        MainMenuPanel menu = new MainMenuPanel(
                this::demarrerPartieLocale,
                this::demarrerPartieAvecIA,
                this::afficherSessionRejointe,
                this::afficherSessionCreee);

        setContentPane(menu);
        revalidate();
        repaint();
    }

    private void demarrerPartieLocale(String nom1, String nom2) {
        fermerSessionEnLigneActuelle();
        isOnlineSession = false;
        construireInterface(nom1, nom2, -1, 0, null, -1);
    }

    private void demarrerPartieAvecIA(String nomJoueur, String niveau) {
        fermerSessionEnLigneActuelle();
        isOnlineSession = false;
        int depth = 1;
        String iaName = "IA (Facile)";
        if ("Moyen".equalsIgnoreCase(niveau)) {
            depth = 3;
            iaName = "IA (Moyen)";
        } else if ("Difficile".equalsIgnoreCase(niveau)) {
            depth = 5;
            iaName = "IA (Difficile)";
        }
        construireInterface(iaName, nomJoueur, 0, depth, null, -1);
    }

    private void afficherSessionRejointe(OnlineSessionConnection sessionConnection) {
        demarrerPartieEnLigne(sessionConnection, false);
    }

    private void afficherSessionCreee(OnlineSessionConnection sessionConnection) {
        demarrerPartieEnLigne(sessionConnection, true);
    }

    private void demarrerPartieEnLigne(OnlineSessionConnection sessionConnection, boolean isHost) {
        if (onlineSessionConnection != sessionConnection) {
            fermerSessionEnLigneActuelle();
            onlineSessionConnection = sessionConnection;
        }
        isOnlineSession = true;
        this.lastIsHost = isHost;
        this.onlineGameCount = 0;
        
        String myName = sessionConnection.getPlayerName();
        String nom1 = isHost ? myName : "Adversaire";
        String nom2 = isHost ? "Adversaire" : myName;
        int localPlayerIndex = isHost ? 0 : 1;
        
        construireInterface(nom1, nom2, -1, 0, sessionConnection, localPlayerIndex);
    }

    private void fermerSessionEnLigneActuelle() {
        if (onlineSessionConnection != null) {
            onlineSessionConnection.close();
            onlineSessionConnection = null;
        }
    }

    private JPanel createCenteredPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 23, 29));
        return panel;
    }

    private JLabel createOnlineLabel(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void construireInterface(String nom1, String nom2, int aiPlayerIndex, int aiDepth, OnlineSessionConnection networkSession, int localPlayerIndex) {
        this.lastNom1 = nom1;
        this.lastNom2 = nom2;
        this.lastAiPlayerIndex = aiPlayerIndex;
        this.lastAiDepth = aiDepth;
        
        GameModel modele;
        if (networkSession != null) {
            long seed = networkSession.getSessionId().hashCode() + onlineGameCount;
            modele = new GameModel(nom1, nom2, seed);
        } else {
            modele = new GameModel(nom1, nom2);
        }

        panelTop = new PlayerPanel(modele, modele.getJoueurs()[0], true);
        panelBottom = new PlayerPanel(modele, modele.getJoueurs()[1], false);
        plateau = new BoardPanel(modele);
        TurnGlowPanel turnGlow = new TurnGlowPanel(modele);

        controleur = new GameController(modele, this, plateau, aiPlayerIndex, aiDepth, networkSession, localPlayerIndex);
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

    public void afficherAttenteRematch() {
        JPanel placeholder = createCenteredPanel();
        placeholder.add(createOnlineLabel("En attente de l'adversaire...", 28, new Color(245, 244, 248)));
        setContentPane(placeholder);
        revalidate();
        repaint();
    }

    public void relancerPartie() {
        if (isOnlineSession && onlineSessionConnection != null) {
            onlineGameCount++;
            construireInterface(lastNom1, lastNom2, -1, 0, onlineSessionConnection, lastIsHost ? 0 : 1);
        } else if (lastNom1 != null) {
            construireInterface(lastNom1, lastNom2, lastAiPlayerIndex, lastAiDepth, null, -1);
        } else {
            afficherMenu();
        }
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
