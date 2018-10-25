/*
 * File: ShowSaveOptionAlert.java
 * Names: Kevin Ahn, Lucas DeGraw, Jackie Hang, Kyle Slager
 * Class: CS 361
 * Project 6
 * Date: October 26, 2018
 * ---------------------------
 * Edited From: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 *
 */

package proj6AhnDeGrawHangSlager;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ShowSaveOptionAlert {

    private Alert alert;
    private ButtonType yesButton;
    private ButtonType noButton;
    private ButtonType cancelButton;

    public ShowSaveOptionAlert() {

        // create the alert
        this.alert = new Alert(Alert.AlertType.CONFIRMATION);
        this.alert.setTitle("Save Changes?");
        this.alert.setHeaderText("Do you want to save your changes?");
        this.alert.setContentText("Your changes will be lost if you don't save them.");

        // the option buttons for the user to click
        this.yesButton = new ButtonType("Yes");
        this.noButton = new ButtonType("No");
        this.cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        this.alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
    }

    public Optional<ButtonType> getUserSaveDecision() {

        return this.alert.showAndWait();
    }

    public ButtonType getYesButton() {
        return this.yesButton;
    }

    public ButtonType getNoButton() {
        return this.noButton;
    }

    public ButtonType getCancelButton() {
        return this.cancelButton;
    }
}