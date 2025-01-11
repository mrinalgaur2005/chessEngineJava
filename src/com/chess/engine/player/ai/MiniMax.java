package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

public class MiniMax implements MoveStrategy{

    public final BoardEvaluator boardEvaluator;
    public final int searchDepth;

    public MiniMax(final int searchDepth){
        this.boardEvaluator=new StandardBoardEvaluator();
        this.searchDepth=searchDepth;
    }

    @Override
    public String toString(){
        return "miniMax";
    }
    
    @Override
public Move execute(Board board) {

    final long startTime = System.currentTimeMillis();

    Move bestMove = null;
    int highestSeenVal = Integer.MIN_VALUE;
    int lowerSeenVal = Integer.MAX_VALUE;

    final int depth = this.searchDepth;

    System.out.println(board.currentPlayer() + " Thinking with depth " + depth);

    for (final Move move : board.currentPlayer().getLegalMoves()) {
        final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
        if (moveTransition.getMoveStatus().isDone()) {
            final int currValue = board.currentPlayer().getAlliance().isWhite()
                ? min(moveTransition.getTransitionBoard(), depth - 1)
                : max(moveTransition.getTransitionBoard(), depth - 1);

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


    private static boolean isGameEndScenario(final Board board){
        return board.currentPlayer().isInCheckMate() || 
                board.currentPlayer().isInStaleMate();
    }

    public int min(final Board board,final int depth){
        if(depth == 0 || isGameEndScenario(board)){
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowerSeenVal = Integer.MAX_VALUE;
        for(final Move move: board.currentPlayer().getLegalMoves()){
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()){
                final int currVal = max(moveTransition.getTransitionBoard(),depth-1);
                if(currVal <= lowerSeenVal){
                    lowerSeenVal=currVal;
                }
            }
        }
        return lowerSeenVal;
    }

    public int max(final Board board,final int depth){
        if(depth == 0 || isGameEndScenario(board)){
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestSeenVal = Integer.MIN_VALUE;
        for(final Move move: board.currentPlayer().getLegalMoves()){
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()){
                final int currVal = min(moveTransition.getTransitionBoard(),depth-1);
                if(currVal >= highestSeenVal){
                    highestSeenVal=currVal;
                }
            }
        }
        return highestSeenVal;
    }
}
