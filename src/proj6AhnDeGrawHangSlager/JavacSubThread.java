package proj6AhnDeGrawHangSlager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class runs a process to compile the file using javac
 */
public class JavacSubThread implements Runnable {

    // Name of the file we want to compile
    private String fileName;

    // Reference to the compiling process, used in both threads
    private Process compileProcess;

    /**
     * Constructor for this thread
     * @param filename the name of the file to compile
     * @param compProcess the javac process referred to in both threads
     */
    public JavacSubThread(String filename, Process compProcess) {
        fileName = filename;
        compileProcess = compProcess;
    }

    /**
     * Compile the file specified using javac
     */
    @Override
    public void run(){
        ProcessBuilder pb = new ProcessBuilder("javac", fileName);
        pb.redirectError();
        AtomicReference<String> result = new AtomicReference<>();

        try {
            compileProcess = pb.start();
            BufferedReader reader =
                    new BufferedReader(new FileReader(fileName));
            StringBuilder builder = new StringBuilder();
            String line = "";
            System.out.println(reader.readLine());
            while ( (line = reader.readLine()) != null) {
                System.out.println(line);
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            if(builder != null) {
                result.set(builder.toString());
            }

        }
        catch (IOException e){

        }

        if(result != null) {
            String str = result.toString();
            System.out.println(str);
        }
    }

//    /**
//     * Returns the compiling process so the other thread can check compiler success
//     * @return
//     */
//    public Process getCompileProcess()
//    {
//        return this.compileProcess;
//    }
}
