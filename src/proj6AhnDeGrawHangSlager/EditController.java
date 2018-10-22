/*
 * File: EditController.java
 * Names: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 */

package proj6AhnDeGrawHangSlager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

/**
 * This is the controller class for all of the edit functions
 * within the edit menu.
 *
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 3.0
 * @since   10-3-2018
 */
public class EditController {

    // Reference to the tab pane of the IDE
    private TabPane tabPane;

    /**
     * Constructor for the class. Initializes
     * the current tab to null
     */
    public EditController(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleUndo() {
        getCurCodeArea().undo();
    }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleRedo() {
        getCurCodeArea().redo();
    }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCut() {
        getCurCodeArea().cut();
    }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCopy() {
        getCurCodeArea().copy();

    }

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML
    public void handlePaste() {
        getCurCodeArea().paste();

    }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML
    public void handleSelectAll() {
        getCurCodeArea().selectAll();
    }


    private JavaCodeArea getCurCodeArea() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane<CodeArea> curPane =
                (VirtualizedScrollPane<CodeArea>) curTab.getContent();
        return (JavaCodeArea) curPane.getContent();
    }
}