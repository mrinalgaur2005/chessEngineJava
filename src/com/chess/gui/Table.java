package com.chess.gui;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;


import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveStrategy;
import com.google.common.collect.Lists;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

public class Table extends Observable{
    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private Board chessBoard;
    private BoardDirection boardDirection;
    private final GameSetup gameSetup;
    private Move computerMove;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;

    private boolean highlightLegalMoves;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600,600);
    private final static Dimension BOARD_PANNEL_DIMENSION = new Dimension(400,350);
    private final static Dimension TILE_PANNEL_DIMENSION = new Dimension(60,60);

    private static final Color lightTileColor = Color.decode("#FFFACD"); 
    private static final Color darkTileColor = Color.decode("#593E1A");

    private static String defaultPiecesImagePath = "public/";


    private static final Table INSTANCE = new Table();


    private Table(){
        final JMenuBar tableMenuBar=createTableMenuBar();
        this.chessBoard = Board.createStaticBoard();
        this.gameFrame = new JFrame("jChess");
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.boardPanel=new BoardPanel();
        this.highlightLegalMoves=false;
        this.addObserver(new TableGameAIWatcher());
        this.gameFrame.add(this.boardPanel,BorderLayout.CENTER);
        this.boardDirection= BoardDirection.NORMAL;
        this.gameSetup= new GameSetup(this.gameFrame, true);
        this.gameFrame.setVisible(true);
    }
        
    public static Table get(){
        return INSTANCE;
    }

    public void show() {
        Table.get().getBoardPannel().drawBoard(Table.get().chessBoard);
    }

    public GameSetup getGameSetup(){
        return this.gameSetup;
    }

    private Board getGameBoard(){
        return this.chessBoard;
    }
    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferenceMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("file");
        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                System.out.println("PGN file open here");
            }
        });
        fileMenu.add(openPGN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }
        });

        fileMenu.add(exitMenuItem);
        return fileMenu;
    }

    private JMenu createPreferenceMenu(){
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e){
                boardDirection=boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);

        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighliterCheckbox= new JCheckBoxMenuItem("Highlight Legal Moves",false);

        legalMoveHighliterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                highlightLegalMoves = legalMoveHighliterCheckbox.isSelected();
            }
        });
        preferencesMenu.add(legalMoveHighliterCheckbox);
        return preferencesMenu;


        
    }

    private JMenu createOptionsMenu(){
        final JMenu optionsMenu = new JMenu("Options");
        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });

        optionsMenu.add(setupGameMenuItem);
        return optionsMenu;
    }

    private void setupUpdate(final GameSetup gameSetup){
        setChanged();
        notifyObservers(gameSetup);
    }

    private static class TableGameAIWatcher implements Observer{
        @Override

        public void update(final Observable o,final Object arg){
            if(Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
            !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
            !Table.get().getGameBoard().currentPlayer().isInStaleMate()){
                //ai will be made here
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }
            if(Table.get().getGameBoard().currentPlayer().isInCheckMate()){
                System.out.println("Game Over "+ Table.get().getGameBoard().currentPlayer() + "checkmate");
            }
            if(Table.get().getGameBoard().currentPlayer().isInStaleMate()){
                System.out.println("Game Over "+ Table.get().getGameBoard().currentPlayer() + "stalkoemate");
            }

        }
    }

    public void updateGameboard(final Board board){
        this.chessBoard=board;
    }
    public void updateComputerMove(final Move move){
        this.computerMove = move;
    }
    private BoardPanel getBoardPannel(){
        return this.boardPanel;
    }
    private void moveMadeUpdate(final PlayerType playerType){
        setChanged();
        notifyObservers(playerType);
    }

    private static class AIThinkTank extends SwingWorker<Move,String>{

        private AIThinkTank(){

        }

        @Override
        protected Move doInBackground() throws Exception{

            final MoveStrategy miniMax = new MiniMax(4 );

            final Move bestMove = miniMax.execute(Table.get().getGameBoard());
            return bestMove;
        }

        @Override
        public void done(){
            try {
                final Move bestMove=get();
                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameboard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getBoardPannel().drawBoard(Table.get().getGameBoard());;
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public enum BoardDirection{
        NORMAL{
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED{
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;

        BoardPanel(){
            super(new GridLayout(8, 8));
            this.boardTiles=new ArrayList<>();
            for(int i=0;i <BoardUtils.NUM_TILES;i++){
                final TilePanel tilePanel = new TilePanel(this,i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANNEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board){
            removeAll();
            for(final TilePanel tilePanel: boardDirection.traverse(boardTiles)){
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }


    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }
    private class TilePanel extends JPanel{

        private final int tileId;

        TilePanel(final BoardPanel boardPanel,final int tileId){
            super(new GridBagLayout());
            this.tileId=tileId;
            setPreferredSize(TILE_PANNEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);
            // highlightLegalMoves(chessBoard);
            validate();

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e){
                    if(isRightMouseButton(e)){

                        sourceTile=null;
                        destinationTile=null;
                        humanMovedPiece=null;
                        
                    }else if(isLeftMouseButton(e)){
                        //first click
                        if(sourceTile == null){
                            sourceTile = chessBoard.getTile(tileId);
                            humanMovedPiece = sourceTile.getPiece();
                            if(humanMovedPiece == null){
                                sourceTile = null;
                            }
                        }
                        //second click
                        else{
                            destinationTile = chessBoard.getTile(tileId);
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordiate(), 
                                                                            destinationTile.getTileCoordiate());

                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if(transition.getMoveStatus().isDone()){
                                chessBoard=transition.getTransitionBoard();
                                
                            }
                            sourceTile=null;
                            destinationTile=null;
                            humanMovedPiece=null;
                        }
                        SwingUtilities.invokeLater(()->{
                            if(gameSetup.isAIPlayer(chessBoard.currentPlayer())){
                                Table.get().moveMadeUpdate(PlayerType.HUMAN);
                            }
                            boardPanel.drawBoard(chessBoard);
                        });

                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            });
            validate();
        }

        public void drawTile(final Board board){
            assignTileColor();
            assignTilePieceIcon(board);
            // highlightLegalMoves(board);
            validate();
            repaint();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileId).isTileOccupied()) {
                try {
                    final BufferedImage originalImage = ImageIO.read(new File(defaultPiecesImagePath + 
                            board.getTile(this.tileId).getPiece().getAlliance().toString().substring(0, 1).toLowerCase() +
                            board.getTile(this.tileId).getPiece().toString().toLowerCase() + ".png"));

                    final Image scaledImage = originalImage.getScaledInstance(
                    TILE_PANNEL_DIMENSION.width,
                    TILE_PANNEL_DIMENSION.height,
                    Image.SCALE_SMOOTH);
                    add(new JLabel(new ImageIcon(scaledImage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            validate();
            repaint(); 
        }
        
        // private void highlightLegalMoves(final Board board){
        //     if (highlightLegalMoves) {
        //         for(final Move move: pieceLegalMoves(board)){
        //             if(move.getDestinationCoordinate() == this.tileId){
        //                 try {
        //                     System.out.println("i am here");
        //                     add( new JLabel(new ImageIcon(ImageIO.read(new File("public/misc/green_dot.png")))));
        //                 } catch (Exception e) {
        //                     e.printStackTrace();
        //                 }
        //             }
        //         }
        //     }
        // }

        // private Collection<Move> pieceLegalMoves(final Board board){
        //     if(humanMovedPiece != null && humanMovedPiece.getAlliance()==board.currentPlayer().getAlliance()){
        //         return humanMovedPiece.calculateLegalMoves(board);
        //     }
        //     return Collections.emptyList();
        // }

        private void assignTileColor() {
            if(BoardUtils.EIGHT_RANK[this.tileId] ||
                BoardUtils.SIXTH_RANK[this.tileId] ||
                BoardUtils.FOURTH_RANK[this.tileId] ||
                BoardUtils.SECOND_RANK[this.tileId]){
                    setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            }
            else if(BoardUtils.SEVENTH_RAMK[this.tileId] ||
            BoardUtils.FIFTH_RANK[this.tileId] ||
            BoardUtils.THIRD_RANK[this.tileId] ||
            BoardUtils.FIRST_RANK[this.tileId]){
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }
        }
    }
}
