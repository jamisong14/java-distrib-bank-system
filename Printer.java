/**
 * Authors:
 *  - Jamison Grudem(grude013)
 *  - Manan Mrig (mrig0001)
 */

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * class Printer
 * @description A utility class used to print to the console and to a file in one statement
 */
public class Printer {

    // Stores the file writer
    static FileWriter writer;
    // Stores the buffered writer
    static BufferedWriter bwriter;

    // Stores the possible files to write to
    public static enum File { 
        CLIENT,
        SERVER
    }

    // Resolves the actual filename from a File enum value
    public static String getFilenameFromFile(File file) {
        if(file.equals(File.CLIENT))
            return "client";
        else if(file.equals(File.SERVER))
            return "server";
        else
            return "";
    }

    // Initializes the writer and buffered writer
    public static void start() {
        writer = null;
        bwriter = null;
    }

    // Prints to the console and the specified file
    public static void print(String arg, File file, String id) {
        try {
            // System.out.println(arg);
            String filename = getFilenameFromFile(file) + id + ".log";
            writer = new FileWriter(filename, true);
            bwriter = new BufferedWriter(writer);
            bwriter.write(arg + "\n");
            bwriter.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }   

    // Closes the writer and buffered writer
    public static void close() {
        try {
            bwriter.close();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
