package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

public class Rook extends Piece {

    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {-8,-1,1,8};

    public Rook(int piecePosition,Alliance pieceAlliance){
        super(PieceType.ROOK,piecePosition, pieceAlliance,true);
    }
    public Rook(final Alliance pieceAlliance,
                final int piecePosition,
                final boolean isFirstMove){
                super(PieceType.ROOK, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board){
        final List<Move> legalMoves = new ArrayList<>();

        for(final int candidateCoordinateOffset:CANDIDATE_MOVE_VECTOR_COORDINATES){
            int candidateDestinationCoordinate=this.piecePosition;

            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){

                if(isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
                    isEightColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset)    
                ){
                    break;
                }
                candidateDestinationCoordinate+=candidateCoordinateOffset;

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
                        break;
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }
    @Override
    public String toString(){
        return PieceType.ROOK.toString();
    }
    @Override
    public int locationBonus(){
        return this.pieceAlliance.rookBonus(this.piecePosition);
    }
    @Override
    public Rook movePiece(final Move move) {
        return PieceUtils.INSTANCE.getMovedRook(move.getMovePiece().getAlliance(), move.getDestinationCoordinate());
    }
    private static boolean isFirstColumnExclusion(final int currentPosition,final int candidateOffset){
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -1);
    }

    private static boolean isEightColumnExclusion(final int currentPosition,final int candidateOffset){
        return BoardUtils.EIGHT_COLUMN[currentPosition] && (candidateOffset == 1);
    }
}
