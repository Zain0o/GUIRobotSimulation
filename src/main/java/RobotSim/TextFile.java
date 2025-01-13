package RobotSim;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TextFile class provides utility methods for reading from, writing to,
 * appending to, and deleting text files. It is designed to handle file
 * input/output operations for saving and loading the arena state.
 */
public class TextFile {

    // Initialize the Logger for this class
    private static final Logger logger = Logger.getLogger(TextFile.class.getName());

    /**
     * Writes the given content to the specified file.
     * If the file exists, it will be overwritten; otherwise, a new file will be created.
     *
     * @param filename The name or path of the file to write to.
     * @param content  The content to write into the file.
     * @return True if the file was successfully written, false otherwise.
     */
    public static boolean writeFile(String filename, String content) {
        Path path = Paths.get(filename);
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.log(Level.INFO, "File saved successfully: {0}", filename);
            return true; // Indicates a successful write operation
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to file: " + filename, e);
            return false; // Indicates that the write operation failed
        }
    }

    /**
     * Appends the given content to the specified file.
     * If the file does not exist, it will be created.
     *
     * @param filename The name or path of the file to append to.
     * @param content  The content to append to the file.
     * @return True if the content was successfully appended, false otherwise.
     */
    public static boolean appendFile(String filename, String content) {
        Path path = Paths.get(filename);
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.log(Level.INFO, "Content appended successfully to file: {0}", filename);
            return true; // Indicates a successful append operation
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error appending to file: " + filename, e);
            return false; // Indicates that the append operation failed
        }
    }

    /**
     * Reads the entire content from the specified file using UTF-8 encoding.
     *
     * @param filename The name or path of the file to read from.
     * @return The content of the file as a String, or null if an error occurs.
     */
    public static String readFile(String filename) {
        Path path = Paths.get(filename);
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            logger.log(Level.INFO, "File read successfully: {0}", filename);
            return content;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading from file: " + filename, e);
            return null; // Indicates that the read operation failed
        }
    }

    /**
     * Deletes the specified file if it exists.
     *
     * @param filename The name or path of the file to delete.
     * @return True if the file was successfully deleted, false otherwise.
     */
    public static boolean deleteFile(String filename) {
        Path path = Paths.get(filename);
        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                logger.log(Level.INFO, "File deleted successfully: {0}", filename);
            } else {
                logger.log(Level.WARNING, "File not found, cannot delete: {0}", filename);
            }
            return deleted; // True if the file was deleted, false otherwise
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error deleting file: " + filename, e);
            return false; // Indicates that the delete operation failed
        }
    }
}
