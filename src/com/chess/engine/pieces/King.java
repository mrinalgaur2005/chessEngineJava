package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.google.common.collect.ImmutableList;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;

public class King extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = {-9,-8,-7,-1,1,7,8,9};

    private final boolean KingSideCastleCapable;
    private final boolean QueenSideCastleCapable;
    private final boolean isCasteled;

    public King(final int piecePosition,final Alliance pieceAlliance,
                final boolean KingSideCastleCapable,final boolean QueenSideCastleCapable){
        super(PieceType.KING,piecePosition, pieceAlliance,true);
        this.isCasteled=false;
        this.KingSideCastleCapable=KingSideCastleCapable;
        this.QueenSideCastleCapable=QueenSideCastleCapable;
    }
    public King(final Alliance pieceAlliance,
                final int piecePosition,
                final boolean isFirstMove,
                final boolean isCasteled,
                final boolean KingSideCastleCapable,
                final boolean QueenSideCastleCapable){
                super(PieceType.KING, piecePosition, pieceAlliance, isFirstMove);
                this.isCasteled=isCasteled;
                this.KingSideCastleCapable=KingSideCastleCapable;
                this.QueenSideCastleCapable=QueenSideCastleCapable;
    }

    public boolean isKingSideCastleCapable(){
        return this.KingSideCastleCapable;
    }
    public boolean isQueenSideCastleCapable(){
        return this.QueenSideCastleCapable;
    }

    public boolean isCasteled(){
        return this.isCasteled;
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        
        for(final int currentCandidateOffest:CANDIDATE_MOVE_COORDINATES){
            int candidateDestinationCoordinate;
            candidateDestinationCoordinate =this.piecePosition+currentCandidateOffest;

            if(isFirstColumnExclusion(this.piecePosition, currentCandidateOffest) || 
            isEightColumnExclusion(this.piecePosition, currentCandidateOffest)){
               continue; 
            }

            if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                if(!candidateDestinationTile.isTileOccupied()){
                    legalMoves.add(new Move.MajourMove(board,this,candidateDestinationCoordinate));
                }
                else{
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance pieceAlliance = pieceAtDestination.getAlliance();

                    if(this.pieceAlliance != pieceAlliance){
                        legalMoves.add(new Move.MajourAttackMove(board,this,candidateDestinationCoordinate,pieceAtDestination));
                    }
                }
            }

        }
        return ImmutableList.copyOf(legalMoves);
    }


    @Override
    public String toString(){
        return PieceType.KING.toString();
    }
    @Override
    public King movePiece(final Move move) {
        return new King(move.getMovePiece().getAlliance(),move.getDestinationCoordinate(),
        false,move.isCastlingMove(),
        false,false);
    }
    public static boolean isFirstColumnExclusion(final int currentPosition,final int candidateOffset){
        return BoardUtils.FIRST_COLUMN[currentPosition] && ((candidateOffset == -9) || (candidateOffset == -1) || 
        (candidateOffset == 7));
    }
    public static boolean isEightColumnExclusion(final int currentPosition,final int candidateOffset){
        return BoardUtils.EIGHT_COLUMN[currentPosition] && ((candidateOffset == -7) || (candidateOffset == 1) || 
        (candidateOffset == 9));
    }
}
