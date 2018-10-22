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

import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import javafx.scene.input.KeyEvent;
import java.io.*;

import java.util.concurrent.*;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;


/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically the compile, compile and run, and stop buttons
 *
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @version 1.0
 * @since   10-3-2018
 *
 */
public class ToolbarController {

    //private Thread compileRunThread;
    private FutureTask<Boolean> curTask;
    private Console console;
    private boolean receivedCommand = false;

    ToolbarController(Console console){
        this.console = console;
    }

    // Compiles the code currently open, assuming it has been saved.
    public void handleCompile(String filename) throws InterruptedException {
        Thread compileThread = new Thread(()->compileFile(filename));
        compileThread.start();
        compileThread.join();
    }

    // Calls compile and runs the code
    public void handleCompileAndRun(String filename) {
        Thread compileRunThread = new Thread(() -> compileRunFile(filename));
        compileRunThread.start();
    }

    /**
     * Stops all currently compiling files and any currently running Java programs
     */
    public void handleStop(){
        this.curTask.cancel(true);
        this.console.appendText("Process terminated." + System.getProperty("line.separator"));
        Platform.runLater(() -> this.console.moveTo(this.console.getText().length()));
        Platform.runLater(() -> this.console.requestFollowCaret());
    }

    public void handleUserKeypress(KeyEvent ke){
        if(ke.getCode() == KeyCode.ENTER){
            receivedCommand = true;
        }
    }

    public String handleCompileSave() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Want to save before compiling?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yesButton){
            return "yesButton";
        }
        else if(result.get() == noButton){
            return "noButton";
        }
        else{
            return "cancelButton";
        }

    }

    private boolean compileFile(String filename){
        CompileTask compileTask = new CompileTask(filename,this.console);
        this.curTask = new FutureTask<Boolean>(compileTask);
        ExecutorService compileExecutor = Executors.newFixedThreadPool(1);
        compileExecutor.execute(curTask);
        try {
            Boolean compSuccessful = curTask.get();
            if (compSuccessful) {
                Platform.runLater(()->this.console.appendText("Compilation was Successful." + System.getProperty("line.separator")));
                Platform.runLater(() -> this.console.moveTo(this.console.getText().length()));
                Platform.runLater(() -> this.console.requestFollowCaret());
            }
            compileExecutor.shutdown();
            return compSuccessful;
        } catch(ExecutionException | InterruptedException | CancellationException e) {
            compileTask.stop();
            return false;
        }
    }

    private void compileRunFile(String filename){
        boolean compSuccessful = compileFile(filename);
        if(!compSuccessful){
            return;
        }
        RunTask runTask = new RunTask(filename,console);
        this.curTask = new FutureTask<Boolean>(runTask);
        ExecutorService curExecutor = Executors.newFixedThreadPool(1);
        curExecutor.execute(this.curTask);
        try{
            curExecutor.shutdown();
        }catch (CancellationException e){runTask.stop();}

    }

    private class RunTask implements Callable{
        private String classFilename;
        private String filepath;
        private Process curProcess;
        private Console console;
        RunTask(String fileNameWithPath, Console console){
            this.console = console;
            int pathLength = fileNameWithPath.length();
            File file = new File(fileNameWithPath);
            String filename = file.getName();
            this.filepath = fileNameWithPath.substring(0,pathLength-filename.length());
            int nameLength = filename.length();
            this.classFilename = filename.substring(0, nameLength - 5);
        }

        @Override
        public Boolean call() throws IOException{
            ProcessBuilder pb = new ProcessBuilder("java","-cp",this.filepath ,this.classFilename);
            this.curProcess = pb.start();
            BufferedReader stdInput, stdError;
            BufferedWriter stdOutput;
            stdInput = new BufferedReader(new InputStreamReader(this.curProcess.getInputStream()));

            stdError = new BufferedReader(new InputStreamReader(this.curProcess.getErrorStream()));

            stdOutput = new BufferedWriter((new OutputStreamWriter(this.curProcess.getOutputStream())));

            String newLine = System.getProperty("line.separator");
            String inputLine;
            String errorLine = null;
            new Thread(()->{
                while(this.curProcess.isAlive()){
                    if(receivedCommand){
                        try {
                            stdOutput.write(this.console.getCommand());
                            receivedCommand = false;
                            stdOutput.flush();
                        }catch (IOException e){e.printStackTrace();}
                    }
                }
            }).start();

            while ((inputLine = stdInput.readLine()) != null || (errorLine = stdError.readLine()) != null){

                final String finalInputLine = inputLine;
                final String finalErrorLine = errorLine;

                if (finalInputLine != null) {
                    Platform.runLater(() -> this.console.appendText(finalInputLine));
                    Platform.runLater(() -> this.console.appendText(newLine));
                }

                if(finalErrorLine != null) {
                    Platform.runLater(() -> this.console.appendText(finalErrorLine));
                    Platform.runLater(() -> this.console.appendText(newLine));
                }
                Platform.runLater(() -> this.console.moveTo(this.console.getText().length()));
                Platform.runLater(() -> this.console.requestFollowCaret());
                try {
                    Thread.sleep(50);
                }catch (InterruptedException e){
                    this.stop();
                    return false;

                }
            }
            stdError.close();
            stdInput.close();
            stdOutput.close();
            return true;
        }

        public void stop(){
            if(this.curProcess != null){
                curProcess.destroyForcibly();
            }
        }
    }

    private class CompileTask implements Callable{
        private String filename;
        private Process curProcess;
        private Console console;
        CompileTask(String filename,Console console){
            this.console = console;
            this.filename = filename;
        }

        @Override
        public Boolean call() throws IOException {
            ProcessBuilder pb = new ProcessBuilder("javac", filename);
            this.curProcess = pb.start();
            BufferedReader stdError;

            stdError = new BufferedReader(new InputStreamReader(this.curProcess.getErrorStream()));

            String line;
            String newLine = System.getProperty("line.separator");
            Boolean compSuccessful = true;
            while ((line = stdError.readLine()) != null){
                compSuccessful = false;
                final String finalLine = line;
                Platform.runLater(() -> this.console.appendText(finalLine));
                Platform.runLater(() -> this.console.appendText(newLine));
            }
            stdError.close();

            return compSuccessful;
        }

        public void stop(){
            if(this.curProcess != null){
                curProcess.destroyForcibly();
            }
        }

    }

}