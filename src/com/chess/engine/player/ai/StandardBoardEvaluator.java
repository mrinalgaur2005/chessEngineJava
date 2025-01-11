package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Piece.PieceType;
import com.chess.engine.player.Player;

public final class StandardBoardEvaluator implements BoardEvaluator {

    private static final int CHECK_BONUS = 45;
    private static final int CHECKMATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 100;
    private static final int CASTLE_BONUS = 25;
    private static final int MOBILITY_MULTIPLIER = 5;
    private static final int ATTACK_MULTIPLIER = 1;
    private static final int TWO_BISHOPS_BONUS = 25;

    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board, board.whitePlayer(), depth) -
                scorePlayer(board, board.blackPlayer(), depth);
    }

    private int scorePlayer(final Board board, final Player player, final int depth) {
        return pieceValue(player) +
                attacks(player) +
                kingThreats(player, depth) +
                mobility(player) +
                check(player) +
                checkMate(player, depth) +
                castled(player) -
                stalemateRisk(player) -
                checkmateRisk(player);
    }

    private static int pieceValue(final Player player) {
        int pieceValuationScore = 0;
        int numBishops = 0;
        for (final Piece piece : player.getActivePieces()) {
            pieceValuationScore += piece.getPieceValue();
            if (piece.getPieceType() == PieceType.BISHOP) {
                numBishops++;
            }
        }
        return pieceValuationScore + (numBishops == 2 ? TWO_BISHOPS_BONUS : 0);
    }

    private static int mobility(final Player player) {
        return MOBILITY_MULTIPLIER * mobilityRatio(player);
    }

    private static int mobilityRatio(final Player player) {
        return player.getLegalMoves().isEmpty() ? 0 :
                (int) ((player.getLegalMoves().size() * 10.0f) / player.getOpponent().getLegalMoves().size());
    }

    private static int attacks(final Player player) {
        int attackScore = 0;
        for (final Move move : player.getLegalMoves()) {
            if (move.isAttack()) {
                final Piece movedPiece = move.getMovePiece();
                final Piece attackedPiece = move.getAttackedPiece();
                if (movedPiece.getPieceValue() <= attackedPiece.getPieceValue()) {
                    attackScore++;
                }
            }
        }
        return attackScore * ATTACK_MULTIPLIER;
    }

    private static int check(final Player player) {
        return player.getOpponent().isInCheck() ? CHECK_BONUS : 0;
    }

    private static int kingThreats(final Player player, final int depth) {
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS * depthBonus(depth) : check(player);
    }

    private static int checkMate(final Player player, final int depth) {
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS * depthBonus(depth) : 0;
    }

    private static int depthBonus(final int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    private static int castled(final Player player) {
        return player.isCasteled() ? CASTLE_BONUS : 0;
    }

    private static int stalemateRisk(final Player player) {
        return player.getLegalMoves().isEmpty() && !player.isInCheck() ? CHECKMATE_BONUS / 2 : 0;
    }

    private static int checkmateRisk(final Player player) {
        return player.isInCheck() && player.getLegalMoves().isEmpty() ? CHECKMATE_BONUS : 0;
    }
}
