package com.chess.pgn;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import com.chess.engine.pieces.Piece;

public class UciParser {

    public static Move parseMove(Board board, String uciMove) {
        // Check for castling moves
        if (uciMove.equals("e1g1") || uciMove.equals("e8g8")) {
            return new KingSideCastleMove(board, null, 0, null, 0, 0); // Adjust as needed
        }
        if (uciMove.equals("e1c1") || uciMove.equals("e8c8")) {
            return new QueenSideCastleMove(board, null, 0, null, 0, 0); // Adjust as needed
        }

        // Check for pawn promotion
        if (uciMove.length() == 5) {
            String fromSquare = uciMove.substring(0, 2);
            String toSquare = uciMove.substring(2, 4);
            char promotionPiece = uciMove.charAt(4);
            
            // Handle pawn promotion logic
            // Example: "e7e8q" -> Pawn from e7 to e8 and promotes to Queen
            // You can create a move object for pawn promotion (create a `PawnPromotionMove` class)
        }

        // Regular piece move (non-castle, non-promotion)
        String fromSquare = uciMove.substring(0, 2);
        String toSquare = uciMove.substring(2, 4);

        // Convert the square coordinates to internal position representations
        int fromCoordinate = getCoordinateFromUci(fromSquare);
        int toCoordinate = getCoordinateFromUci(toSquare);

        // Handle the logic of which piece is making the move
        // Example for a pawn move (from `e2e4`):
        Piece pieceMoved = board.getPieceAt(fromCoordinate); // Get piece at starting square
        
        // Create and return the move object
        return new Move.PawnMove(board, pieceMoved, toCoordinate); // Adjust for other piece types
    }

    // Convert UCI square notation (e.g., "e2") to the corresponding board coordinate
    public static int getCoordinateFromUci(String uciSquare) {
        char column = uciSquare.charAt(0);
        char row = uciSquare.charAt(1);

        int columnIndex = column - 'a';
        int rowIndex = 8 - (row - '0');  // Reverse the row because the board is flipped

        return rowIndex * 8 + columnIndex;
    }
}

