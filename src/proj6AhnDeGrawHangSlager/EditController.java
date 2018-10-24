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
import org.fxmisc.richtext.NavigationActions.SelectionPolicy;
import java.util.regex.Pattern;


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
        getCurJavaCodeArea().undo();
    }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleRedo() {
        getCurJavaCodeArea().redo();
    }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCut() {
        getCurJavaCodeArea().cut();
    }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCopy() {
        getCurJavaCodeArea().copy();

    }

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML
    public void handlePaste() {
        getCurJavaCodeArea().paste();

    }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML
    public void handleSelectAll() {
        getCurJavaCodeArea().selectAll();
    }

    @FXML
    public void handleDarkMode(){
        for(Tab tab: this.tabPane.getTabs()){

            CodeArea codeArea = getCurJavaCodeArea();
            System.out.println(codeArea.getStylesheets());
            codeArea.getStylesheets().add("proj6AhnDeGrawHangSlager/DarkMode.css");
            System.out.println(codeArea.getStylesheets());
        }
    }

    /**
     * comments out the line that the cursor is one if it uncommented,
     * undoes a "layer" of commenting (pair of forward slashes "//") if there is at least one
     */
    public void toggleSingleLineComment() {

        // handle single line comment toggling
        JavaCodeArea curCodeArea = getCurJavaCodeArea();

        // position caret at start of line
        curCodeArea.lineStart( SelectionPolicy.ADJUST );

        // get caret index location in file
        int caretIdx = curCodeArea.getCaretPosition();

        // temporarily highlight the current line to get its text as a string
        curCodeArea.selectLine();
        String curLineText = curCodeArea.getSelectedText();
        curCodeArea.deselect();

        // regex to check if current line is commented
        if ( Pattern.matches("(?:\\/\\/.*)", curLineText) ) {

            // uncomment the line by taking out the first instance of "//"
            String curLineUncommented = curLineText.replaceFirst("//", "");

            // replace the current line with the newly commented line
            curCodeArea.replaceText(caretIdx, caretIdx+curLineText.length(),
                                                    curLineUncommented);
            return;
        }

        // add a "//" at the beginning of the line to comment it out
        curCodeArea.replaceText(caretIdx, caretIdx, "//");
    }

    /**
     *
     * @return the JavaCodeArea currently in focus of the TabPane
     */
    public JavaCodeArea getCurJavaCodeArea() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane<CodeArea> curPane =
                (VirtualizedScrollPane<CodeArea>) curTab.getContent();
        return (JavaCodeArea)curPane.getContent();
    }

}