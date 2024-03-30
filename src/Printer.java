/**
 * @file src/Printer.java
 * @brief Printer class used for writing to the .log and .html files for logging and debugging.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 1 grace day
 */

package src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;

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

    /**
     * Resolves the actual filename from a File enum value
     * @return [String] The filename
     */
    public static String getFilenameFromFile(File file) {
        if(file.equals(File.CLIENT))
            return "client";
        else if(file.equals(File.SERVER))
            return "server";
        else
            return "";
    }

    /**
     * Initializes the writer and buffered writer
     */
    public static void start() {
        writer = null;
        bwriter = null;
    }

    /**
     * Prints to the console and the specified file
     * @param arg The string to print, delimited by '|' for HTML
     * @param file The enum value of the file to write to
     * @param id An optional identifier for the file
     */
    public static synchronized void print(String arg, File file, String id) {
        try {
            // Write to the .log file as is
            String filename = "../log/" + getFilenameFromFile(file) + id + ".log";
            writer = new FileWriter(filename, true);
            bwriter = new BufferedWriter(writer);
            bwriter.write(arg + "\n");
            bwriter.flush();

            // Write to the HTML file with a table row delimited by '|'
            String htmlFilename = "../log/html/" + getFilenameFromFile(file) + id + ".html";
            FileWriter htmlWriter = new FileWriter(htmlFilename, true);
            BufferedWriter htmlBwriter = new BufferedWriter(htmlWriter);
            String[] args = arg.split("\\|");
            htmlBwriter.write("<tr>\n");
            for(String a : args) {
                htmlBwriter.write("<td style='padding: 0 5px; border: 1px solid black;'>" + a + "</td>\n");
            }
            htmlBwriter.write("</tr>\n");
            htmlBwriter.flush();
            htmlBwriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }   

    /**
     * Prints to the console and the specified file
     * @param arg The string to print, delimited by '|' for HTML
     * @param file The enum value of the file to write to
     * @param id An optional identifier for the file
     * @param htmlColor The color to use for the HTML row
     */
    public static synchronized void print(String arg, File file, String id, String htmlColor) {
        try {
            // System.out.println(arg);
            String filename = "../log/" + getFilenameFromFile(file) + id + ".log";
            writer = new FileWriter(filename, true);
            bwriter = new BufferedWriter(writer);
            bwriter.write(arg + "\n");
            bwriter.flush();

            // Write to the html file - with specified color
            String htmlFilename = "../log/html/" + getFilenameFromFile(file) + id + ".html";
            FileWriter htmlWriter = new FileWriter(htmlFilename, true);
            BufferedWriter htmlBwriter = new BufferedWriter(htmlWriter);
            String[] args = arg.split("\\|");
            htmlBwriter.write("<tr style='background-color:" + htmlColor + ";'>\n");
            for(String a : args) {
                htmlBwriter.write("<td style='padding: 0 5px; border: 1px solid black;'>" + a.trim() + "</td>\n");
            }
            htmlBwriter.write("</tr>\n");
            htmlBwriter.flush();
            htmlBwriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the HTML log file, writes HTML header and starts the table
     * @param file The file to write to
     * @param id The identifier for the file
     * @param links The links to other logs
     */
    public static synchronized void initHtmlLog(File file, String id, String links) {
        try {
            String fileString = getFilenameFromFile(file);
            String htmlFilename = "../log/html/" + fileString + id + ".html";
            FileWriter htmlWriter = new FileWriter(htmlFilename, false);
            BufferedWriter htmlBwriter = new BufferedWriter(htmlWriter);
            htmlBwriter.write("<html>\n<head>\n<title>" + fileString + " log</title>\n</head>\n<body><h1>" + fileString + id + " log</h1><p>" + LocalDateTime.now() + "</p>\n" + links + "\n");

            // Write the header row for a client log
            if(fileString.contains("client")) {
                htmlBwriter.write("<table style='width: 100%; position: relative;'><tr style='position: sticky; top: 0; background: white;'>" + 
                    "<th style='border: 1px solid black;'>Thread</th>" +
                    "<th style='border: 1px solid black;'>Server</th>" +
                    "<th style='border: 1px solid black;'>Operation</th>" +
                    "<th style='border: 1px solid black;'>Timestamp</th>" +
                    "<th style='border: 1px solid black;'>Message</th>" +
                    "<th style='border: 1px solid black;'>Parameters</th></tr>\n"
                );
            }
            // Write the header row for a server log
            else {
                htmlBwriter.write("<table style='width: 100%; position: relative;'><tr style='position: sticky; top: 0; background: white;'>" + 
                    "<th style='border: 1px solid black;'>Server</th>" +
                    "<th style='border: 1px solid black;'>Operation</th>" +
                    "<th style='border: 1px solid black;'>Timestamp</th>" +
                    "<th style='border: 1px solid black;'>Lamport Clock</th>" +
                    "<th style='border: 1px solid black;'>Origin</th>" +
                    "<th style='border: 1px solid black;'>Message</th>" + 
                    "<th style='border: 1px solid black;'>Parameters</th></tr>\n"
                );
            }

            htmlBwriter.flush();
            htmlBwriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the HTML log file, writes the closing table and body tags
     * @param file The file to write to
     * @param id The identifier for the file
     */
    public static synchronized void closeHtmlLog(File file, String id) {
        try {
            String htmlFilename = "../log/html/" + getFilenameFromFile(file) + id + ".html";
            FileWriter htmlWriter = new FileWriter(htmlFilename, true);
            BufferedWriter htmlBwriter = new BufferedWriter(htmlWriter);
            htmlBwriter.write("</table>\n</body>\n</html>\n");
            htmlBwriter.flush();
            htmlBwriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the writer and buffered writer
     */
    public static void close() {
        try {
            bwriter.close();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
