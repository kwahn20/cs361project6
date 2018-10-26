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

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.Stack;

import java.util.Arrays;
import java.util.ArrayList;
import javafx.util.Pair;


/**
 * This is the controller class for all of the edit functions
 * within the edit menu.
 *
 * @author Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou (Project 5)
 * @author Kevin Ahn, Lucas DeGraw, Jackie Hang, Kyle Slager
 * @version 3.0
 * @since 10-3-2018
 */
public class EditController {

    // Reference to the tab pane of the IDE
    private TabPane tabPane;
    private TextField findTextEntry;
    // fields relating to string finding
    private String fileTextSearched;
    private ArrayList<Integer> matchStartingIndices;
    private int curMatchLength;
    private int curMatchHighlightedIdx;
    private Button prevMatchBtn;
    private Button nextMatchBtn;



    /**
     * Constructor for the class. Initializes
     * the current tab to null
     */
    public EditController(TabPane tabPane, TextField findTextEntry, Button prevMatchBtn, Button nextMatchBtn) {
        this.tabPane = tabPane;
        this.findTextEntry = findTextEntry;
        this.matchStartingIndices = new ArrayList<>();
        this.prevMatchBtn = prevMatchBtn;
        this.nextMatchBtn = nextMatchBtn;
        this.resetFindMatchingStringFields();
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
        curCodeArea.lineStart(SelectionPolicy.ADJUST);

        // get caret index location in file
        int caretIdx = curCodeArea.getCaretPosition();

        // temporarily highlight the current line to get its text as a string
        curCodeArea.selectLine();
        String curLineText = curCodeArea.getSelectedText();
        curCodeArea.deselect();

        // regex to check if current line is commented
        if (Pattern.matches(" *\\/\\/.*", curLineText)) {

            // uncomment the line by taking out the first instance of "//"
            String curLineUncommented =
                    curLineText.replaceFirst("//", "");

            // replace the current line with the newly commented line
            curCodeArea.replaceText(caretIdx, caretIdx + curLineText.length(),
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
            showAlert("Please Highlight a Bracket!");
            return;
        } else if (highlightedText.length() == 1) {

            // true if matching a closing character to an opening character,
            // false if matching an opening character to a closing character
            Boolean findClosingCharacter;

            if (highlightedText.equals("{") || highlightedText.equals("[")
                    || highlightedText.equals("(")) {
                findClosingCharacter = true;
            } else if (highlightedText.equals("}") || highlightedText.equals("]")
                    || highlightedText.equals(")")) {
                findClosingCharacter = false;
            } else {
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

//            boolean parsingString = false;
//            String strChar = null;
//            String strCharToMatch = null;

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
                    String curChar = curJavaCodeArea.getText(i, i + 1);

                    // check that the character is not not written as a string "(" or '('
                    try {
                        if (curJavaCodeArea.getText(i - 1, i).equals("\"")
                                && curJavaCodeArea.getText(i + 1, i + 2).equals("\"")
                                || curJavaCodeArea.getText(i - 1, i).equals("'")
                                && curJavaCodeArea.getText(i + 1, i + 2).equals("'")) {
                            System.out.println("continuing");
                            continue;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println(e);
                    }
//                    if (strChar != null && curChar.equals(strChar)) {
//                        parsingString = !parsingString;
//                        strChar
//                    }

//                    if (!parsingString) {
                        // pop the top opening char off the stack if its closing match is found,
                        // otherwise push the newly found opening char onto the stack
                        switch (curChar) {
                            case ("]"):
                                if (openingMatchCharacter.equals("[")) charStack.pop();
                                break;
                            case (")"):
                                if (openingMatchCharacter.equals("(")) charStack.pop();
                                break;
                            case ("}"):
                                if (openingMatchCharacter.equals("{")) charStack.pop();
                                break;
                            case ("["):
                                charStack.push(curChar);
                                break;
                            case ("("):
                                charStack.push(curChar);
                                break;
                            case ("{"):
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
//                    }
                }
                System.out.println(Arrays.toString(charStack.toArray()));
                System.out.println("matching closing character not found");
                showAlert("MATCHING CLOSING CHARACTER NOT FOUND");
                return;
            } else {
                String closingMatchCharacter;
                int idxBeforeCharToMatch = highlightedCharRange.getStart();
                // search backward through file
                for (int i = idxBeforeCharToMatch; i > 0; i--) {

                    // get closing character on top of the stack
                    closingMatchCharacter = charStack.peek();

                    // check that the character is not not written as a string "(" or '('
//                    try {
//                        if (curJavaCodeArea.getText(i - 2, i - 1).equals("\"")
//                                && curJavaCodeArea.getText(i, i + 1).equals("\"")
//                                || curJavaCodeArea.getText(i - 2, i - 1).equals("'")
//                                && curJavaCodeArea.getText(i, i + 1).equals("'")) {
//                            System.out.println("continuing");
//                            continue;
//                        }
//                    } catch (IndexOutOfBoundsException e) {
//                        System.out.println(e);
//                    }

                    // pop the top opening char off the stack if its closing match is found,
                    // otherwise push the newly found opening char onto the stack
                    // current character being checked for a closing bracket match
                    String curChar = curJavaCodeArea.getText(i - 1, i);

                    switch (curChar) {
                        case ("["):
                            if (closingMatchCharacter.equals("]")) charStack.pop();
                            break;
                        case ("("):
                            if (closingMatchCharacter.equals(")")) charStack.pop();
                            break;
                        case ("{"):
                            if (closingMatchCharacter.equals("}")) charStack.pop();
                            break;
                        case ("]"):
                            charStack.push(curChar);
                            break;
                        case (")"):
                            charStack.push(curChar);
                            break;
                        case ("}"):
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
        } else {
            System.out.println("must select opening/closing parenthesis/bracket/brace");
            showAlert("VALID CHARACTERS ARE A SINGLE '{', '}', '[', ']', '(' or ')'");
        }

    }

    /**
     * creates and displays an informational alert
     *
     * @param header the content of the alert
     */
    private void showAlert(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.show();
    }

    /**
     * Entabs the selected text
     */

    public void handleEnTabbing() {
        //lines is an array of lines separated by a \n character
        String[] lines;
        int caretIdx;
        JavaCodeArea curCodeArea = getCurJavaCodeArea();

        String selectedText = curCodeArea.getSelectedText();


        if (selectedText.equals("")) {
            curCodeArea.selectLine();
            selectedText = curCodeArea.getSelectedText();
            curCodeArea.deselect();
            curCodeArea.lineStart(SelectionPolicy.ADJUST);
            caretIdx = curCodeArea.getCaretPosition();
            lines = selectedText.split("\\n");

        } else {

            lines = selectedText.split("\\n");
            caretIdx = curCodeArea.getText().indexOf(selectedText);

            //moves caret to the front of the selected text
            curCodeArea.moveTo(caretIdx);
            caretIdx = curCodeArea.getCaretPosition();
        }


        for (int i = 0; i < lines.length; i++) {
            singleLineTabbing(caretIdx);

            // incrementing the caret index by the number of characters
            // in the line and +2 for the new line character at the end
            caretIdx += lines[i].length() + 2;
        }
    }

    /**
     * Detabs the selected text
     */

    public void handleDeTabbing() {
        JavaCodeArea curCodeArea = getCurJavaCodeArea();
        String selectedText = curCodeArea.getSelectedText();

        //lines is an array of lines separated by a \n character
        String[] lines;
        int caretIdx;
        if (selectedText.equals("")) {
            curCodeArea.selectLine();
            selectedText = curCodeArea.getSelectedText();
            curCodeArea.deselect();
            curCodeArea.lineStart(SelectionPolicy.ADJUST);
            caretIdx = curCodeArea.getCaretPosition();
            lines = selectedText.split("\\n");
        } else {
            lines = selectedText.split("\\n");
            caretIdx = curCodeArea.getText().indexOf(selectedText);
            curCodeArea.moveTo(caretIdx);
            curCodeArea.lineStart(SelectionPolicy.ADJUST);
            caretIdx = curCodeArea.getCaretPosition();
        }


        for (int i = 0; i < lines.length; i++) {
            curCodeArea.moveTo(caretIdx);
            String curLineText = lines[i];
            singleLineDeTabbing(curLineText, caretIdx);

            // incrementing the caret index by the number of characters
            // in the line and +2 for the new line character at the end
            caretIdx += lines[i].length() + 2;
        }
    }

    /**
     * Entabs a single line
     */
    private void singleLineTabbing(int caretIdx) {
        JavaCodeArea curCodeArea = getCurJavaCodeArea();
        curCodeArea.replaceText(caretIdx, caretIdx, "\t");
    }

    /**
     * Detabs a single line
     */
    private void singleLineDeTabbing(String curLineText, int caretIdx) {

        JavaCodeArea curCodeArea = getCurJavaCodeArea();

        // regex to check if current line is commented
        if (Pattern.matches("(?:[ \\t].*)", curLineText)) {
            // detabs the line by taking out the first instance of a tab
            String curLineUncommented = curLineText.replaceFirst("[ \\t]", "");

            curCodeArea.moveTo(caretIdx);
            curCodeArea.lineStart(SelectionPolicy.ADJUST);
            caretIdx = curCodeArea.getCaretPosition();

            // replace the current line with the newly commented line
            curCodeArea.replaceText(caretIdx, caretIdx + curLineText.length(),
                    curLineUncommented);
            return;
        }
    }


    /**
     * @return the JavaCodeArea currently in focus of the TabPane
     */
    public JavaCodeArea getCurJavaCodeArea() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane<CodeArea> curPane =
                (VirtualizedScrollPane<CodeArea>) curTab.getContent();
        return (JavaCodeArea) curPane.getContent();
    }


    /**
     * searches for the text entered in the "Find" TextField
     * shows appropriate error message if nothing found or provided as search string
     * enables the Previous and Next buttons if more than one match is found
     */
    public void handleFindText() {

        String textToFind = this.findTextEntry.getText();
        int textToFindLength = textToFind.length();

        // check if some text was searched for
        if (textToFindLength > 0) {
            System.out.println("textToFind: " + textToFind);

            // get current file's text
            String openFileText = getCurJavaCodeArea().getText();

            // get index of first match, -1 if no matches
            int index = openFileText.indexOf(textToFind);

            // check if any match was found
            if (index != -1) {

                // build list of starting indices
                this.matchStartingIndices.clear();
                while (index >= 0) {
                    this.matchStartingIndices.add(index);
                    index = openFileText.indexOf(textToFind, index + 1);

                }
                // print found indices
                for (Integer idx : this.matchStartingIndices) {
                    System.out.println("idx: " + idx);
                }
                // save text of searched file
                this.fileTextSearched = openFileText;

                // currently highlighted match is at the first index (0) of the match indices array
                this.curMatchHighlightedIdx = 0;

                // save length of valid match
                this.curMatchLength = textToFindLength;

                // get starting index in file of first found match
                int highlightStartIdx = this.matchStartingIndices.get(0);

                // highlight first found match
                getCurJavaCodeArea().selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);

                // notify the user of search results
                showAlert(this.matchStartingIndices.size() + " MATCHES FOUND");

                // enable the Previous and Next buttons if more than 1 match is found
                if (this.matchStartingIndices.size() > 1) this.setMatchNavButtonsClickable(true);

                return;
            }
            resetFindMatchingStringFields();
            showAlert("NO MATCH FOUND");
            return;
        }
        resetFindMatchingStringFields();
        showAlert("NOTHING TO SEARCH FOR");
    }


    /**
     * highlights the previous match if there are multiple matches found in the file
     */
    public void handleHighlightPrevMatch() {

        if (this.canHighlightMatches()) {

            JavaCodeArea curJavaCodeArea = getCurJavaCodeArea();

            // if first match highlighted, highlight the last match
            if (this.curMatchHighlightedIdx == 0) {

                // get index of match located last in file
                int highlightStartIdx = this.matchStartingIndices.get(
                        this.matchStartingIndices.size()-1);

                // highlight this last match
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);

                // update the index of the currently highlighted match
                this.curMatchHighlightedIdx = this.matchStartingIndices.size()-1;
            }
            // otherwise highlight the previous match
            else {
                // decrement index of highlighted match
                this.curMatchHighlightedIdx--;

                // get starting index in file of preceding match
                int highlightStartIdx = this.matchStartingIndices.get(
                        this.curMatchHighlightedIdx);

                // highlight match preceding currently highlighted match
                curJavaCodeArea.selectRange( highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);
            }
            return;
        }
    }

    /**
     *
     */
    public void handleHighlightNextMatch() {
        System.out.println("handlingNext");


        if (this.matchStartingIndices.size() == 1) {
            showAlert("ONLY 1 MATCH");
            return;
        }
        if (this.canHighlightMatches()) {

            System.out.println("highlightingNext");
            JavaCodeArea curJavaCodeArea = getCurJavaCodeArea();

            // if last match in file highlighted, wrap around to highlight the first match
            if (this.curMatchHighlightedIdx == this.matchStartingIndices.size()-1) {
                System.out.println("wrapping back to highlight last match");
                // get index of match located last in file

                int highlightStartIdx = this.matchStartingIndices.get(0);
                // highlight the match located first in the file
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);

                // update the index of the currently highlighted match
                this.curMatchHighlightedIdx = 0;
            }
            // otherwise highlight the previous match
            else {
                // increment index of highlighted match
                this.curMatchHighlightedIdx++;

                // get starting index in file of next match
                int highlightStartIdx = this.matchStartingIndices.get(
                        this.curMatchHighlightedIdx);

                // highlight match after currently highlighted match
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);
            }
            return;
        }
    }

    /**
     *
     * @return true if any matches from Find can currently be highlighted, else false
     */
    private boolean canHighlightMatches() {
        String openFileText = getCurJavaCodeArea().getText();
        System.out.println("searched: " + this.fileTextSearched);
        System.out.println("current: " + openFileText);

        // check if anything searched for
        if (this.fileTextSearched == null || this.curMatchHighlightedIdx == -1
                || this.curMatchLength == -1) {
            showAlert("MUST SEARCH FOR SOME TEXT");
            return false;
        }
        // check if any matches found
        if (this.matchStartingIndices.size() == 0) {
            showAlert("NO MATCHES TO HIGHLIGHT");
            return false;
        }
        // check if the file has been changed since the last search
        if (!this.fileTextSearched.equals(openFileText)) {
            showAlert("FILE HAS BEEN CHANGED SINCE PREVIOUS SEARCH, FIND AGAIN");
            return false;
        }
        return true;
    }

    /**
     * resets the fields used for string searching in the file when no match is found
     */
    private void resetFindMatchingStringFields() {
        this.fileTextSearched = null;
        this.curMatchLength = -1;
        this.curMatchHighlightedIdx = -1;
        this.setMatchNavButtonsClickable(false);
    }

    /**
     * enables or disables the Previous and Next match navigation buttons
     * @param enable boolean denoting whether or not the Previous & Next buttons
     *               are enabled
     */
    private void setMatchNavButtonsClickable(boolean enable) {
        this.prevMatchBtn.setDisable(!enable);
        this.nextMatchBtn.setDisable(!enable);
    }
}