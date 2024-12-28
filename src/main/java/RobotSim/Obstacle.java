package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The Obstacle class represents a static obstacle in the RobotArena.
 * It is drawn as a red square and inherits from the abstract ArenaItem class.
 */
public class Obstacle extends ArenaItem {

    /**
     * Constructs an Obstacle with a specified position and radius.
     * @param x The X-coordinate of the obstacle
     * @param y The Y-coordinate of the obstacle
     * @param radius The radius of the obstacle
     */
    public Obstacle(double x, double y, double radius) {
        super(x, y, radius);
    }

    /**
     * Draws the obstacle as a red square on the canvas.
     * @param gc The GraphicsContext used for drawing
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.RED); // Set the fill color for the obstacle
        gc.fillRect(x - radius, y - radius, radius * 2, radius * 2); // Draw the square
    }

    /**
     * Adds additional behavior for future functionality, if needed.
     * Currently, this method is a placeholder for obstacle interactions.
     */
    public void interact() {
        // Placeholder for future obstacle interaction logic
    }
}
