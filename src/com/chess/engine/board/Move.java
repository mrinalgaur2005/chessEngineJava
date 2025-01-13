package com.chess.engine.board;

import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;

import java.util.Objects;

import com.chess.engine.board.Board.Builder;

public abstract class Move {

    protected final Board board;
    protected final Piece movedPiece;
    protected final int destinationCoordinate;
    protected final boolean isFirstMove;

    public static final Move NULL_MOVE = new NullMove();
    
    
    private Move(final Board board,
    final Piece movedPiece,
    final int destinationCoordinate){
        this.board=board;
        this.movedPiece=movedPiece;
        this.destinationCoordinate=destinationCoordinate;
        this.isFirstMove=movedPiece.isFirstMove();
    }

    private Move(final Board board,
    final int destinationCoordinate){
        this.board=board;
        this.destinationCoordinate=destinationCoordinate;
        this.movedPiece=null;
        this.isFirstMove=false;
    }


    @Override
    public int hashCode(){
        final int prime = 31;
        int result =1;
        
        result=prime*result +this.destinationCoordinate;
        result = prime * result + this.movedPiece.hashCode();
        result=prime*result +this.movedPiece.getPiecePosition();
        return result;

    }

    @Override
    public boolean equals(final Object other){
        if(this == other){
            return true;
        }
        if(!(other instanceof Move)){
            return false;
        }
        final Move otherMove = (Move)other;

        return  getCurrentCoordinate() == otherMove.getCurrentCoordinate() &&
                getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
                getMovePiece().equals(otherMove.getMovePiece());
    }
    
    public int getCurrentCoordinate(){
        return this.getMovePiece().getPiecePosition();
    }

    public int getDestinationCoordinate(){
        return this.destinationCoordinate;
    }
    
    public Piece getMovePiece(){
        return this.movedPiece;
    }

    public boolean isAttack(){
        return false;
    }

    public boolean isCastlingMove(){
        return false;
    }

    public Piece getAttackedPiece(){
        return null;
    }

    public Board getBoard(){
        return this.board;
    }

    public Board execute() {
        final Board.Builder builder = new Builder();
        this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movedPiece.equals(piece)).forEach(builder::setPiece);
        this.board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
        builder.setPiece(this.movedPiece.movePiece(this));
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
        builder.setMoveTransition(this);
        return builder.build();
    }

    
    public static final class MajourMove extends Move{
        public MajourMove(Board board,
        Piece movedPiece,
        int destinationCoordinate){
            super(board, movedPiece, destinationCoordinate);
        } 
        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof MajourMove && super.equals(other);
        }
        
        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + 
            BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }
    public static  class AttackMove extends Move{
        
        final Piece attackedPiece;
        
        public AttackMove(final Board board,
        final Piece movedPiece,
        final int destinationCoordinate,
        final Piece attackedPiece){
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece=attackedPiece;
        }
        
        
        @Override
        public boolean isAttack(){
            return true;
        }
        
        @Override
        public Piece getAttackedPiece(){
            return this.attackedPiece;
        }
        
        @Override
        public int hashCode(){
            return this.attackedPiece.hashCode() + super.hashCode();
        }
        
        @Override
        public boolean equals(final Object other){
            if(this == other){
                return true;
            }
            if(!(other instanceof AttackMove)){
                return false;
            }
            final AttackMove otherAttackMove = (AttackMove)other;
            
            return super.equals(otherAttackMove) && getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }
    }
    
    public static class MajourAttackMove extends AttackMove{
        public MajourAttackMove(final Board board,
        final Piece pieceMoved,
        final int destinationCoordinate,
        final Piece pieceAttacked
        ){
            super(board, pieceMoved, destinationCoordinate, pieceAttacked);
        }

        @Override
        public boolean equals(final Object other){
            return this == other || other  instanceof MajourAttackMove && super.equals(other);
        }

        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()).substring(0, 1) + "x" + 
            BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnPromotion extends PawnMove{

        final Move decoratedMove;
        final Pawn promotedPawn;
        final Piece promtionPiece;
        public PawnPromotion(final Move decoratedMove,
                            final Piece promotionPiece){
            super(decoratedMove.getBoard(),decoratedMove.getMovePiece(),decoratedMove.getDestinationCoordinate());
            this.decoratedMove=decoratedMove;
            this.promotedPawn =(Pawn)decoratedMove.getMovePiece();
            this.promtionPiece=promotionPiece;
        }
        @Override
        public Board execute(){

            final Board pawnMovedBoard = this.decoratedMove.execute();
            final Board.Builder builder = new Builder();
            pawnMovedBoard.currentPlayer().getActivePieces().stream().filter(piece -> !this.promotedPawn.equals(piece)).forEach(builder::setPiece);
            pawnMovedBoard.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
            builder.setPiece(this.promtionPiece.movePiece(this));
            builder.setMoveMaker(pawnMovedBoard.currentPlayer().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }
        @Override
        public boolean isAttack(){
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece(){
            return this.decoratedMove.getAttackedPiece();
        }

        @Override
        public String toString(){
            return decoratedMove.toString() + "=" + promtionPiece.getPieceType().toString().charAt(0);
        }


        @Override
        public int hashCode(){
            return decoratedMove.hashCode() +(31 + promotedPawn.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            final PawnPromotion that = (PawnPromotion) o;
            return Objects.equals(decoratedMove, that.decoratedMove) && Objects.equals(promotedPawn, that.promotedPawn) && Objects.equals(promtionPiece, that.promtionPiece);
        }
    }
    public static class PawnMove extends Move{
        public PawnMove(Board board,
        Piece movedPiece,
        int destinationCoordinate){
            super(board, movedPiece, destinationCoordinate);
            
        }
        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof PawnMove && super.equals(other);
        }
        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + 
            BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }

    public static  class PawnAttackMove extends AttackMove{
        public PawnAttackMove(Board board,
        Piece movedPiece,
        int destinationCoordinate,
        final Piece attackedPiece){
            super(board, movedPiece, destinationCoordinate,attackedPiece);

        }
        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof PawnAttackMove && super.equals(other);
        }
        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.movedPiece.getPiecePosition()).substring(0, 1) + "x" + 
                BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static final class PawnEnPassantAttackMove extends PawnAttackMove{
        public PawnEnPassantAttackMove(Board board,
        Piece movedPiece,
        int destinationCoordinate,
        final Piece attackedPiece){
            super(board, movedPiece, destinationCoordinate,attackedPiece);

        }

        @Override
        public boolean equals(final Object other){
            return this== other || other instanceof PawnEnPassantAttackMove && super.equals(other);

        }

        @Override
        public Board execute(){
            final Board.Builder builder = new Builder();
            this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movedPiece.equals(piece)).forEach(builder::setPiece);
            this.board.currentPlayer().getOpponent().getActivePieces().stream().filter(piece -> !piece.equals(this.getAttackedPiece())).forEach(builder::setPiece);
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }
        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.movedPiece.getPiecePosition()).substring(0, 1) + "x" + 
                BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + " e.p.";
        }

    }

    public static final class PawnJump extends Move{
        public PawnJump(Board board,
        Piece movedPiece,
        int destinationCoordinate){
            super(board, movedPiece, destinationCoordinate);

        }
        @Override
        public Board execute(){
            final Board.Builder builder = new Builder();
            this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movedPiece.equals(piece)).forEach(builder::setPiece);
            this.board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
            final Pawn movedPawn = (Pawn)this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();

        }
        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + 
            BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }
    static abstract class CastleMove extends Move{

        protected final Rook castleRook;
        protected final int castleRookStart;
        protected final int castleRookDestination;

        public CastleMove(final Board board,
        final Piece movedPiece,
        final int destinationCoordinate,
        final Rook castleRook,
        final int castleRookStart,
        final int castleRookDestination){
            super(board, movedPiece, destinationCoordinate);
            this.castleRook=castleRook;
            this.castleRookStart=castleRookStart;
            this.castleRookDestination=castleRookDestination;
        } 

        public Rook getCastleRook(){
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove(){
            return true;
        }
        @Override
        public Board execute(){
            final Board.Builder builder = new Builder();
            for (final Piece piece : this.board.getAllPieces()) {
                if (!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            //calling movePiece here doesn't work, we need to explicitly create a new Rook
            builder.setPiece(new Rook(this.castleRook.getAlliance(), this.castleRookDestination, false));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        @Override
        public int hashCode(){
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.castleRook.hashCode();
            result = prime *result +this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object other){
            if(this == other){
                return true;
            }
            if(!(other instanceof CastleMove)){
                return false;
            }
            final CastleMove otherCastleMove = (CastleMove)other;
            return super.equals(otherCastleMove) && this.castleRook == otherCastleMove.getCastleRook();
        }
    }

    public static final class KingSideCastleMove extends CastleMove{
        public KingSideCastleMove(Board board,
                                  Piece movedPiece,
                                  int destinationCoordinate,
                                  final Rook castleRook,
                                  final int castleRookStart,
                                  final int castleRookDestination){
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }
    
        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof KingSideCastleMove && super.equals(other);
        }
    
        @Override
        public String toString(){
            return this.getMovePiece().getAlliance().isWhite() ? "e1g1" : "e8g8";
        }
    }
    
    public static final class QueenSideCastleMove extends CastleMove{
        public QueenSideCastleMove(Board board,
        Piece movedPiece,
        int destinationCoordinate,
        final Rook castleRook,
        final int castleRookStart,
        final int castleRookDestination){
            super(board, movedPiece, destinationCoordinate,castleRook,castleRookStart,castleRookDestination);

        }
        @Override
        public String toString(){
            return this.getMovePiece().getAlliance().isWhite() ? "e1c1" : "e8c8";
        }
        @Override
        public boolean equals(final Object other){
            return this== other || other instanceof QueenSideCastleMove && super.equals(other);

        }
    }

    public static final class NullMove extends Move{
        public NullMove(){
            super(null, 65);
        }

        @Override
        public Board execute(){
            throw new RuntimeException("Null move cant be executed");
        }
        @Override
        public int getCurrentCoordinate() {
            return -1;
        }
    }

    public static class MoveFactory{

        private MoveFactory(){
            throw new RuntimeException("Cant instantiate MoveFactory");
        }

        public static Move getNullMove() {
            return MoveUtils.NULL_MOVE;
        }

        public static Move createMove(final Board board,final int currentCoordinate,
        final int destinationCoordinate){
            for(final Move move:board.getAllLegalMoves()){
                if(move.getCurrentCoordinate() == currentCoordinate &&
                move.getDestinationCoordinate() == destinationCoordinate){
                    return move;
                }
            }
            return NULL_MOVE;
        }
    }
    

}

