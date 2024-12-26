package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.google.common.collect.ImmutableList;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;

public class Pawn extends Piece {

    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {8,16,7,9};

    public Pawn(final int piecePosition,final Alliance pieceAlliance){
        super(PieceType.PAWN,piecePosition, pieceAlliance);
    }
    @Override
    public Collection<Move> calculateLegalMoves(final Board board){

        final List<Move> legalMoves = new ArrayList<>();
        for(final int currentCandidateOffest:CANDIDATE_MOVE_VECTOR_COORDINATES){
            final int candidateDestinationCoordinate=this.piecePosition+(currentCandidateOffest)*(this.pieceAlliance.getDirection());

            if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                continue;
            }

            if(currentCandidateOffest == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                legalMoves.add(new Move.MajourMove(board, this, candidateDestinationCoordinate));
            }else if (currentCandidateOffest==16 && this.isFirstMove() && 
            (BoardUtils.SEVENTH_RAMK[this.piecePosition] && this.getAlliance().isBlack()) || 
            BoardUtils.SECOND_RANK[this.piecePosition] && this.getAlliance().isWhite()) {

                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection()*8);

                if(!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() && 
                !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    legalMoves.add(new Move.MajourMove(board, this, candidateDestinationCoordinate));
                }
            }else if(currentCandidateOffest == 7 &&
            !((BoardUtils.EIGHT_COLUMN[piecePosition] && this.pieceAlliance.isWhite())||
            (BoardUtils.FIRST_COLUMN[piecePosition] && this.pieceAlliance.isBlack()))){
                if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                    if(this.pieceAlliance != pieceOnCandidate.getAlliance()){
                        legalMoves.add(new Move.MajourMove(board, this, candidateDestinationCoordinate));

                    }
                }
                
            }else if(currentCandidateOffest==9 && 
            !((BoardUtils.FIRST_COLUMN[piecePosition] && this.pieceAlliance.isWhite())||
            (BoardUtils.EIGHT_COLUMN[piecePosition] && this.pieceAlliance.isBlack()))){
                if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                    if(this.pieceAlliance != pieceOnCandidate.getAlliance()){
                        legalMoves.add(new Move.MajourMove(board, this, candidateDestinationCoordinate));

                    }
                }
            }
        
        }
    return ImmutableList.copyOf(legalMoves);
    }
    @Override
    public String toString(){
        return PieceType.PAWN.toString();
    }
    @Override
    public Pawn movePiece(final Move move) {
        return new Pawn(move.getDestinationCoordinate(),move.getMovePiece().getAlliance());
    }
}
