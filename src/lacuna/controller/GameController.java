package lacuna.controller;

import lacuna.model.Flower;
import lacuna.model.FlowerColor;
import lacuna.model.GameModel;
import lacuna.model.Pawn;
import lacuna.model.Player;
import lacuna.model.AI.AIMove;
import lacuna.model.AI.Minimax;
import lacuna.network.OnlineGameChannel;
import lacuna.network.OnlineGameMessage;
import lacuna.network.OnlineMove;
import lacuna.network.PeerLeftException;
import lacuna.view.BoardPanel;
import lacuna.view.GameResultDialog;
import lacuna.view.MainFrame;
import lacuna.view.OnlineGameResultDialog;

import javax.swing.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameController {
    private static final int AI_THINK_DELAY_MS = 900;

    private final GameModel model;
    private final MainFrame mainFrame;
    private final BoardPanel boardPanel;
    private final OnlineGameChannel onlineChannel;
    private final boolean p1IsAi;
    private final int p1AiDepth;
    private final boolean p2IsAi;
    private final int p2AiDepth;
    private final lacuna.network.OnlineSessionConnection networkSession;
    private final int localPlayerIndex;
    private SwingWorker<Void, OnlineGameMessage> networkWorker;
    private OnlineGameResultDialog onlineResultDialog;

    private boolean aiThinking = false;
    private Timer aiTimer;
    private SwingWorker<AIMove, Void> aiWorker;
    private volatile boolean localRematchRequested;
    private volatile boolean peerRematchRequested;
    private volatile boolean peerLeftAfterResult;
    private volatile boolean peerLeftHandled;
    private volatile boolean listenerEndedForResult;

    public GameController(GameModel model, MainFrame mainFrame, BoardPanel boardPanel, boolean p1IsAi, int p1AiDepth, boolean p2IsAi, int p2AiDepth, lacuna.network.OnlineSessionConnection networkSession, int localPlayerIndex) {
        this.model = model;
        this.mainFrame = mainFrame;
        this.boardPanel = boardPanel;
        this.p1IsAi = p1IsAi;
        this.p1AiDepth = p1AiDepth;
        this.p2IsAi = p2IsAi;
        this.p2AiDepth = p2AiDepth;
        this.networkSession = networkSession;
        this.onlineChannel = networkSession != null ? new OnlineGameChannel(networkSession) : null;
        this.localPlayerIndex = localPlayerIndex;
        
        if (onlineChannel != null) {
            startNetworkListener();
        }
    }

    private void startNetworkListener() {
        networkWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    try {
                        OnlineGameMessage message = onlineChannel.receiveMessage();
                        publish(message);
                        if (message.getType() == OnlineGameMessage.Type.REMATCH_REQUEST
                                && model.getPhase() == GameModel.GamePhase.FINISHED) {
                            listenerEndedForResult = true;
                            break;
                        }
                    } catch (PeerLeftException e) {
                        publish(OnlineGameMessage.peerLeft());
                        listenerEndedForResult = model.getPhase() == GameModel.GamePhase.FINISHED;
                        break;
                    } catch (java.io.IOException e) {
                        if (!isCancelled()) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void process(List<OnlineGameMessage> chunks) {
                for (OnlineGameMessage message : chunks) {
                    if (message.getType() == OnlineGameMessage.Type.MOVE) {
                        traiterCoupReseau(message.getMove());
                    } else if (message.getType() == OnlineGameMessage.Type.REMATCH_REQUEST) {
                        traiterDemandeRevanche();
                    } else if (message.getType() == OnlineGameMessage.Type.PEER_LEFT) {
                        traiterAdversaireParti();
                    } else if (message.getType() == OnlineGameMessage.Type.STARTING_PLAYER) {
                        traiterStartingPlayer(message.getStartingPlayerIndex());
                    }
                }
            }
            
            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                if (listenerEndedForResult || peerLeftHandled) {
                    return;
                }
                if (model.getPhase() != GameModel.GamePhase.FINISHED
                        && model.getPhase() != GameModel.GamePhase.RESOLVING) {
                    mainFrame.afficherAdversaireDeconnecte();
                }
            }
        };
        networkWorker.execute();
    }

    public void arreter() {
        if (networkWorker != null) {
            networkWorker.cancel(true);
        }
        if (aiTimer != null) {
            aiTimer.stop();
        }
        if (aiWorker != null) {
            aiWorker.cancel(true);
        }
        aiThinking = false;
        mainFrame.updateButtonsState();
    }

    private void traiterCoupReseau(OnlineMove move) {
        if (move == null || model.getJoueurCourant().getIndex() == localPlayerIndex) {
            return;
        }

        try {
            int f1Index = move.getFirstFlowerIndex();
            int f2Index = move.getSecondFlowerIndex();
            double posX = move.getPawnX();
            double posY = move.getPawnY();
            if (f1Index < 0 || f1Index >= model.getFleurs().size()
                    || f2Index < 0 || f2Index >= model.getFleurs().size()
                    || !Double.isFinite(posX) || !Double.isFinite(posY)) {
                return;
            }
            
            Flower f1 = model.getFleurs().get(f1Index);
            Flower f2 = model.getFleurs().get(f2Index);
            if (!model.estLigneValide(f1, f2) || !isRemotePawnPositionValid(f1, f2, posX, posY)) {
                return;
            }
            
            Pawn pionLibre = null;
            for (Pawn pw : model.getJoueurCourant().getPawns()) {
                if (!pw.isPlaced()) { pionLibre = pw; break; }
            }
            final Pawn finalPionLibre = pionLibre;
            
            boardPanel.montrerIntentionPlacement(f1, f2, posX, posY, () -> {
                boolean success = model.placerPionEtCapturer(f1, f2, posX, posY);
                if (success && finalPionLibre != null) {
                    boardPanel.jouerAnimationPionDistant(finalPionLibre, f1, f2, this::onPlacementAnimationFinished);
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private boolean isRemotePawnPositionValid(Flower f1, Flower f2, double posX, double posY) {
        if (!Double.isFinite(posX) || !Double.isFinite(posY)) {
            return false;
        }

        Line2D.Double segment = new Line2D.Double(f1.getX(), f1.getY(), f2.getX(), f2.getY());
        return segment.ptSegDist(posX, posY) <= GameModel.HITBOX_RADIUS;
    }

    private void traiterDemandeRevanche() {
        peerRematchRequested = true;
        if (onlineResultDialog != null) {
            onlineResultDialog.markPeerRematchRequested();
        }
    }

    private void traiterAdversaireParti() {
        peerLeftHandled = true;
        if (model.getPhase() == GameModel.GamePhase.FINISHED || onlineResultDialog != null) {
            peerLeftAfterResult = true;
            if (onlineResultDialog != null) {
                onlineResultDialog.markPeerLeft();
            }
            return;
        }
        mainFrame.afficherAdversaireDeconnecte();
    }

    private void traiterStartingPlayer(int starterIndex) {
        model.setStartingPlayer(starterIndex);
        boardPanel.afficherToastTour();
        declencherCoupIASiNecessaire();
    }

    private boolean demanderRevancheEnLigne() {
        if (localRematchRequested) {
            return true;
        }

        localRematchRequested = onlineChannel.sendRematchRequest();
        return localRematchRequested;
    }

    public boolean isAiMode() {
        return p1IsAi || p2IsAi;
    }

    public boolean isAiTurn() {
        int currentIdx = model.getJoueurCourant().getIndex();
        if (currentIdx == 0) return p1IsAi;
        if (currentIdx == 1) return p2IsAi;
        return false;
    }

    public boolean isAiThinking() {
        return aiThinking;
    }

    public boolean isLocalInputBlocked() {
        if (onlineChannel != null && model.getJoueurCourant().getIndex() != localPlayerIndex) {
            return true;
        }
        return isAiTurn() || aiThinking;
    }

    public boolean onPlacementValid(Flower f1, Flower f2, double posX, double posY) {
        if (onlineChannel != null && model.getJoueurCourant().getIndex() != localPlayerIndex) {
            return false;
        }
        
        boolean success = model.placerPionEtCapturer(f1, f2, posX, posY);
        if (success && onlineChannel != null) {
            int f1Index = model.getFleurs().indexOf(f1);
            int f2Index = model.getFleurs().indexOf(f2);
            onlineChannel.sendMove(new OnlineMove(f1Index, f2Index, posX, posY));
        }
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

    public void annulerCoup() {
        if (onlineChannel != null) return;
        if (model.getPhase() != GameModel.GamePhase.PLACING) return;
        if (!model.peutAnnuler()) return;

        if (aiThinking) {
            if (aiTimer != null) aiTimer.stop();
            if (aiWorker != null) aiWorker.cancel(true);
            aiThinking = false;
            mainFrame.updateButtonsState();
        }
        if (!model.peutAnnuler()) return;

        model.annulerDernierCoup();
        boardPanel.onUndoPerformed();
        declencherCoupIASiNecessaire();
    }

    public void refaireCoup() {
        if (onlineChannel != null) return;
        if (model.getPhase() != GameModel.GamePhase.PLACING) return;
        if (!model.peutRefaire()) return;

        if (aiThinking) {
            if (aiTimer != null) aiTimer.stop();
            if (aiWorker != null) aiWorker.cancel(true);
            aiThinking = false;
            mainFrame.updateButtonsState();
        }
        if (!model.peutRefaire()) return;

        model.refaireDernierCoup();
        boardPanel.onRedoPerformed();
        declencherCoupIASiNecessaire();
    }

    private void schedulerCoupIA() {
        if (aiThinking) return;
        aiThinking = true;
        mainFrame.updateButtonsState();

        aiTimer = new Timer(AI_THINK_DELAY_MS, e -> {
            aiWorker = new SwingWorker<>() {
                @Override
                protected AIMove doInBackground() {
                    int activePlayerIndex = model.getJoueurCourant().getIndex();
                    int activeAiDepth = (activePlayerIndex == 0) ? p1AiDepth : p2AiDepth;
                    if (activeAiDepth <= 1) {
                        return choisirCoupSimple(false, activePlayerIndex);
                    }
                    if (activeAiDepth == 2) {
                        return choisirCoupSimple(true, activePlayerIndex);
                    }
                    Minimax minimax = new Minimax(model, activePlayerIndex, activeAiDepth, 2000);
                    return minimax.findBestMove();
                }

                @Override
                protected void done() {
                    if (isCancelled()) return;
                    aiThinking = false;
                    mainFrame.updateButtonsState();
                    try {
                        AIMove move = get();
                        if (move != null) {
                            jouerCoupIA(move);
                        }
                    } catch (Exception ex) {
                        if (!(ex instanceof java.util.concurrent.CancellationException)) {
                            ex.printStackTrace();
                        }
                    }
                }
            };
            aiWorker.execute();
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private AIMove choisirCoupSimple(boolean greedy, int activePlayerIndex) {
        lacuna.model.AI.AIState state = lacuna.model.AI.AIState.fromModel(model);
        List<AIMove> moves = new lacuna.model.AI.DefaultAIMoveGenerator(true).generateMoves(state);
        if (moves.isEmpty()) return null;

        lacuna.model.AI.IAIEvaluator evaluator = new lacuna.model.AI.DefaultAIEvaluator();

        if (!greedy) {
            java.util.Collections.shuffle(moves);
            for (AIMove move : moves) {
                if (evaluator.quickEval(state, move, activePlayerIndex) > 0) {
                    return move;
                }
            }
            return moves.get(0);
        }

        AIMove bestMove = moves.get(0);
        double bestScore = evaluator.quickEval(state, bestMove, activePlayerIndex);
        for (int i = 1; i < moves.size(); i++) {
            AIMove move = moves.get(i);
            double score = evaluator.quickEval(state, move, activePlayerIndex);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
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
        
        final Pawn finalPionLibre = pionLibre;

        boardPanel.montrerIntentionPlacement(f1, f2, move.pawnX, move.pawnY, () -> {
            boolean success = model.placerPionEtCapturer(f1, f2, move.pawnX, move.pawnY);
            if (success && finalPionLibre != null) {
                boardPanel.jouerAnimationPionIA(finalPionLibre, f1, f2, this::onPlacementAnimationFinished);
            }
        });
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

        GameResultDialog.PlayerFlowers[] resultats = new GameResultDialog.PlayerFlowers[]{
            new GameResultDialog.PlayerFlowers(model.getJoueurs()[0].getName(), j1Fleurs),
            new GameResultDialog.PlayerFlowers(model.getJoueurs()[1].getName(), j2Fleurs)
        };
        String texteVainqueur = vainqueur != null ? "Vainqueur : " + vainqueur.getName() : "Egalite";

        if (onlineChannel != null) {
            afficherResultatEnLigne(resultats, texteVainqueur);
            return;
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

    private void afficherResultatEnLigne(GameResultDialog.PlayerFlowers[] resultats, String texteVainqueur) {
        onlineResultDialog = new OnlineGameResultDialog(
            mainFrame,
            "Resultats",
            resultats,
            texteVainqueur,
            this::demanderRevancheEnLigne
        );

        if (peerRematchRequested) {
            onlineResultDialog.markPeerRematchRequested();
        }
        if (peerLeftAfterResult) {
            onlineResultDialog.markPeerLeft();
        }

        OnlineGameResultDialog.Choice choix = onlineResultDialog.showDialog();
        onlineResultDialog = null;

        if (choix == OnlineGameResultDialog.Choice.REPLAY) {
            SwingUtilities.invokeLater(() -> mainFrame.relancerPartieEnLigneMemeSession(localPlayerIndex == 0));
        } else if (choix == OnlineGameResultDialog.Choice.WAIT_FOR_PLAYER) {
            SwingUtilities.invokeLater(mainFrame::attendreNouveauJoueurDansSession);
        } else {
            SwingUtilities.invokeLater(mainFrame::retourMenuPrincipal);
        }
    }

    public boolean isLocalMatch() {
        return networkSession == null;
    }

    public GameModel getModel() {
        return model;
    }

    public void demarrerPartieAvecStarter(int starterIndex) {
        model.setStartingPlayer(starterIndex);
        if (onlineChannel != null) {
            onlineChannel.sendStartingPlayer(starterIndex);
        }
        boardPanel.afficherToastTour();
        declencherCoupIASiNecessaire();
    }

    public boolean isHost() {
        return onlineChannel != null && localPlayerIndex == 0;
    }
}
