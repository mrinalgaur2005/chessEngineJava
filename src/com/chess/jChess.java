package com.chess;
import com.chess.engine.board.Board;
import com.chess.gui.Table;


public class jChess {
    public static void main(String[] args) {
        Board board = Board.createStaticBoard();

        System.out.println(board);

        Table.get().show();
    }
}
