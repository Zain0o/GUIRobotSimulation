package GUI;

import RobotSim.RobotArena;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The ArenaView class handles the graphical rendering of the RobotArena on a JavaFX Canvas.
 */
public class ArenaView {

    private final Canvas canvas;
    private final RobotArena robotArena;

    /**
     * Constructs an ArenaView with a specified RobotArena.
     */
    public ArenaView(RobotArena robotArena) {
        this.robotArena = robotArena;
        canvas = new Canvas(600, 400);
        canvas.setFocusTraversable(true);
    }

    /**
     * Returns the JavaFX Canvas used for rendering.
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Renders the arena and all items in it onto the canvas.
     */
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Clear
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Background
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Draw items
        robotArena.drawItems(gc);
    }

    /**
     * Updates the canvas size dynamically if the arena size changes.
     */
    public void updateCanvasSize(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
        render();
    }
}
