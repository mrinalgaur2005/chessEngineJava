package com.chess.pgn;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Rook;
 
public class UciParser {

    public Move parseMove(Board board, String uciMove) {
        // Parse UCI move string
        if (uciMove == null || uciMove.length() < 4) {
            return Move.NULL_MOVE;
        }

        String fromSquare = uciMove.substring(0, 2);
        String toSquare = uciMove.substring(2, 4);

        int fromCoordinate = getCoordinateFromUci(fromSquare);
        int toCoordinate = getCoordinateFromUci(toSquare);

        Piece movedPiece = board.getPieceAt(fromCoordinate);
        if (movedPiece == null) {
            return Move.NULL_MOVE;
        }

        // Handle castling moves
        if (uciMove.equals("e1g1") || uciMove.equals("e8g8")) {
            Rook rook = (Rook) board.getPieceAt(toCoordinate + 1); // Adjust based on rook position
            return new Move.KingSideCastleMove(board, movedPiece, toCoordinate, rook, rook.getPiecePosition(), toCoordinate - 1);
        }
        if (uciMove.equals("e1c1") || uciMove.equals("e8c8")) {
            Rook rook = (Rook) board.getPieceAt(toCoordinate - 2); // Adjust based on rook position
            return new Move.QueenSideCastleMove(board, movedPiece, toCoordinate, rook, rook.getPiecePosition(), toCoordinate + 1);
        }

        // Handle pawn promotion
        if (uciMove.length() == 5) {
            char promotionPiece = uciMove.charAt(4);
            Piece promotion = createPromotionPiece(promotionPiece, movedPiece.getAlliance(), toCoordinate);
            if (movedPiece instanceof Pawn) {
                Move decoratedMove = new Move.PawnMove(board, movedPiece, toCoordinate);
                return new Move.PawnPromotion(decoratedMove, promotion);
            }
        }

        // Handle en passant
        if (movedPiece instanceof Pawn && board.getEnPassentPawn() != null && board.getEnPassentPawn().getPiecePosition() == toCoordinate) {
            return new Move.PawnEnPassantAttackMove(board, movedPiece, toCoordinate, board.getEnPassentPawn());
        }

        // Regular attack move
        Piece attackedPiece = board.getPieceAt(toCoordinate);
        if (attackedPiece != null) {
            return new Move.AttackMove(board, movedPiece, toCoordinate, attackedPiece);
        }

        // Regular piece move
        Move move = new Move.MajourMove(board, movedPiece, toCoordinate);
        System.out.println(move.toString());
        return new Move.MajourMove(board, movedPiece, toCoordinate);
    }

    // Helper method to create a promotion piece
    private static Piece createPromotionPiece(char promotionPiece, Alliance alliance, int destinationCoordinate) {
        switch (promotionPiece) {
            case 'q':
                return new Queen(destinationCoordinate, alliance);
            case 'r':
                return new Rook(destinationCoordinate, alliance);
            case 'b':
                return new Bishop(destinationCoordinate, alliance);
            case 'n':
                return new Knight(destinationCoordinate, alliance);
            default:
                throw new IllegalArgumentException("Invalid promotion piece: " + promotionPiece);
        }
    }

    // Convert UCI square notation (e.g., "e2") to the corresponding board coordinate
    public static int getCoordinateFromUci(String uciSquare) {
        char column = uciSquare.charAt(0);
        char row = uciSquare.charAt(1);

        int columnIndex = column - 'a';
        int rowIndex = 8 - (row - '0'); // Flip row for internal representation

        return rowIndex * 8 + columnIndex;
    }
}
