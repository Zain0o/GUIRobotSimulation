package GUI;

import RobotSim.ArenaItem;
import RobotSim.Obstacle;
import RobotSim.Robot;
import RobotSim.RobotArena;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;

/**
 * The ArenaView class is responsible for rendering the Robot Arena,
 * handling user interactions such as selection, drag-and-drop,
 * and context menus for robots and obstacles.
 */
public class ArenaView {

    private final Canvas canvas;
    private final RobotArena robotArena;
    private final DropShadow glow;

    // Variables for Drag-and-Drop functionality
    private double dragStartX, dragStartY;
    private ArenaItem draggedItem;

    /**
     * Constructor to initialize the ArenaView with the given RobotArena.
     *
     * @param robotArena The RobotArena instance to be visualized.
     */
    public ArenaView(RobotArena robotArena) {
        this.robotArena = robotArena;
        // If you want to let the parent resize the Canvas automatically, you can do:
        // this.canvas = new Canvas(); // rely on parent to set size
        // Otherwise, keep default size for clarity:
        this.canvas = new Canvas(800, 600);

        this.canvas.setFocusTraversable(true);

        // Initialize DropShadow effect for selected items
        this.glow = new DropShadow();
        glow.setColor(Color.YELLOW);
        glow.setRadius(15); // Increased radius for better visibility

        // Initialize Drag-and-Drop and Context Menu Handling
        initializeDragHandling();
        initializeContextMenu();

        // Initialize hover effects for better user feedback
        initializeHoverEffects();
    }

    /**
     * Returns the Canvas used for rendering the arena.
     *
     * @return The Canvas object.
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Renders the current state of the arena, including the background,
     * grid, border, all items, selection highlights, hover highlights,
     * and status information.
     */
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Optionally fill a subtle background color before drawing the grid
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the grid
        drawEnhancedGrid(gc);

        // Draw the arena items (robots, obstacles, etc.)
        robotArena.drawItems(gc);

        // Draw selection highlight if something is selected
        if (robotArena.getSelectedItem() != null) {
            drawSelectionEffect(gc, robotArena.getSelectedItem());
        }

        // Draw hover highlight if hovering over something that's not selected
        if (robotArena.getHoveredItem() != null &&
                robotArena.getHoveredItem() != robotArena.getSelectedItem()) {
            drawHoverEffect(gc, robotArena.getHoveredItem());
        }

        // Draw stats (e.g. arena size, selected item info)
        drawEnhancedStats(gc);

        // Finally, add a subtle, rounded border around the canvas
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(2);
        // We offset by 1 pixel so the stroke isn't clipped at the edge of the canvas
        // 15,15 are the corner arc widths (adjust to taste)
        gc.strokeRoundRect(1, 1, canvas.getWidth() - 2, canvas.getHeight() - 2, 15, 15);
    }

    /**
     * Draws some textual stats (arena size, selected item) on the canvas.
     */
    private void drawEnhancedStats(GraphicsContext gc) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));

        String stats = String.format("Arena Size: %.0fx%.0f",
                canvas.getWidth(),
                canvas.getHeight());
        gc.fillText(stats, 15, 25);

        if (robotArena.getSelectedItem() != null) {
            ArenaItem item = robotArena.getSelectedItem();
            String info = String.format("%s (ID: %d) at (%.0f, %.0f)",
                    item.getClass().getSimpleName(), item.getId(),
                    item.getX(), item.getY());
            gc.fillText(info, 15, 45);
        }
    }

    /**
     * Draws a selection highlight around the given ArenaItem (in orange).
     */
    private void drawSelectionEffect(GraphicsContext gc, ArenaItem selectedItem) {
        double x = selectedItem.getX();
        double y = selectedItem.getY();
        double r = selectedItem.getRadius();

        gc.setEffect(glow);
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(4);
        gc.strokeOval(x - r - 10, y - r - 10, (r + 10) * 2, (r + 10) * 2);
        gc.setEffect(null);
    }

    /**
     * Draws a hover highlight around the given ArenaItem (in light green).
     */
    private void drawHoverEffect(GraphicsContext gc, ArenaItem hoveredItem) {
        double x = hoveredItem.getX();
        double y = hoveredItem.getY();
        double r = hoveredItem.getRadius();

        DropShadow hoverGlow = new DropShadow();
        hoverGlow.setColor(Color.LIGHTGREEN);
        hoverGlow.setRadius(10);

        gc.setEffect(hoverGlow);
        gc.setStroke(Color.LIGHTGREEN);
        gc.setLineWidth(2);
        gc.strokeOval(x - r - 5, y - r - 5, (r + 5) * 2, (r + 5) * 2);
        gc.setEffect(null);
    }

    /**
     * Updates the size of the canvas and re-renders the arena.
     *
     * @param width  The new width of the canvas.
     * @param height The new height of the canvas.
     */
    public void updateCanvasSize(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
        render();
    }

    /**
     * Sets the color of the glow effect used for highlighting selected items.
     *
     * @param color The Color to set for the glow effect.
     */
    public void setGlowColor(Color color) {
        glow.setColor(color);
    }

    /**
     * Initializes drag-and-drop event handlers for the canvas.
     * Handles mouse press, drag, and release events to enable dragging
     * of robots and obstacles within the arena.
     */
    private void initializeDragHandling() {
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                draggedItem = robotArena.findItemAt(e.getX(), e.getY());
                if (draggedItem != null) {
                    dragStartX = e.getX() - draggedItem.getX();
                    dragStartY = e.getY() - draggedItem.getY();
                    robotArena.setSelectedItem(draggedItem);
                    render();
                } else {
                    robotArena.setSelectedItem(null);
                    render();
                }
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (draggedItem != null) {
                double newX = e.getX() - dragStartX;
                double newY = e.getY() - dragStartY;

                // Ensure the new position is within arena bounds
                newX = Math.max(draggedItem.getRadius(),
                        Math.min(robotArena.getWidth() - draggedItem.getRadius(), newX));
                newY = Math.max(draggedItem.getRadius(),
                        Math.min(robotArena.getHeight() - draggedItem.getRadius(), newY));

                // Check for collisions before moving
                if (!robotArena.isColliding(newX, newY, draggedItem.getRadius(), draggedItem.getId())) {
                    draggedItem.setX(newX);
                    draggedItem.setY(newY);
                    render();
                }
            }
        });

        canvas.setOnMouseReleased(e -> draggedItem = null);
    }

    /**
     * Initializes right-click context menu for the canvas.
     * Provides options to delete items, view item information,
     * and adjust robot speed if applicable.
     */
    private void initializeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        canvas.setOnContextMenuRequested(e -> {
            contextMenu.getItems().clear();
            ArenaItem item = robotArena.findItemAt(e.getX(), e.getY());

            if (item != null) {
                // Delete Menu Item
                MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction(event -> {
                    robotArena.deleteItem(item);
                    render();
                    updateStatus("Deleted: " + item.getClass().getSimpleName());
                });

                // Info Menu Item
                MenuItem infoItem = new MenuItem("Info");
                infoItem.setOnAction(event -> showItemInfo(item));

                contextMenu.getItems().addAll(deleteItem, infoItem);

                // Adjust Speed Menu Item (only for Robots)
                if (item instanceof Robot) {
                    MenuItem speedItem = new MenuItem("Adjust Speed");
                    speedItem.setOnAction(event -> showSpeedDialog((Robot) item));
                    contextMenu.getItems().add(speedItem);
                }
            }

            // Show context menu if there are items to display
            if (!contextMenu.getItems().isEmpty()) {
                Window window = canvas.getScene().getWindow();
                contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
            }
        });
    }

    /**
     * Displays an information dialog about the selected ArenaItem.
     *
     * @param item The ArenaItem whose information is to be displayed.
     */
    private void showItemInfo(ArenaItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Info");
        alert.setHeaderText(null);
        alert.setContentText(item.toString());
        alert.showAndWait();
    }

    /**
     * Displays a dialog allowing the user to adjust the speed of a Robot.
     *
     * @param robot The Robot whose speed is to be adjusted.
     */
    private void showSpeedDialog(Robot robot) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Adjust Speed");

        // Create a slider for speed adjustment
        Slider speedSlider = new Slider(0.1, 5.0, robot.getSpeed());
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setMinorTickCount(4);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.setPrefWidth(300);

        // Tooltip for slider
        Tooltip tooltip = new Tooltip("Slide to adjust the robot's speed.");
        Tooltip.install(speedSlider, tooltip);

        // Layout for the dialog
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(new Label("Adjust Robot Speed:"), speedSlider);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert the result to the slider value when the OK button is clicked
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return speedSlider.getValue();
            }
            return null;
        });

        // Handle the dialog result
        dialog.showAndWait().ifPresent(speed -> {
            robot.setSpeed(speed);
            render();
            updateStatus("Robot speed adjusted to " + speed + "x.");
        });
    }

    /**
     * Initializes hover effects for items to provide visual feedback.
     */
    private void initializeHoverEffects() {
        canvas.setOnMouseMoved(e -> {
            ArenaItem hoveredItem = robotArena.findItemAt(e.getX(), e.getY());
            if (hoveredItem != robotArena.getSelectedItem()) {
                robotArena.setHoveredItem(hoveredItem);
                render();
            }
        });

        canvas.setOnMouseExited(e -> {
            robotArena.setHoveredItem(null);
            render();
        });
    }

    /**
     * Updates the status label with the given message.
     *
     * @param message The status message to display.
     */
    private void updateStatus(String message) {
        // If there's a linkage to a status label in your ControlPanel,
        // you can call it here. For now, we assume an external mechanism
        // does the actual update. This is just a placeholder.
    }

    /**
     * Draws a grid (with optional dotted lines) and then a standard rectangular border.
     * Now replaced by a subtle *rounded* border in the render() method.
     */
    private void drawEnhancedGrid(GraphicsContext gc) {
        // Optionally set dotted lines (uncomment if desired):
        // gc.setLineDashes(6, 4);

        gc.setStroke(Color.web("#e8e8e8"));
        gc.setLineWidth(0.5);
        double spacing = 40;

        // Draw vertical lines
        for (double x = 0; x <= canvas.getWidth(); x += spacing) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }

        // Draw horizontal lines
        for (double y = 0; y <= canvas.getHeight(); y += spacing) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }

        // If you want a standard border (not rounded), you could keep this:
        // gc.setLineDashes(null);
        // gc.setStroke(Color.web("#bdc3c7"));
        // gc.setLineWidth(2);
        // gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //
        // But we've replaced it with strokeRoundRect(...) in render().
    }
}
