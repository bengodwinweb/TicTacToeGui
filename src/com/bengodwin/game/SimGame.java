package com.bengodwin.game;

import java.util.ArrayList;
import java.util.Random;

// class to simulate moves that can be made on a game without affecting that game's state
public class SimGame extends Game {

    public SimGame(int[][] board, ArrayList<Integer> emptySpaces, Player lastPlayed) {
        super();
        int[][] localBoard = new int[3][3];
        // copy the passed in board so it won't be an alias
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                localBoard[i][j] = board[i][j];
            }
        }
        this.setBoard(localBoard);

        // copy the spaces so the spaces list won't be an alias
        ArrayList<Integer> localEmptySpaces = new ArrayList<>(emptySpaces);
        this.setEmptySpaces(localEmptySpaces);
        this.setLastPlayed(lastPlayed);
    }

    // initializer without lastPlayed - used by GameLogic when checking for draws when down to 1 or 2 spaces
    public SimGame(int[][] board, ArrayList<Integer> emptySpaces) {
        // lastPlayed does not matter in this instance, so we choose USER
        this(board, emptySpaces, Player.USER);
    }

    // recursive method to simulate moves until a result is reached
    public int simulate() {
        GameState gameState = getGameState();
        if (gameState == GameState.COMPUTER_WON) return 1;
        if (gameState == GameState.PLAYER_WON) return -1;
        if (gameState == GameState.DRAW) return 0;

        int selectedSpace;
        int winningCpuMove = winningMove(Player.COMPUTER);
        int winningPlayerMove = winningMove(Player.USER);

        ArrayList<Integer> emptySpaces = super.getEmptySpaces();
        int[][] board = super.getBoard();

        // if the current player is the computer, check for offensive and defensive winning moves.
        // Prioritize offensive move. If neither exists, randomly select an empty space
        if (this.getLastPlayed() == Player.USER) {
            selectedSpace = -1;

            if (winningPlayerMove > -1) selectedSpace = winningPlayerMove;
            if (winningCpuMove > -1) selectedSpace = winningCpuMove;
            selectedSpace = selectedSpace == -1 ? emptySpaces.get((new Random()).nextInt(emptySpaces.size())) : selectedSpace;
        }
        // if the current player is the USER, only check for winning offensive moves
        // this keeps the computer from assuming every match will end in a draw and going for moves that
        // could lead to a win
        else {
            selectedSpace = winningPlayerMove > -1 ? winningPlayerMove : emptySpaces.get((new Random()).nextInt(emptySpaces.size()));
        }

        int row = (selectedSpace - 1) / 3;
        int col = (selectedSpace - 1) % 3;

        if (this.getLastPlayed() == Player.COMPUTER) {
            board[row][col] = 1;
            this.setLastPlayed(Player.USER);
        } else {
            board[row][col] = 2;
            this.setLastPlayed(Player.COMPUTER);
        }

        emptySpaces.remove((Integer) selectedSpace);

        // recurse until there is a winner or draw, result will be caught at the beginning of the last recursion
        return simulate();
    }

    // method for master class to add a specific move to the board
    // used prior to simulating or for checking final spaces on board
    public void addMove(int space, Player player) {
        int row = (space - 1) / 3;
        int col = (space - 1) % 3;

        ArrayList<Integer> emptySpaces = super.getEmptySpaces();
        int[][] board = super.getBoard();

        board[row][col] = player == Player.COMPUTER ? 2 : 1;
        emptySpaces.remove((Integer) space);
        this.setLastPlayed(player == Player.COMPUTER ? Player.COMPUTER : Player.USER);
    }

    // checks if there is a winning move on the board for the specified player
    public int winningMove(Player player) {
        int playerNumber = player == Player.COMPUTER ? 2 : 1;

        ArrayList<Integer> emptySpaces = super.getEmptySpaces();
        int[][] board = super.getBoard();

        // check for horizontal win
        for (int i = 0; i < 3; i++) { // loop through rows
            if (board[i][0] == playerNumber && board[i][0] == board[i][1] && emptySpaces.contains((i + 1) * 3))
                return (i + 1) * 3; // end column
            if (board[i][0] == playerNumber && board[i][0] == board[i][2] && emptySpaces.contains(i * 3 + 2))
                return i * 3 + 2; // middle column
            if (board[i][1] == playerNumber && board[i][1] == board[i][2] && emptySpaces.contains(i * 3 + 1))
                return i * 3 + 1; // first column
        }

        // check for vertical win
        for (int i = 0; i < 3; i++) { // loop through columns
            if (board[0][i] == playerNumber && board[0][i] == board[1][i] && emptySpaces.contains(i + 7))
                return i + 7; // bottom row
            if (board[0][i] == playerNumber && board[0][i] == board[2][i] && emptySpaces.contains(i + 4))
                return i + 4; // middle row
            if (board[1][i] == playerNumber && board[1][i] == board[2][i] && emptySpaces.contains(i + 1))
                return i + 1; // top row
        }

        // check diagonals
        if (board[0][0] == playerNumber && board[0][0] == board[1][1] && emptySpaces.contains(9))
            return 9; // bottom right
        if (board[0][2] == playerNumber && board[0][2] == board[1][1] && emptySpaces.contains(7))
            return 7; // bottom left
        if ((board[0][0] == playerNumber && board[0][0] == board[2][2] && emptySpaces.contains(5)) || (board[0][2] == playerNumber && board[0][2] == board[2][0] && emptySpaces.contains(5)))
            return 5; // center from either direction
        if (board[2][0] == playerNumber && board[2][0] == board[1][1] && emptySpaces.contains(3)) return 3; // top right
        if (board[2][2] == playerNumber && board[2][2] == board[1][1] && emptySpaces.contains(1)) return 1; // top left

        // no winner, return -1
        return -1;
    }

    public GameState getGameState() {
        return GameStateLogic.checkGameState(this);
    }
}
