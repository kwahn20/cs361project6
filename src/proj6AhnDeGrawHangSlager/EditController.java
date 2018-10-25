/*
 * File: EditController.java
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

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.NavigationActions.SelectionPolicy;
import java.util.regex.Pattern;
import java.util.Stack;
import javafx.geometry.Bounds;
import java.util.Arrays;


/**
 * This is the controller class for all of the edit functions
 * within the edit menu.
 *
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou (Project 5)
 * @author  Kevin Ahn, Lucas DeGraw, Jackie Hang, Kyle Slager
 *
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

    /**
     * comments out the line that the cursor is one if it uncommented,
     * undoes a "layer" of commenting (pair of forward slashes "//") if there >= one
     */
    public void toggleSingleLineComment() {

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
            String curLineUncommented =
                    curLineText.replaceFirst("//", "");

            // replace the current line with the newly commented line
            curCodeArea.replaceText(caretIdx, caretIdx+curLineText.length(),
                                                    curLineUncommented);

            return;
        }

        // add a "//" at the beginning of the line to comment it out
        curCodeArea.replaceText(caretIdx, caretIdx, "//");
    }


    /**
     * if a single "{", "}", "[", "]", "(", ")" is highlighted, this will attempt to find
     * the matching opening or closing character and if successful, will highlight the
     * text in between the matching set of {}, [], or (),
     * otherwise will display an appropriate error message
     */
    public void handleMatchBracketOrParen() {

        // get in-focus code area
        JavaCodeArea curJavaCodeArea = getCurJavaCodeArea();

        // get any highlighted text in the code area
        String highlightedText = curJavaCodeArea.getSelectedText();

        if (highlightedText.isEmpty()) {
            showAlert("NOTHING HIGHLIGHTED");
            return;
        }
        else if (highlightedText.length() == 1) {

            // true if matching a closing character to an opening character,
            // false if matching an opening character to a closing character
            Boolean findClosingCharacter;

            if (highlightedText.equals("{") || highlightedText.equals("[")
                    || highlightedText.equals("(")) {
                findClosingCharacter = true;
            }
            else if (highlightedText.equals("}") || highlightedText.equals("]")
                    || highlightedText.equals(")")) {
                findClosingCharacter = false;
            }
            else {
                System.out.println("VALID CHARACTER NOT HIGHLIGHTED\n" +
                        "VALID CHARACTERS ARE '{', '}', '[', ']', '(' or ')'");
                showAlert("VALID CHARACTER NOT HIGHLIGHTED\n" +
                        "VALID CHARACTERS ARE '{', '}', '[', ']', '(' or ')'");
                return;
            }

            // save length of whole file
            int fileTextLength = curJavaCodeArea.getLength();


            // this stack holds only opening "[","(","{" or closing "]",")","}" characters
            // depending which type was initially highlighted to match against
            // start with initial highlighted bracket/parenthesis/brace on the stack
            Stack<String> charStack = new Stack<>();
            charStack.push(highlightedText);

            // get the indices of the highlighted character within the file
            IndexRange highlightedCharRange = curJavaCodeArea.getSelection();

            // TODO: account for opening/closing characters in commented lines
            // TODO: account for opening/closing characters within a string not limited to "{" or '{'
            if (findClosingCharacter) {

                String openingMatchCharacter;

                // search forward through file
                int idxAfterCharToMatch = highlightedCharRange.getEnd();
                for (int i = idxAfterCharToMatch; i < fileTextLength; i++) {

                    // get the opening char on top of stack
                    openingMatchCharacter = charStack.peek();

                    // current character being checked for a closing bracket match
                    String curChar = curJavaCodeArea.getText(i, i+1);

                    // check that the character is not not written as a string "(" or '('
                    try {
                        if (curJavaCodeArea.getText(i-1, i).equals("\"")
                                && curJavaCodeArea.getText(i+1, i+2).equals("\"")
                                || curJavaCodeArea.getText(i-1, i).equals("'")
                                && curJavaCodeArea.getText(i+1, i+2).equals("'")) {
                            System.out.println("continuing");
                            continue;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println(e);
                    }

                    // pop the top opening char off the stack if its closing match is found,
                    // otherwise push the newly found opening char onto the stack
                    switch(curChar) {
                        case("]"):
                            if (openingMatchCharacter.equals("[")) charStack.pop();
                            break;
                        case(")"):
                            if (openingMatchCharacter.equals("(")) charStack.pop();
                            break;
                        case("}"):
                            if (openingMatchCharacter.equals("{")) charStack.pop();
                            break;
                        case("["):
                            charStack.push(curChar);
                            break;
                        case("("):
                            charStack.push(curChar);
                            break;
                        case("{"):
                            charStack.push(curChar);
                            break;
                        default:
                            break;
                    }
                    // stack is empty if the originally highlighted character has been
                    /// matched with the current character
                    if (charStack.isEmpty()) {
                        // highlight between matching characters ({}, () or [])
                        curJavaCodeArea.selectRange(idxAfterCharToMatch, i);
                        return;
                    }
                }
                System.out.println(Arrays.toString(charStack.toArray()));
                System.out.println("matching closing character not found");
                showAlert("MATCHING CLOSING CHARACTER NOT FOUND");
                return;
            }
            else {
                String closingMatchCharacter;
                int idxBeforeCharToMatch = highlightedCharRange.getStart();
                // search backward through file
                for (int i = idxBeforeCharToMatch; i > 0; i--) {

                    // get closing character on top of the stack
                    closingMatchCharacter = charStack.peek();

                    // check that the character is not not written as a string "(" or '('
                    try {
                        if (curJavaCodeArea.getText(i-2, i-1).equals("\"")
                                && curJavaCodeArea.getText(i, i+1).equals("\"")
                                || curJavaCodeArea.getText(i-2, i-1).equals("'")
                                && curJavaCodeArea.getText(i, i+1).equals("'")) {
                            System.out.println("continuing");
                            continue;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println(e);
                    }

                    // pop the top opening char off the stack if its closing match is found,
                    // otherwise push the newly found opening char onto the stack
                    // current character being checked for a closing bracket match
                    String curChar = curJavaCodeArea.getText(i-1, i);
                    switch(curChar) {
                        case("["):
                            if (closingMatchCharacter.equals("]")) charStack.pop();
                            break;
                        case("("):
                            if (closingMatchCharacter.equals(")")) charStack.pop();
                            break;
                        case("{"):
                            if (closingMatchCharacter.equals("}")) charStack.pop();
                            break;
                        case("]"):
                            charStack.push(curChar);
                            break;
                        case(")"):
                            charStack.push(curChar);
                            break;
                        case("}"):
                            charStack.push(curChar);
                            break;
                        default:
                            break;
                    }
                    // stack is empty if the originally highlighted character has been
                    /// matched with the current character in the file
                    if (charStack.isEmpty()) {
                        // highlight between matching characters ({}, () or [])
                        curJavaCodeArea.selectRange(i, idxBeforeCharToMatch);
                        return;
                    }
                }
                System.out.println("matching opening character not found");
                showAlert("MATCHING OPENING CHARACTER NOT FOUND");
                return;
            }
        }
        else {
            System.out.println("must select opening/closing parenthesis/bracket/brace");
            showAlert("VALID CHARACTERS ARE A SINGLE '{', '}', '[', ']', '(' or ')'");
        }

    }

    /**
     * creates and displays an informational alert
     * @param header the content of the alert
     */
    private void showAlert(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.show();
    }
    /**
     * Tabs the line of code that the cursor is on
     *
     */
    public void singleLineTabbing() {

        JavaCodeArea curCodeArea = getCurJavaCodeArea();

        // position caret at start of line
        curCodeArea.lineStart( SelectionPolicy.ADJUST );

        // get caret index location in file
        int caretIdx = curCodeArea.getCaretPosition();

        // temporarily highlight the current line to get its text as a string
        curCodeArea.selectLine();
        String curLineText = curCodeArea.getSelectedText();
        curCodeArea.deselect();

        // add a tab at the beginning of the line to indent it
        curCodeArea.replaceText(caretIdx, caretIdx, "\t");
    }

    /**
     * Detabs the line of code that the cursor is on
     *
     */
    public void singleLineUntabbing() {

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
        if ( Pattern.matches("(?:[ \\t].*)", curLineText) ) {

            // detabs the line by taking out the first instance of a tab
            String curLineUncommented = curLineText.replaceFirst("[ \\t]", "");

            // replace the current line with the newly commented line
            curCodeArea.replaceText(caretIdx, caretIdx+curLineText.length(),
                    curLineUncommented);
            return;
        }
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