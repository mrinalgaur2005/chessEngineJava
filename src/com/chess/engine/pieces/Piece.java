package com.chess.engine.pieces;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    Piece(final PieceType pieceType,final int piecePosition,final Alliance pieceAlliance,final boolean isFirstMove){
        this.pieceAlliance = pieceAlliance;
        this.piecePosition=piecePosition;
        this.isFirstMove=isFirstMove;
        this.pieceType=pieceType;
        this.cachedHashCode=computeHashCode();
    }


    private int computeHashCode(){
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result +(isFirstMove ? 1 : 0);
        return result; 
    }
    @Override
    public boolean equals(final Object other){
        if(this == other){
            return true;
        }
        if(!(other instanceof Piece)){
            return false;
        }
        final Piece otherPiece = (Piece) other;
        return piecePosition == otherPiece.getPiecePosition() && pieceType == otherPiece.getPieceType() &&
                pieceAlliance == otherPiece.getAlliance() && isFirstMove == otherPiece.isFirstMove();
    }

    @Override
    public int hashCode(){
        return this.cachedHashCode;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);
    public abstract Piece movePiece(Move move);

    public Alliance getAlliance(){
        return this.pieceAlliance;
    }

    public boolean isFirstMove(){
        return isFirstMove;
    }

    public int getPiecePosition(){
        return this.piecePosition;
    }

    public PieceType getPieceType(){
        return this.pieceType;
    }

    public int getPieceValue(){
        return this.pieceType.getPieceValue();
    }

    public abstract int locationBonus();

    public enum PieceType {

        PAWN(100,"p") {
            @Override
            public boolean isKing() {
                return false;
            }

            @Override
            public boolean isRook(){
                return false;
            }
            @Override
            public void adjustValueBasedOnPosition(int positionVal){
                this.pieceValue+=positionVal;
            }
        },
        KNIGHT(320,"n") {
            @Override
            public boolean isKing() {
               return false;
            }
            @Override
            public boolean isRook(){
                return false;
            }
            @Override
            public void adjustValueBasedOnPosition(int positionVal){
                this.pieceValue+=positionVal;
            }
        },
        BISHOP(330,"b") {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook(){
                return false;
            }
            @Override
            public void adjustValueBasedOnPosition(int positionVal){
                this.pieceValue+=positionVal;
            }
        },
        ROOK(500,"r") {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook(){
                return true;
            }
            @Override
            public void adjustValueBasedOnPosition(int positionVal){
                this.pieceValue+=positionVal;
            }
        },
        QUEEN(900,"q") {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook(){
                return false;
            }
            @Override
            public void adjustValueBasedOnPosition(int positionVal){
                this.pieceValue+=positionVal;
            }
        },
        KING(20000,"k") {
            @Override
            public boolean isKing() {
                return true;
            }
            @Override
            public boolean isRook(){
                return false;
            }
        };
    
        private final String pieceName;
        protected int pieceValue;
    
        PieceType(final int pieceValue,final String pieceName) {
            this.pieceName = pieceName;
            this.pieceValue = pieceValue;
        }
        
        @Override
        public String toString() {
            return this.pieceName;
        }

        public abstract boolean isKing();

        public  abstract boolean isRook();

        public int getPieceValue(){
            return this.pieceValue;
        }

        public void adjustValueBasedOnPosition(int positionVal){}
        
    }

    

}
