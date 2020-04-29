package com.bengodwin.tictactoegui;

import com.bengodwin.game.Game;
import com.bengodwin.game.GameState;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.ArrayList;

public class Controller {
    private enum State {
        ACTIVE, INACTIVE;
    }

    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 3;

    private static final int TOKEN_WIDTH = 10;
    private static final int WINNING_PATH_WIDTH = 8;

    private static final Color PLAYER_COLOR = Color.rgb(185, 163, 116);
    private static final Color COMPUTER_COLOR = Color.rgb(216, 217, 216);

    private static final Duration FADE_DURATION = Duration.millis(500);
    private static final Duration PAUSE_DURATION = Duration.millis(200);
    private static final Duration TOKEN_DURATION = Duration.millis(250);
    private static final Duration WIN_LINE_DURATION = Duration.millis(325);

    private ArrayList<Pane> panes;
    private Game game;
    private State state;
    private int playerScore;
    private int computerScore;

    private final ToggleGroup modeGroup;

    @FXML
    private GridPane grid;
    @FXML
    private Label messageLabel;
    @FXML
    private Label xScore;
    @FXML
    private Label oScore;
    @FXML
    private HBox modeBox;
    @FXML
    private StackPane centerStack;

    // constructor - add listener for user clicking on new mode
    public Controller() {
        game = new Game();
        panes = new ArrayList<>();
        state = State.INACTIVE;
        modeGroup = new ToggleGroup();
        modeGroup.selectedToggleProperty().addListener(e -> {
            setMode();
            newGame();
        });
    }


    public void initialize() {
        // create the panes to draw tokens and add one to each space in the grid
        // add the space clicked click handler to each pane and pass in the space number of the pane
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                Pane pane = new Pane();
                int space = (i * 3) + (j + 1);
                pane.setOnMouseClicked(e -> spaceClicked(space));
                panes.add(pane);
                if (i == 1) pane.getStyleClass().add("middle-row");
                if (j == 1) pane.getStyleClass().add("middle-col");
                if (i == 1 && j == 1) pane.getStyleClass().add("middle-cell");
                grid.add(pane, j, i);
            }
        }

        // create the buttons that will go into the toggle group for changing the mode at the top of the GUI
        modeBox.paddingProperty().setValue(new Insets(0, 5, 0, 5));
        modeBox.setSpacing(5);

        ToggleButton easyButton = new ToggleButton("Easy");
        easyButton.setUserData("EASY");
        easyButton.setToggleGroup(modeGroup);

        ToggleButton mediumButton = new ToggleButton("Medium");
        mediumButton.setUserData("MEDIUM");
        // set the default mode to Medium
        mediumButton.setSelected(true);
        mediumButton.setToggleGroup(modeGroup);

        ToggleButton impossibleButton = new ToggleButton("Impossible");
        impossibleButton.setUserData("IMPOSSIBLE");
        impossibleButton.setToggleGroup(modeGroup);

        ToggleButton twoPlayerButton = new ToggleButton("Two Players");
        twoPlayerButton.setUserData("TWO_PLAYER");
        twoPlayerButton.setToggleGroup(modeGroup);

        modeBox.getChildren().addAll(easyButton, mediumButton, impossibleButton, twoPlayerButton);
    }

    @FXML
    // starts a new game, serves as button handler for new game button
    private void newGame() {
        switch (state) {
            case ACTIVE:
                fadeBetween(grid, grid, true);
                break;
            default:
                fadeBetween(messageLabel, grid, true);
                break;
        }

        game = new Game();
        setMode();
        setMessage();

        state = State.ACTIVE;
    }

    // sets the game mode, called by the ToggleGroup named modeGroup
    private void setMode() {
        if (modeGroup.getSelectedToggle() == null) return; // catch in case toggles aren't initialized
        switch (modeGroup.getSelectedToggle().getUserData().toString()) {
            case "EASY":
                game.setMode(Game.Mode.EASY);
                break;
            case "MEDIUM":
                game.setMode(Game.Mode.MEDIUM);
                break;
            case "IMPOSSIBLE":
                game.setMode(Game.Mode.IMPOSSIBLE);
                break;
            case "TWO_PLAYER":
                game.setMode(Game.Mode.TWO_PLAYER);
            default: // catch in case user clicks on same button twice and there is no selected toggle
                break;
        }
    }

    @FXML
    // button handler to reset the X and O scores on the screen
    private void resetScores() {
        computerScore = 0;
        playerScore = 0;
        updateScoreLabel(xScore, playerScore);
        updateScoreLabel(oScore, computerScore);
    }

    // space clicked - draw the appropriate token in the appropriate space
    private void spaceClicked(int space) {
        if (state == State.INACTIVE) return;

        // if two player, enter the token of the player that didn't play last in the clicked space and don't get a computer move
        if (game.getMode() == Game.Mode.TWO_PLAYER) {
            int token = game.enterTwoPlayerMove(space);
            if (token > 0) drawToken(space, token, false);
        // if not two player, enter the user token in the space and get a computer move
        } else {
            if (game.enterMove(space)) drawToken(space, 1, true);
        }
    }

    // draws the visual tokens on the GUI board
    private void drawToken(int space, int token, boolean thenGetComputerMove) {
        // break if space is out of range
        if (space < 1 || space > 9) return;

        // get the pane in the grid that corresponds to the space in the game board
        Pane pane = panes.get(space - 1);

        // token is 1 - draw an X in the pane
        // this corresponds to the User player, in one person modes call for a computer move
        // then draw that token as well
        if (token == 1) {
            Path line1 = new Path(
                    new MoveTo(30, 30),
                    new LineTo(70, 70)
            );
            line1.setStroke(PLAYER_COLOR);
            line1.setStrokeWidth(TOKEN_WIDTH);
            Path line2 = new Path(
                    new MoveTo(30, 70),
                    new LineTo(70, 30)
            );
            line2.setStroke(PLAYER_COLOR);
            line2.setStrokeWidth(TOKEN_WIDTH);

            pane.getChildren().addAll(line1, line2);
            Animation animation_1 = clipAnimation(line1, TOKEN_DURATION);
            Animation animation_2 = clipAnimation(line2, TOKEN_DURATION);

            if (thenGetComputerMove) {
                int computerSpace = game.computerMove();
                // if the game is over, computerSpace will be invalid, call setMessage so the game doesn't get stuck
                if (computerSpace > 0) {
                    animation_2.setOnFinished(e -> drawToken(computerSpace, 2, false));
                } else {
                    animation_2.setOnFinished(e -> setMessage());

                }
            // if the game is in two player mode, don't get a computer move
            } else {
                animation_2.setOnFinished(e -> setMessage());
            }

            animation_1.play();
            animation_2.play();
        }
        // draw a circle in the pane, corresponds to Computer move if not two player
        if (token == 2) {
            int center = 50;
            int radius = 25;
            MoveTo startPoint = new MoveTo(center, center - radius);
            ArcTo arc = new ArcTo(radius, radius, 0, center - 1, center - radius, true, true);

            Path circle = new Path(
                    startPoint,
                    arc,
                    new ClosePath()
            );
            circle.setStrokeWidth(TOKEN_WIDTH);
            circle.setStroke(COMPUTER_COLOR);
            circle.setFill(null);
            pane.getChildren().add(circle);

            Animation circle_animation = clipAnimation(circle, TOKEN_DURATION);
            circle_animation.setOnFinished(e -> setMessage());

            circle_animation.play();
        }
    }

    // checks the status of the game, if the game is over sets the label text and starts the transition
    private void setMessage() {
        if (game.getGameState() == GameState.UNFINISHED) {
            return;
        }

        switch (game.getGameState()) {
            case DRAW:
                messageLabel.setText("Draw");
                break;
            case PLAYER_WON:
                messageLabel.setText("X Wins");
                updateScoreLabel(xScore, ++playerScore);
                break;
            case COMPUTER_WON:
                messageLabel.setText("O Wins");
                updateScoreLabel(oScore, ++computerScore);
                break;
            default:
                break;
        }

        fadeBetween(grid, messageLabel, false);

        state = State.INACTIVE;
    }

    private void updateScoreLabel(Label label, int score) {
        label.setText(Integer.toString(score));
    }

    // creates and plays the animations when a game is won or reset
    private void fadeBetween(Node outNode, Node inNode, boolean fast) {
        // pause before fading out first node
        PauseTransition pause1 = new PauseTransition(fast ? Duration.ZERO : PAUSE_DURATION);
        // pause between fading out first node and fading in second node
        PauseTransition pause2 = new PauseTransition(fast ? Duration.ZERO : PAUSE_DURATION);

        // sequential transition will play each transition in order
        SequentialTransition sequentialTransition = new SequentialTransition();

        // if the game is won, the line that crosses out the winning row will be added to this parallel
        // transition so that it fades with the grid
        ParallelTransition parallelFadeOut = new ParallelTransition();

        // fade out for the first node
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, outNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        parallelFadeOut.getChildren().add(fadeOut);
        // if the node being taken off screen is the grid, clear out all children from each pane
        // in the grid after the fade out happens, removing the visual tokens from the GUI board
        if (outNode == grid) {
            fadeOut.setOnFinished(e -> {
                for (Pane pane : panes) {
                    pane.getChildren().clear();
                }
            });
        }
        // if the node being taken off screen is the message, set visible to false after the message
        // has faded off the screen
        if (outNode == messageLabel) {
            fadeOut.setOnFinished(e -> {
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
            });
        }

        // if the node that is about to be brought onto the screen is the message, set message visible
        // to true after the second pause
        if (inNode == messageLabel) {
            pause2.setOnFinished(e -> {
                messageLabel.setVisible(true);
                messageLabel.setManaged(true);
            });
        }

        // fade in for the second node
        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, inNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // sequential transition to play them in order - pause, fade out first, pause, fade in second
        sequentialTransition.getChildren().addAll(pause1, parallelFadeOut, pause2, fadeIn);

        // if state is inactive, play the transition and exit method so that the winning line isn't drawn again
        if (state == State.INACTIVE) {
            sequentialTransition.play();
            return;
        }

        switch (game.getGameState()) {
            case UNFINISHED:
                sequentialTransition.play();
                return;
            case DRAW:
                sequentialTransition.play();
                break;
            default:
                // remaining two states - a player has won.
                // 1. Create a pane in front of the grid to draw the line on
                // 2. Get the path for the winning line and add it to the pane
                // 3. Get the animation to draw the winning line
                // 4. Set the callback on the animation to call the sequential transition from above
                // 5. Add the path to the fadeOut group, so it goes out with the grid in parallel
                // 6. play the animation
                Pane drawPane = new Pane();
                drawPane.setBackground(null);
                ObservableList<Node> children = FXCollections.observableArrayList(centerStack.getChildren());
                children.add(drawPane);
                centerStack.getChildren().setAll(children);

                Path path = makeWinningPath();
                drawPane.getChildren().add(path);

                Animation path_transition = clipAnimation(path, WIN_LINE_DURATION);
                path_transition.setOnFinished(e -> sequentialTransition.play());

                FadeTransition fadeWinningLine = new FadeTransition(FADE_DURATION, path);
                fadeWinningLine.setFromValue(1.0);
                fadeWinningLine.setToValue(0.0);
                // after the winning line fades off the screen with the grid, remove it and the
                // pane it's drawn on from the stack pane
                fadeWinningLine.setOnFinished(e -> {
                    drawPane.getChildren().clear();
                    children.remove(drawPane);
                    centerStack.getChildren().setAll(children);
                });

                parallelFadeOut.getChildren().add(fadeWinningLine);

                path_transition.play();
                break;
        }
    }

    // returns the path from the start of the winning row to the end of the winning row
    private Path makeWinningPath() {
        // get the stating and ending space from the Game
        int start = game.getWinningRowStart();
        int end = game.getWinningRowEnd();

        // catch impossible row and return empty path
        if (start < 1 || (start > 4 && start != 7) || (end < 6 && end != 3) || end > 9) return new Path();

        double startX = -1;
        double startY = -1;
        double endX = -1;
        double endY = -1;

        final double GRID_HALF_SIZE = 150;
        final double PANE_HALF_SIZE = 50;
        final double PANE_FULL_SIZE = 100;
        final double STACK_HALF_WIDTH = centerStack.getWidth() / 2;
        final double STACK_HALF_HEIGHT = centerStack.getHeight() / 2;
        final double OFFSET = 15;

        // diagonals
        if ((start == 1 && end == 9) || (start == 7 && end == 3)) {
            startX = STACK_HALF_WIDTH - GRID_HALF_SIZE + OFFSET;
            endX = STACK_HALF_WIDTH + GRID_HALF_SIZE - OFFSET;

            // end points for each point if diagonal is from 1 - 9
            if (end == 9) {
                startY = STACK_HALF_HEIGHT - GRID_HALF_SIZE + OFFSET;
                endY = STACK_HALF_HEIGHT + GRID_HALF_SIZE - OFFSET;
                // end points for each point if diagonal is from 7 - 3
            } else {
                startY = STACK_HALF_HEIGHT + GRID_HALF_SIZE - OFFSET;
                endY = STACK_HALF_HEIGHT - GRID_HALF_SIZE + OFFSET;
            }
        }
        // rows
        if (start + 2 == end) {
            startX = STACK_HALF_WIDTH - GRID_HALF_SIZE + OFFSET;
            endX = STACK_HALF_WIDTH + GRID_HALF_SIZE - OFFSET;

            startY = STACK_HALF_HEIGHT - GRID_HALF_SIZE + PANE_HALF_SIZE + PANE_FULL_SIZE * (start / 3);
            endY = startY;
        }
        // columns
        if (start + 6 == end) {
            startX = STACK_HALF_WIDTH - GRID_HALF_SIZE + PANE_HALF_SIZE + PANE_FULL_SIZE * (start - 1);
            endX = startX;

            startY = STACK_HALF_HEIGHT - GRID_HALF_SIZE + OFFSET;
            endY = STACK_HALF_HEIGHT + GRID_HALF_SIZE - OFFSET;
        }
        // catch in case any of the coordinates weren't updated
        if (startX == -1 || startY == -1 || endX == -1 || endY == -1) {
            return new Path();
        }

        Path path = new Path(
                new MoveTo(startX, startY),
                new LineTo(endX, endY)
        );
        path.setStrokeWidth(WINNING_PATH_WIDTH);
        path.setStroke(COMPUTER_COLOR);

        return path;
    }

    // returns an animation that will animate the shape of the path over the specified duration
    private Animation clipAnimation(Path path, Duration duration) {
        Pane clip = new Pane();
        path.clipProperty().set(clip);

        Circle pen = new Circle(0, 0, 20);

        ChangeListener pen_Listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o1, Object o2) {
                Circle clip_eraser = new Circle(pen.getTranslateX(), pen.getTranslateY(), pen.getRadius());
                clip.getChildren().add(clip_eraser);
            }
        };

        pen.translateXProperty().addListener(pen_Listener);
        pen.translateYProperty().addListener(pen_Listener);
        pen.rotateProperty().addListener(pen_Listener);

        PathTransition pathTransition = new PathTransition(duration, path, pen);
        pathTransition.setOnFinished(e -> {
            path.setClip(null);
            clip.getChildren().clear();
        });

        return pathTransition;
    }
}
