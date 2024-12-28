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
        super(PieceType.PAWN,piecePosition, pieceAlliance,true);
    }
    public Pawn(final Alliance pieceAlliance,
                final int piecePosition,
                final boolean isFirstMove){
                super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }
    @Override
    public Collection<Move> calculateLegalMoves(final Board board){

        final List<Move> legalMoves = new ArrayList<>();
        for(final int currentCandidateOffest:CANDIDATE_MOVE_VECTOR_COORDINATES){
            final int candidateDestinationCoordinate=this.piecePosition+(currentCandidateOffest)*(this.pieceAlliance.getDirection());

            if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                continue;
            }
            //pawn promotion
            if(currentCandidateOffest == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                    legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board, this, candidateDestinationCoordinate)));
                }else{
                    legalMoves.add(new Move.PawnMove(board, this, candidateDestinationCoordinate));
                }
                
            }


            else if (currentCandidateOffest==16 && this.isFirstMove() && 
            ((BoardUtils.SEVENTH_RAMK[this.piecePosition] && this.getAlliance().isBlack()) || 
            (BoardUtils.SECOND_RANK[this.piecePosition] && this.getAlliance().isWhite()))) {

                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection()*8);
                //pawn jump
                if(!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() && 
                !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    legalMoves.add(new Move.PawnJump(board, this, candidateDestinationCoordinate));
                }

                
            //pawn attack move
            }else if(currentCandidateOffest == 7 &&
            !((BoardUtils.EIGHT_COLUMN[piecePosition] && this.pieceAlliance.isWhite())||
            (BoardUtils.FIRST_COLUMN[piecePosition] && this.pieceAlliance.isBlack()))){
                if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                    if(this.pieceAlliance != pieceOnCandidate.getAlliance()){
                        //pawn promotion
                        if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new Move.PawnPromotion(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate,pieceOnCandidate)));

                        } else {
                            legalMoves.add(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate,pieceOnCandidate));
                        }
                    }
                    //en passent
                }else if(board.getEnPassentPawn()!=null){
                    if(board.getEnPassentPawn().getPiecePosition() ==(this.piecePosition +(this.pieceAlliance.getOppositeDirection()))){
                        final Piece pieceOnCandidate = board.getEnPassentPawn();
                        if(this.pieceAlliance != pieceOnCandidate.getAlliance()){
                            legalMoves.add(new Move.PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }   
                
            }else if(currentCandidateOffest==9 && 
            !((BoardUtils.FIRST_COLUMN[piecePosition] && this.pieceAlliance.isWhite())||
            (BoardUtils.EIGHT_COLUMN[piecePosition] && this.pieceAlliance.isBlack()))){
                if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                    if(this.pieceAlliance != pieceOnCandidate.getAlliance()){
                        //pawn promotion
                        if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new Move.PawnPromotion(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate,pieceOnCandidate)));

                        } else {
                            legalMoves.add(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate,pieceOnCandidate));
                        }
                    }
                    //en passent
                }else if(board.getEnPassentPawn()!=null){
                    if(board.getEnPassentPawn().getPiecePosition() ==(this.piecePosition -(this.pieceAlliance.getOppositeDirection()))){
                        final Piece pieceOnCandidate = board.getEnPassentPawn();
                        if(this.pieceAlliance != pieceOnCandidate.getAlliance()){
                            legalMoves.add(new Move.PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
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

    //only promote to queen
    public Piece getPromotionPiece(){
        return new Queen(this.pieceAlliance,this.piecePosition,false);
    }
}
