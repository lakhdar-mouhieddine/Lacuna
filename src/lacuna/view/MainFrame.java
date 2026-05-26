package lacuna.view;

import lacuna.model.GameModel;
import lacuna.controller.GameController;
import lacuna.network.OnlineSessionConnection;
import lacuna.view.menu.MainMenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class MainFrame extends JFrame {
    private static final Integer TURN_GLOW_LAYER = JLayeredPane.DEFAULT_LAYER + 50;

    private BoardPanel plateau;
    private PlayerPanel panelTop;
    private PlayerPanel panelBottom;
    private GameController controleur;
    private TutorialOverlay tutorialOverlay;
    private String lastNom1;
    private String lastNom2;
    private boolean lastP1IsAi;
    private int lastP1AiDepth;
    private boolean lastP2IsAi;
    private int lastP2AiDepth;
    private OnlineSessionConnection onlineSessionConnection;
    private boolean lastIsHost;
    private boolean isOnlineSession;
    private int onlineGameCount = 0;

    public BoardPanel getBoardPanel() {
        return plateau;
    }

    public PlayerPanel getPanelTop() {
        return panelTop;
    }

    public PlayerPanel getPanelBottom() {
        return panelBottom;
    }

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
                this::demarrerPartieAiVsAi,
                this::chargerPartie,
                this::afficherSessionRejointe,
                this::afficherSessionCreee);

        setMinimumSize(new Dimension(550, 620));
        setContentPane(menu);
        revalidate();
        repaint();
    }

    private void demarrerPartieLocale(String nom1, String nom2) {
        fermerSessionEnLigneActuelle();
        isOnlineSession = false;
        construireInterface(nom1, nom2, false, 0, false, 0, 0, null, -1);
    }

    private void demarrerPartieAvecIA(String nomJoueur, String niveau) {
        fermerSessionEnLigneActuelle();
        isOnlineSession = false;
        int depth = 1;
        String iaName = "IA (Facile)";
        if ("Moyen".equalsIgnoreCase(niveau)) {
            depth = 2;
            iaName = "IA (Moyen)";
        } else if ("Difficile".equalsIgnoreCase(niveau)) {
            depth = 5;
            iaName = "IA (Difficile)";
        }
        construireInterface(iaName, nomJoueur, true, depth, false, 0, 0, null, -1);
    }

    private void demarrerPartieAiVsAi(String level1, String level2) {
        fermerSessionEnLigneActuelle();
        isOnlineSession = false;
        int depth1 = 1;
        String ia1Name = "IA 1 (Facile)";
        if ("Moyen".equalsIgnoreCase(level1)) {
            depth1 = 2;
            ia1Name = "IA 1 (Moyen)";
        } else if ("Difficile".equalsIgnoreCase(level1)) {
            depth1 = 5;
            ia1Name = "IA 1 (Difficile)";
        }

        int depth2 = 1;
        String ia2Name = "IA 2 (Facile)";
        if ("Moyen".equalsIgnoreCase(level2)) {
            depth2 = 2;
            ia2Name = "IA 2 (Moyen)";
        } else if ("Difficile".equalsIgnoreCase(level2)) {
            depth2 = 5;
            ia2Name = "IA 2 (Difficile)";
        }
        construireInterface(ia1Name, ia2Name, true, depth1, true, depth2, 0, null, -1);
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

        construireInterface(nom1, nom2, false, 0, false, 0, 0, sessionConnection, localPlayerIndex);
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

    private void construireInterface(String nom1, String nom2, boolean p1IsAi, int p1AiDepth, boolean p2IsAi, int p2AiDepth,
            int startingPlayerIndex, OnlineSessionConnection networkSession, int localPlayerIndex) {
        GameModel modele;
        if (networkSession != null) {
            long seed = networkSession.getSessionId().hashCode() + onlineGameCount;
            modele = new GameModel(nom1, nom2, 0, seed);
        } else {
            modele = new GameModel(nom1, nom2, 0, new java.util.Random().nextLong(), true);
        }
        initialiserInterface(modele, p1IsAi, p1AiDepth, p2IsAi, p2AiDepth, networkSession, localPlayerIndex);
    }

    private void initialiserInterface(GameModel modele, boolean p1IsAi, int p1AiDepth, boolean p2IsAi, int p2AiDepth,
            OnlineSessionConnection networkSession, int localPlayerIndex) {
        setMinimumSize(new Dimension(920, 620));
        this.lastNom1 = modele.getJoueurs()[0].getName();
        this.lastNom2 = modele.getJoueurs()[1].getName();
        this.lastP1IsAi = p1IsAi;
        this.lastP1AiDepth = p1AiDepth;
        this.lastP2IsAi = p2IsAi;
        this.lastP2AiDepth = p2AiDepth;

        panelTop = new PlayerPanel(modele, modele.getJoueurs()[0], true);
        panelBottom = new PlayerPanel(modele, modele.getJoueurs()[1], false);
        plateau = new BoardPanel(modele);
        TurnGlowPanel turnGlow = new TurnGlowPanel(modele);

        controleur = new GameController(modele, this, plateau, p1IsAi, p1AiDepth, p2IsAi, p2AiDepth, networkSession,
                localPlayerIndex);
        plateau.setController(controleur);

        JButton undoButton = createUndoButton(modele);
        JButton quitButton = createQuitButton();
        JButton helpButton = createHelpButton();

        JPanel topLeftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        topLeftBar.setOpaque(false);
        if (networkSession == null) {
            JButton saveButton = createSaveButton(modele, p1IsAi, p1AiDepth, p2IsAi, p2AiDepth);
            JButton loadButton = createLoadButton();
            topLeftBar.add(saveButton);
            topLeftBar.add(loadButton);
        }

        JPanel topRightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        topRightBar.setOpaque(false);
        topRightBar.add(helpButton);
        topRightBar.add(undoButton);
        topRightBar.add(quitButton);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.add(topLeftBar, BorderLayout.WEST);
        headerBar.add(topRightBar, BorderLayout.EAST);

        JPanel hud = new JPanel(new BorderLayout());
        hud.setOpaque(false);
        hud.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        hud.add(headerBar, BorderLayout.NORTH);
        hud.add(panelTop, BorderLayout.WEST);
        hud.add(panelBottom, BorderLayout.EAST);

        JLayeredPane gameRoot = new JLayeredPane() {
            @Override
            public void doLayout() {
                Dimension size = getSize();
                plateau.setBounds(0, 0, size.width, size.height);
                turnGlow.setBounds(0, 0, size.width, size.height);
                hud.setBounds(0, 0, size.width, size.height);
                if (tutorialOverlay != null) {
                    tutorialOverlay.setBounds(0, 0, size.width, size.height);
                }
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

    private JButton createSaveButton(GameModel modele, boolean p1IsAi, int p1AiDepth, boolean p2IsAi, int p2AiDepth) {
        JButton btn = new JButton("SAUVEGARDER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(66, 135, 245, 210));
                } else {
                    g2.setColor(new Color(88, 87, 94, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(220, 220, 230));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 32));
        btn.addActionListener(e -> sauvegarderPartie(modele, p1IsAi, p1AiDepth, p2IsAi, p2AiDepth));
        return btn;
    }

    private JButton createLoadButton() {
        JButton btn = new JButton("CHARGER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(104, 214, 132, 210));
                } else {
                    g2.setColor(new Color(88, 87, 94, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(220, 220, 230));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 32));
        btn.addActionListener(e -> chargerPartie());
        return btn;
    }

    private void sauvegarderPartie(GameModel modele, boolean p1IsAi, int p1AiDepth, boolean p2IsAi, int p2AiDepth) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sauvegarder la partie");
        fileChooser.setSelectedFile(new java.io.File("sauvegarde_lacuna.dat"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                lacuna.model.SaveState state = new lacuna.model.SaveState(
                    modele, p1IsAi, p1AiDepth, p2IsAi, p2AiDepth,
                    modele.getJoueurs()[0].getName(), modele.getJoueurs()[1].getName()
                );
                oos.writeObject(state);
                JOptionPane.showMessageDialog(this, "Partie sauvegardée avec succès !", "Sauvegarde", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerPartie() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Charger une partie");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                lacuna.model.SaveState state = (lacuna.model.SaveState) ois.readObject();
                initialiserInterface(state.model, state.p1IsAi, state.p1AiDepth, state.p2IsAi, state.p2AiDepth, null, -1);
                JOptionPane.showMessageDialog(this, "Partie chargée avec succès !", "Chargement", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors du chargement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void afficherTutoriel() {
        if (tutorialOverlay != null) {
            return;
        }
        tutorialOverlay = new TutorialOverlay(this);
        JLayeredPane gameRoot = (JLayeredPane) getContentPane();
        gameRoot.add(tutorialOverlay, JLayeredPane.DRAG_LAYER);
        tutorialOverlay.setBounds(0, 0, gameRoot.getWidth(), gameRoot.getHeight());
        gameRoot.revalidate();
        gameRoot.repaint();
    }

    public void fermerTutoriel() {
        if (tutorialOverlay != null) {
            JLayeredPane gameRoot = (JLayeredPane) getContentPane();
            gameRoot.remove(tutorialOverlay);
            tutorialOverlay = null;
            gameRoot.revalidate();
            gameRoot.repaint();
        }
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
            construireInterface(lastNom1, lastNom2, false, 0, false, 0, 0, onlineSessionConnection, lastIsHost ? 0 : 1);
        } else if (lastNom1 != null) {
            construireInterface(lastNom1, lastNom2, lastP1IsAi, lastP1AiDepth, lastP2IsAi, lastP2AiDepth, 0, null, -1);
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

    public void annulerCoup() {
        if (controleur != null) {
            controleur.annulerCoup();
        }
    }

    private JButton createUndoButton(GameModel modele) {
        JButton btn = new JButton("UNDO") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(120, 118, 130, 210));
                } else {
                    g2.setColor(new Color(88, 87, 94, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        Icon undoIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 230));
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
                // Draw curved arrow body (starts bottom right, curves up and left)
                path.moveTo(x + 13, y + 11);
                path.curveTo(x + 13, y + 2, x + 6, y + 2, x + 2, y + 6);
                // Draw arrow head
                path.moveTo(x + 2, y + 6);
                path.lineTo(x + 7, y + 6);
                path.moveTo(x + 2, y + 6);
                path.lineTo(x + 2, y + 1);

                g2.draw(path);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }
        };

        btn.setIcon(undoIcon);
        btn.setIconTextGap(6);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(220, 220, 230));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        btn.addActionListener(e -> annulerCoup());
        return btn;
    }

    private JButton createHelpButton() {
        JButton btn = new JButton("TUTORIEL") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(66, 135, 245, 210));
                } else {
                    g2.setColor(new Color(88, 87, 94, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        Icon helpIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 230));
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Draw a beautiful small question mark icon
                g2.drawArc(x + 2, y + 2, 8, 8, -40, 220);
                g2.drawLine(x + 10, y + 6, x + 6, y + 9);
                g2.drawLine(x + 6, y + 9, x + 6, y + 10);
                g2.fillRect(x + 5, y + 12, 2, 2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 14;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }
        };

        btn.setIcon(helpIcon);
        btn.setIconTextGap(6);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(220, 220, 230));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        btn.addActionListener(e -> afficherTutoriel());
        return btn;
    }

    private JButton createQuitButton() {
        JButton btn = new JButton("QUIT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 80, 88, 210)); // Reddish color for quit
                } else {
                    g2.setColor(new Color(88, 87, 94, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        Icon quitIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 230));
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawLine(x + 2, y + 2, x + 10, y + 10);
                g2.drawLine(x + 10, y + 2, x + 2, y + 10);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };

        btn.setIcon(quitIcon);
        btn.setIconTextGap(6);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(220, 220, 230));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        btn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment quitter la partie en cours ?",
                    "Quitter",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                retourMenuPrincipal();
            }
        });
        return btn;
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
