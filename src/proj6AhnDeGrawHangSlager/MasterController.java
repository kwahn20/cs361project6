/*
 * File: ToolbarController.java
 * Names: Matt Jones, Kevin Zhou, Kevin Ahn, Jackie Hang
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Edited By: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 */

package proj6AhnDeGrawHangSlager;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.event.Event;


/**
 * This is the master controller for the program. it references
 * the other controllers for proper menu functionality.
 *
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * @version 2.0
 * @since   10-12-2018
 */
public class MasterController {
    @FXML private Menu editMenu;
    @FXML private TabPane tabPane;
    @FXML private VBox vBox;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeMenuItem;
    @FXML private Console console;
    @FXML private Button stopButton;
    @FXML private Button compileButton;
    @FXML private Button compileRunButton;
    private Tab curTab;
    private EditController editController;
    private FileController fileController;
    private ToolbarController toolbarController;
    private BooleanProperty compileDisable;
    private BooleanProperty compileRunDisable;

    /**
     * Constructor for the class. Initializes the three controllers
     * and sets the current Tab to null
     */
    public MasterController() {
        curTab = null;
    }

    @FXML public void initialize(){
        editController = new EditController(tabPane);
        fileController = new FileController(vBox,tabPane);
        toolbarController = new ToolbarController(console);
        SimpleListProperty<Tab> listProperty = new SimpleListProperty<Tab> (tabPane.getTabs());
        editMenu.disableProperty().bind(listProperty.emptyProperty());
        saveMenuItem.disableProperty().bind(listProperty.emptyProperty());
        saveAsMenuItem.disableProperty().bind(listProperty.emptyProperty());
        closeMenuItem.disableProperty().bind(listProperty.emptyProperty());
        compileDisable = new SimpleBooleanProperty();
        compileRunDisable = new SimpleBooleanProperty();
        compileButton.disableProperty().bind(saveMenuItem.disableProperty().or(compileDisable));
        compileRunButton.disableProperty().bind(saveMenuItem.disableProperty().or(compileRunDisable));
    }

    /**
     * Calls handleNewCommand() from the Toolbar Controller if the user
     * presses the enter key.
     * @param ke the key event
     */
    @FXML public void handleUserKeypress(KeyEvent ke){
        toolbarController.handleUserKeypress(ke);
    }

    /**
     * Handler for the Compile in the toolbar. Checks if the current file
     * has been saved. If it has not, prompts the user to save, if so,
     * compiles the program. If user chooses not to save, compiles last
     * version of the file.
     */
    @FXML public void handleCompile() throws InterruptedException {
        disableToolbarButtons();
        if(!fileController.getSaveStatus()) {
            String saveResult = toolbarController.handleCompileSave();
            if (saveResult == "yesButton") {
                fileController.handleSave();
                toolbarController.handleCompile(fileController.getFileName());
            } else if (saveResult == "noButton") {
                toolbarController.handleCompile(fileController.getFileName());
            }
        } else {
            toolbarController.handleCompile(fileController.getFileName());
        }
        //enableToolbarButtons();
    }

    /**
     * Handler for the Compile and Run button in the toolbar.
     * Checks if the current file has been saved. If it has not,
     * prompts the user to save, if so, compiles and runs the program.
     * If user chooses not to save, compiles and runs the last
     * version of the file.
     */
    @FXML public void handleCompileAndRun() {
        disableToolbarButtons();
        if(!fileController.getSaveStatus()) {
            String saveResult = toolbarController.handleCompileSave();
            if (saveResult == "yesButton") {
                System.out.println("This is executing");
                fileController.handleSave();
                toolbarController.handleCompileAndRun(fileController.getFileName());
            } else if (saveResult == "noButton") {
                toolbarController.handleCompileAndRun(fileController.getFileName());
            }
        } else {
            toolbarController.handleCompileAndRun(fileController.getFileName());
        }
    }

    /**
     * Handler for the Stop button in the toolbar.
     * Calls the handleStop() method from Toolbar Controller and re-enables the toolbar buttons.
     */
    @FXML public void handleStop(){
        toolbarController.handleStop();
        enableToolbarButtons();
    }

    /**
     * Handler for the "About" menu item in the "File" menu.
     * Creates an Information alert dialog to display author and information of this program
     */
    @FXML public void handleAbout() {
        fileController.handleAbout();
    }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, and also adds null to the HashMap
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleNew() {
        Tab newTab = fileController.handleNew();
        enableToolbarButtons();
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Creates a FileChooser to select a file
     * Use scanner to read the file and write it into a new tab.
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleOpen() {
        Tab newTab = fileController.handleOpen();
        enableToolbarButtons();
    }

    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSave and then close the tab.
     * Otherwise, just close the tab.
     */
    @FXML public void handleClose(Event event) {
        disableToolbarButtons();
        fileController.handleClose(event);
    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    @FXML public void handleSave() {
        fileController.handleSave();
    }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    @FXML public void handleSaveAs( ) {
        fileController.handleSaveAs();
    }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     */
    @FXML public void handleExit(Event event) {
        fileController.handleExit(event);
    }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    @FXML public void handleUndo() { editController.handleUndo(); }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML public void handleRedo() {
        editController.handleRedo(); }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML public void handleCut() {
        editController.handleCut(); }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML public void handleCopy() {
        editController.handleCopy();}

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML public void handlePaste() {
        editController.handlePaste(); }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML public void handleSelectAll() {
        editController.handleSelectAll(); }

    /**
     * Disables the Compile and Compile and Run buttons, enables the Stop button.
     */
    private void disableToolbarButtons() {
        this.compileDisable.set(true);
        this.compileRunDisable.set(true);
        this.stopButton.setDisable(false);
    }

    /**
     * Enables the Compile and Compile and Run buttons, disables the Stop button.
     */
    private void enableToolbarButtons() {
        this.compileDisable.set(false);
        this.compileRunDisable.set(false);
        stopButton.setDisable(true);
    }
}