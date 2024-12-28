package RobotSim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The TextFile class provides utility methods for reading from and writing to text files.
 * It is designed to handle file input/output operations for saving and loading the arena state.
 */
public class TextFile {

    /**
     * Writes the given content to the specified file.
     * If the file exists, it will be overwritten; otherwise, a new file will be created.
     *
     * @param filename The name or path of the file to write to.
     * @param content  The content to write into the file.
     * @return True if the file was successfully written, false otherwise.
     */
    public static boolean writeFile(String filename, String content) {
        try {
            Files.write(Paths.get(filename), content.getBytes());
            System.out.println("File saved successfully: " + filename);
            return true; // Indicates a successful write operation
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return false; // Indicates that the write operation failed
        }
    }

    /**
     * Reads the entire content from the specified file.
     *
     * @param filename The name or path of the file to read from.
     * @return The content of the file as a String, or null if an error occurs.
     */
    public static String readFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
            return null; // Indicates that the read operation failed
        }
    }

    /**
     * Deletes the specified file.
     *
     * @param filename The name or path of the file to delete.
     * @return True if the file was successfully deleted, false otherwise.
     */
    public static boolean deleteFile(String filename) {
        try {
            return Files.deleteIfExists(Paths.get(filename));
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false; // Indicates that the delete operation failed
        }
    }
}
