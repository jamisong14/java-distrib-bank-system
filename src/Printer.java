/**
 * Authors:
 *  - Jamison Grudem(grude013)
 */

package src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;

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
    public static synchronized void print(String arg, File file, String id) {
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

    // Prints to the console and the specified file
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
