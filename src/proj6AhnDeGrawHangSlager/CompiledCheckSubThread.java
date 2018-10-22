package proj6AhnDeGrawHangSlager;

import java.io.IOException;

/**
 * This class runs a sub-thread to check if a compiled java
 * file has no errors, and prints any errors if it does
 */
public class CompiledCheckSubThread implements Runnable{

    // Reference to the compiling process, used in both threads
    private Process compileProcess;

    /**
     * Constructor for this thread
     * @param compProcess the javac process referred to in both threads
     */
    public CompiledCheckSubThread(Process compProcess) {
        compileProcess = compProcess;
    }


    /**
     * Assuming the compiling process has finished, check for any errors
     */
    @Override
    public void run() {
        System.out.println("Check if file compiled - START "+Thread.currentThread().getName());
        try{
            System.out.println("HI");
            // If javac has finished compiling the java
            if( compileProcess.exitValue() == 0 ){

                // If -1 wasn't returned, there is/are error(s)
                if( compileProcess.getErrorStream().read() != -1 ){
                    System.out.println("Compilation Errors " + compileProcess.getErrorStream());
                }
                else{
                    System.out.println("Output " + compileProcess.getInputStream());
                }
            }

        }
        catch (IOException e){

        }

        System.out.println("Check if file compiled - END "+Thread.currentThread().getName());
    }


}