package com.chess.engine.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move.MoveFactory;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Rook;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.chess.pgn.FenUtilities;

public class Board {

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;

    private final Player currentPlayer;

    public final Pawn enPassantPawn;

    private final Move transitionMove;

    private Board(final Builder builder){
        this.gameBoard=createGameBoard(builder);
        this.whitePieces=calculateActivePieces(this.gameBoard,Alliance.WHITE);
        this.blackPieces=calculateActivePieces(this.gameBoard,Alliance.BLACK);
        this.enPassantPawn=builder.enPassantPawn;

        final Collection<Move> whiteStandardLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = calculateLegalMoves(this.blackPieces);

        this.whitePlayer= new WhitePlayer(this,whiteStandardLegalMoves,blackStandardLegalMoves);
        this.blackPlayer= new BlackPlayer(this,whiteStandardLegalMoves,blackStandardLegalMoves);
        this.currentPlayer= builder.nextMoveMaker.choosePlayer(this.whitePlayer,this.blackPlayer);

        this.transitionMove = builder.transitionMove != null ? builder.transitionMove : MoveFactory.getNullMove();
    }

    @Override
    public String toString(){
        final StringBuilder builder = new StringBuilder();
        for(int i=0;i<BoardUtils.NUM_TILES;i++){
            final String tileText = gameBoard.get(i).toString();
            builder.append(String.format("%3s", tileText));
            if((i+1)%BoardUtils.NUM_TILES_PER_ROW == 0){
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public Player whitePlayer(){
        return this.whitePlayer;
    }

    public Player blackPlayer(){
        return this.blackPlayer;
    }

    public Player currentPlayer(){
        return this.currentPlayer;
    }

    public Collection<Piece> getBlackPieces(){
        return this.blackPieces;
    }

    public Collection<Piece> getWhitePieces(){
        return this.whitePieces;
    }

    public Collection<Piece> getAllPieces() {
        return Stream.concat(this.whitePieces.stream(),
                             this.blackPieces.stream()).collect(Collectors.toList());
    }

    public Pawn getEnPassentPawn(){
        return this.enPassantPawn;
    }

    public Move getTransitionMove(){
        return this.transitionMove;
    }


    private Collection<Move> calculateLegalMoves(Collection<Piece> pieces){
        final List<Move> legalMoves =new ArrayList<>();

        for(final Piece piece: pieces){
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }
        return ImmutableList.copyOf(legalMoves);
    }

    public Tile getTile(final int tileCoordinate){
        return gameBoard.get(tileCoordinate);
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard,final Alliance alliance){
        final List<Piece> activePieces = new ArrayList<>();

        for(final Tile tile: gameBoard){
            if(tile.isTileOccupied()){
                final Piece piece = tile.getPiece();
                if(piece.getAlliance() == alliance){
                    activePieces.add(piece);
                }
            }
        }
        return ImmutableList.copyOf(activePieces);
    }

    private static List<Tile> createGameBoard(final Builder builder){

        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];
        for(int i=0;i<BoardUtils.NUM_TILES;i++){
            tiles[i]=Tile.createTile(i, builder.boardConfig.get(i));
        }
        return ImmutableList.copyOf(tiles);
    }

    public static Board createStaticBoard() {
        Board board =FenUtilities.createGameFromFEN("8/8/8/K2nk3/8/4b3/1q6/2q2q2 w - - 0 76");
        //rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
        return board;
    }
    
    public static class Builder{

        Map<Integer,Piece>boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;
        Move transitionMove;

        public Builder(){
            this.boardConfig=new HashMap<>();
        }

        public Builder setPiece(final Piece piece){
            this.boardConfig.put(piece.getPiecePosition(),piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker){
            this.nextMoveMaker = nextMoveMaker;
            return this;
        } 

        public Builder setMoveTransition(final Move transitionMove) {
            this.transitionMove = transitionMove;
            return this;
        }

        public Board build(){
            return new Board(this);
        }

        public void setEnPassantPawn(Pawn movedPawn) {
            this.enPassantPawn=movedPawn;
        }
    }

    public Iterable<Move> getAllLegalMoves() {
        return Iterables.unmodifiableIterable(Iterables.concat(this.whitePlayer.getLegalMoves(),this.blackPlayer.getLegalMoves()));
    }

}

