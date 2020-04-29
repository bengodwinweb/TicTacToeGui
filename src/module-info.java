module TicTacToeGui {
    requires javafx.fxml;
    requires javafx.controls;

    exports com.bengodwin.tictactoegui;
    opens com.bengodwin.tictactoegui to javafx.fxml;
}