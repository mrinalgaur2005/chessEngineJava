package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.OpeningBook.OpeningBook;
import com.chess.pgn.FenUtilities;

import java.util.Optional;

public class MiniMax implements MoveStrategy {

    public final BoardEvaluator boardEvaluator;
    public final int searchDepth;
    private final OpeningBook openingBook;

    public MiniMax(final int searchDepth, String openingBookContent) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
        this.openingBook = new OpeningBook(openingBookContent);
    }

    @Override
    public String toString() {
        return "MiniMax with Opening Book";
    }

    @Override
    public Move execute(Board board) {

        final long startTime = System.currentTimeMillis();

        Move bestMove = null;
        int highestSeenVal = Integer.MIN_VALUE;
        int lowerSeenVal = Integer.MAX_VALUE;

        System.out.println(board.currentPlayer() + " Thinking with depth " + searchDepth);

        String positionFen = FenUtilities.createFENFromGame(board);
        
        // First check if there's a move in the opening book
        Optional<String> bookMove = openingBook.getBookMove(positionFen, 0.5); // Use a default weight
        if (bookMove.isPresent()) {
            bestMove = Move.fromString(bookMove.get()); // Convert the book move to a Move object
            System.out.println("Opening book move found: " + bestMove);
            return bestMove; // Early exit if we found a book move
        }

        // If no book move found, fall back to Minimax
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currValue = board.currentPlayer().getAlliance().isWhite()
                        ? min(moveTransition.getTransitionBoard(), searchDepth - 1)
                        : max(moveTransition.getTransitionBoard(), searchDepth - 1);

                System.out.println("Evaluating Move: " + move + " Score: " + currValue);

                if (board.currentPlayer().getAlliance().isWhite() && currValue > highestSeenVal) {
                    highestSeenVal = currValue;
                    bestMove = move;
                } else if (board.currentPlayer().getAlliance().isBlack() && currValue < lowerSeenVal) {
                    lowerSeenVal = currValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;

        System.out.println("Execution Time: " + executionTime + "ms");
        System.out.println("Best Move: " + bestMove);

        return bestMove;
    }

    private int min(final Board board, final int depth) {
        // Ensure depth doesn't go below 1 to avoid shallow search or infinite recursion
        if (depth <= 1 || isGameEndScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowerSeenVal = Integer.MAX_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currVal = max(moveTransition.getTransitionBoard(), depth - 1);
                if (currVal <= lowerSeenVal) {
                    lowerSeenVal = currVal;
                }
            }
        }
        return lowerSeenVal;
    }

    private int max(final Board board, final int depth) {
        // Ensure depth doesn't go below 1 to avoid shallow search or infinite recursion
        if (depth <= 1 || isGameEndScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestSeenVal = Integer.MIN_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currVal = min(moveTransition.getTransitionBoard(), depth - 1);
                if (currVal >= highestSeenVal) {
                    highestSeenVal = currVal;
                }
            }
        }
        return highestSeenVal;
    }

    private static boolean isGameEndScenario(final Board board) {
        return board.currentPlayer().isInCheckMate() || board.currentPlayer().isInStaleMate();
    }
}
