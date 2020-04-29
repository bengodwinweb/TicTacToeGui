package com.bengodwin.game;

import java.util.*;

public class Game {
    public enum Mode {
        EASY, MEDIUM, IMPOSSIBLE, TWO_PLAYER
    }

    private static final ArrayList<Integer> corners = new ArrayList<>();
    private static final ArrayList<Integer> middles = new ArrayList<>();
    private static final int center = 5;

    public static Map<Integer, Character> gameSymbols;

    private int[][] board;
    private ArrayList<Integer> emptySpaces;

    private GameState gameState;
    private Player lastPlayed;
    private int lastSpace;
    private boolean firstMove;
    private Mode mode;

    private int winningRowStart;
    private int winningRowEnd;

    // initializes a static hashmap for the symbols that will be used to represent each user or no value when printing the board
    static {
        gameSymbols = new HashMap<>();
        gameSymbols.put(0, ' ');
        gameSymbols.put(1, 'X');
        gameSymbols.put(2, 'O');

        int[] cornersArr = {1, 3, 7, 9};
        for (int corner : cornersArr) {
            corners.add(corner);
        }

        int[] middlesArr = {2, 4, 6, 8};
        for (int middle : middlesArr) {
            middles.add(middle);
        }
    }

    public Game() {
        this.board = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        this.gameState = GameState.UNFINISHED;
        this.lastPlayed = Player.COMPUTER;
        this.emptySpaces = new ArrayList<>();
        // initialize the empty spaces list to include all spaces
        for (int i = 1; i <= 9; i++) {
            emptySpaces.add(i);
        }
        this.firstMove = true;
        this.mode = Mode.IMPOSSIBLE;
        this.lastSpace = -1;
        this.winningRowStart = -1;
        this.winningRowEnd = -1;
    }

    // method for master class to enter a move for the User
    public boolean enterMove(int space) {
        // do not process the move if it is not the user's turn
        if (lastPlayed != Player.COMPUTER) {
            return false;
        }
        // do not process the move if the selected space is already full
        if (emptySpaces.indexOf(space) < 0) {
            return false;
        }
        // do not process the move if the game is finished
        if (gameState != GameState.UNFINISHED) {
            return false;
        }

        // set the value of the specified space to 1 (the user value)
        int row = (space - 1) / 3;
        int col = (space - 1) % 3;
        board[row][col] = 1;

        // remove the space from the emptySpaces list
        emptySpaces.remove((Integer) space);

        this.setLastPlayed(Player.USER);
        this.setGameState(GameStateLogic.checkGameState(this));
        this.setLastSpace(space);

        return true;
    }

    // method for master class to enter a move for either player in two player mode
    public int enterTwoPlayerMove(int space) {
        if (getMode() != Mode.TWO_PLAYER) return -1;
        // do not process the move if the selected space is already full
        if (emptySpaces.indexOf(space) < 0) {
            return -1;
        }
        // do not process the move if the game is finished
        if (getGameState() != GameState.UNFINISHED) {
            return -1;
        }

        int row = (space - 1) / 3;
        int col = (space - 1) % 3;

        int token = getLastPlayed() == Player.USER ? 2 : 1;
        board[row][col] = token;

        emptySpaces.remove((Integer) space);

        this.setLastPlayed(getLastPlayed() == Player.USER ? Player.COMPUTER : Player.USER);
        this.setGameState(GameStateLogic.checkGameState(this));
        this.setLastSpace(space);

        return token;
    }

    // method for master class to call for the computer to make an automated move
    public int computerMove() {
        // do not process if it is not the computer's turn
        if (lastPlayed != Player.USER) {
            return -1;
        }
        // do not process if the game is finished
        if (gameState != GameState.UNFINISHED) {
            return -1;
        }

        int selectedSpace;

        // for easy mode, pick a random space from the empty spaces list
        if (mode == Mode.EASY) selectedSpace = emptySpaces.get((new Random()).nextInt(emptySpaces.size()));
        else {
            // for medium or impossible mode, use the specified first moves for each mode if it is the computer's first move
            if (isFirstMove()) {
                selectedSpace = selectFirst();
            // for either medium or impossible mode, if it is not the first computer move
            // 1. Check if there is a winning move for the computer, if so make that move
            // 2. Check if there is a winning move for the user, if so play in that space
            // 3. Simulate games for each empty space and pick the one that has he best results
            } else {
                SimGame aGame = createGameBoard();

                // look for a winning offensive move and defensive move to save an opponent win
                int winningCpuMove = aGame.winningMove(Player.COMPUTER);
                int winningPlayerMove = aGame.winningMove(Player.USER);

                if (winningCpuMove == -1 && winningPlayerMove == -1) { // no winning move, perform simulated games to select best move

                    // 1. Create new array, same size as empty spaces, and assign a starting weight of zero for each space
                    int[] weightArray = new int[emptySpaces.size()];
                    for (int i = 0; i < weightArray.length; i++) {
                        weightArray[i] = 0;
                    }

                    // 2. for each index in emptySpaces, sim 25 games, adding the result of each to the weightArray index for that space
                    // if the sim is a win, the weight will increase by 1, loss will decrease by 1, draw will stay the same
                    int numberOfSims = 1000;

                    for (int i = 0; i < emptySpaces.size(); i++) {
                        for (int j = 0; j < numberOfSims; j++) {
                            SimGame gameBoard = createGameBoard();
                            gameBoard.addMove(emptySpaces.get(i), Player.COMPUTER);
                            int weight = gameBoard.simulate();
                            weightArray[i] += weight;
                        }
                    }

                    // 3. select the index with the highest weight and get the corresponding empty space from emptySpaces
                    int maxIndex = 0;
                    for (int i = 1; i < weightArray.length; i++) {
                        if (weightArray[i] > weightArray[maxIndex]) maxIndex = i;
                    }

                    selectedSpace = emptySpaces.get(maxIndex);

                } else { // there is a winning move, select that space
                    if (winningCpuMove > -1)
                        selectedSpace = winningCpuMove; // if there is a winning computer move, use that
                    else
                        selectedSpace = winningPlayerMove; // if there is not a winning computer move, defend against a winning player move
                }
            }
        }

        // 4. place a computer marker at the selected space, return the space number
        int row = (selectedSpace - 1) / 3;
        int col = (selectedSpace - 1) % 3;
        board[row][col] = 2;

        emptySpaces.remove((Integer) selectedSpace);

        this.setLastPlayed(Player.COMPUTER);
        this.setGameState(GameStateLogic.checkGameState(this));
        this.setLastSpace(selectedSpace);

        return selectedSpace;
    }

    // if it is the computer's first move, return the appropriate move for medium or impossible mode
    private int selectFirst() {
        int selectedSpace;

        // first moves for impossible mode
        if (mode == Mode.IMPOSSIBLE) {
            // if user played in center space, play in space 3
            if (lastSpace == 5) {
                selectedSpace = 3;
            // if user played in a middle space, play in an adjacent corner
            } else if (lastSpace == 2 || lastSpace == 4) {
                selectedSpace = 1;
            } else if (lastSpace == 6 || lastSpace == 8) {
                selectedSpace = 9;
            // if user played in a corner space, play in the middle space
            } else {
                selectedSpace = 5;
            }
        } else {
            // moves for medium mode
            int userSpaceRow = (lastSpace - 1) / 3;
            int userSpaceCol = (lastSpace - 1) % 3;

            // if the user played in a corner, play in an any other outside space
            if (corners.contains(lastSpace)) {
                ArrayList<Integer> edgeOptions = new ArrayList<>();
                for (int i = 1; i < 9; i++) if (i != lastSpace && i != center) edgeOptions.add(i);
                selectedSpace = edgeOptions.get((new Random()).nextInt(edgeOptions.size()));
            } else if (middles.contains(lastSpace)) {
                // if the user played in a middle outside space (not a corner or the middle space on the board),
                // play in any other outside space that does not share a row or column with the user's space
                ArrayList<Integer> rowOptions = new ArrayList<>();
                ArrayList<Integer> colOptions = new ArrayList<>();
                for (int i = 0; i < 3; i++) if (i != userSpaceRow) rowOptions.add(i);
                for (int j = 0; j < 3; j++) if (j != userSpaceCol) colOptions.add(j);
                selectedSpace = (rowOptions.get((new Random()).nextInt(rowOptions.size())) * 3) + colOptions.get((new Random()).nextInt(colOptions.size())) + 1;
            } else {
                // if the user played in the center space, play in any outside middle space
                selectedSpace = middles.get((new Random()).nextInt(middles.size()));
            }
        }

        setFirstMove(false);

        return selectedSpace;
    }

    // create a new SimGame that shares the state of the current game
    private SimGame createGameBoard() {
        return new SimGame(board, emptySpaces, lastPlayed);
    }

    // used to print out the current tic-tac-toe board for the user when playing in the command line
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("\n");

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                sb.append(' ').append(gameSymbols.get(board[i][j])).append(' ');

                if (j < board[0].length - 1) {
                    sb.append('|');
                }
            }

            if (i < board.length - 1) {
                sb.append("\n-----------\n");
            }
        }

        sb.append('\n');

        return sb.toString();
    }

    // prints a version of the tic tac toe board showing the space numbers for each space (for command line only)
    public static void printKeyMap() {
        StringBuilder sb = new StringBuilder().append('\n');
        int boardSize = 3;
        int current = 1;

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                sb.append(' ').append(current).append(' ');

                if (j < boardSize - 1) {
                    sb.append('|');
                }

                current++;
            }
            if (i < boardSize - 1) {
                sb.append("\n-----------\n");
            }
        }

        System.out.println(sb.toString());
    }

    // used for debugging to see the current state of the game
    public void printState() {
        System.out.println("\nLast Move: " + this.lastPlayed);
        System.out.println("Game State: " + this.gameState);
        System.out.println("Empty Spaces:");
        for (int space : emptySpaces) {
            System.out.print(space + "\t");
        }
        System.out.println();
    }

    public GameState getGameState() {
        return gameState;
    }

    protected void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getWinningRowStart() {
        return winningRowStart;
    }

    public void setWinningRowStart(int winningRowStart) {
        this.winningRowStart = winningRowStart;
    }

    public int getWinningRowEnd() {
        return winningRowEnd;
    }

    public void setWinningRowEnd(int winningRowEnd) {
        this.winningRowEnd = winningRowEnd;
    }

    public ArrayList<Integer> getEmptySpaces() {
        return emptySpaces;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public void setEmptySpaces(ArrayList<Integer> emptySpaces) {
        this.emptySpaces = emptySpaces;
    }

    public void setLastPlayed(Player lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public Player getLastPlayed() {
        return lastPlayed;
    }

    public void setLastSpace(int lastSpace) {
        this.lastSpace = lastSpace;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }
}




