package proj6AhnDeGrawHangSlager;

/**
 * This class creates the compile subthread, waits until it is done, and
 * then tells the other thread to check for errors
 */
public class CompilerMainThread{

    // The subthread that compiles the file
    private Thread javacSubThread;

    // The subclass that checks if compilation was successful and prints any error(s)
    private Thread compiledCheckSubThread;

    // Reference to the compiling process, used in both threads
    private Process compileProcess;

    /**
     * Constructor for this thread
     * @param filename
     */
    public CompilerMainThread(String filename)
    {
        javacSubThread  = new Thread(new JavacSubThread(filename, compileProcess));

    }

    public void runThreads()
    {
        try{
            javacSubThread.start();
            compiledCheckSubThread = new Thread(new CompiledCheckSubThread(compileProcess));
            javacSubThread.join();
            compiledCheckSubThread.start();
        }
        catch (InterruptedException e)
        {

        }


        //compiledCheckSubThread.join();
    }
}