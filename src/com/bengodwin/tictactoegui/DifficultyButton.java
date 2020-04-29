package com.bengodwin.tictactoegui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.ToggleButton;

public class DifficultyButton extends ToggleButton {

    private static PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    BooleanProperty selected;

    public DifficultyButton() {
        selected = new SimpleBooleanProperty(false);
        selected.addListener(e -> pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected.get()));

        getStyleClass().add("selected");
    }


}
