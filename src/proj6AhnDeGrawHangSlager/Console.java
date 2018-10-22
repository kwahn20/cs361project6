package proj6AhnDeGrawHangSlager;

import org.fxmisc.richtext.StyleClassedTextArea;


public class Console extends StyleClassedTextArea {
    public Console(){
        super();
    }

    public String getCommand(){
        String newLine = System.getProperty("line.separator");
        String[] lines = this.getText().split("\n");
        int newLineIndex = lines.length-1;
        return lines[newLineIndex]+"\n";
    }
}