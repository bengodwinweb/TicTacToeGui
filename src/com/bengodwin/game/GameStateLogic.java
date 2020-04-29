package com.bengodwin.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameStateLogic {
    private static Map<Integer, GameState> player;

    static {
        player = new HashMap<>();
        player.put(1, GameState.PLAYER_WON);
        player.put(2, GameState.COMPUTER_WON);
    }

    public static GameState checkGameState(Game game) {
        int[][] board = game.getBoard();
        ArrayList<Integer> emptySpaces = game.getEmptySpaces();

        // check rows, if there is a winner get the value from a cell in the winning row and get the player from it
        int row = checkRows(game);
        if (row > -1) return player.get(board[row][0]);

        // check columns, if there is a winner get the value from a cell in the winning column and get the player from it
        int col = checkColumns(game);
        if (col > -1) return player.get(board[0][col]);

        // check diagonals, if there is a winner get the value from the center cell and get the player from it
        if (checkDiagonal(game)) return player.get(board[1][1]);

        // no winner, check for a draw. This method will either return Draw or Unfinished
        return checkDraw(board, emptySpaces);
    }

    private static int checkRows(Game game) {
        int[][] board = game.getBoard();
        // for each row, if the first box is not 0, and all boxes match, then there is a win in that row, return the row number
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
                game.setWinningRowStart(coordToSpace(i, 0));
                game.setWinningRowEnd(coordToSpace(i, 2));
                return i;
            }
        }
        return -1;
    }

    private static int checkColumns(Game game) {
        int[][] board = game.getBoard();
        // for each column, if the first top box is not 0, and all boxes match, then there is a win in that column, return the column number
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
                game.setWinningRowStart(coordToSpace(0, i));
                game.setWinningRowEnd(coordToSpace(2, i));
                return i;
            }
        }
        return -1;
    }

    private static boolean checkDiagonal(Game game) {
        int[][] board = game.getBoard();
        // if all three boxes are equal and one is not equal to 0, there is a winner, return true
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            game.setWinningRowStart(1);
            game.setWinningRowEnd(9);
            return true; // top left to bottom right diag
        }
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            game.setWinningRowStart(7);
            game.setWinningRowEnd(3);
            return true; // top right to bottom left
        }
        return false;
    }

    private static GameState checkDraw(int[][] board, ArrayList<Integer> emptySpaces) {
        // if there are empty spaces, game is unfinished
        if (emptySpaces.size() > 0) return GameState.UNFINISHED;

        // no empty spaces and a win was not caught above, game is a draw
        return GameState.DRAW;
    }

    private static int coordToSpace(int i, int j) {
        return i * 3 + j + 1;
    }

}
