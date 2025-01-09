package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * The RobotArena class manages the items (robots and obstacles) in the arena and their interactions.
 * It provides methods to add new items, render them, handle their movement and collisions,
 * and manage the selection of items.
 */
public class RobotArena {

    private final ArrayList<ArenaItem> items;
    private double width;
    private double height;
    private ArenaItem selectedItem;
    private ArenaItem hoveredItem; // Added for hover handling

    /**
     * Constructs a new RobotArena with specified dimensions.
     *
     * @param width  The width of the arena.
     * @param height The height of the arena.
     * @throws IllegalArgumentException if width or height is non-positive.
     */
    public RobotArena(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Arena dimensions must be positive.");
        }
        this.width = width;
        this.height = height;
        items = new ArrayList<>();
    }

    /**
     * Updates the simulation speed by setting the speed of all robots.
     *
     * @param speed The speed multiplier to set.
     */
    public void updateSimulationSpeed(double speed) {
        for (ArenaItem item : items) {
            if (item instanceof Robot robot) {
                robot.setSpeed(speed); // Set speed directly instead of multiplying
            }
        }
    }

    /**
     * Clears all items from the arena, resetting it to an empty state.
     */
    public void clearArena() {
        items.clear();
        selectedItem = null;
        hoveredItem = null; // Clear hovered item as well
    }

    /**
     * Adds a new basic Robot at a random non-overlapping position.
     */
    public void addRobot() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 20));
        items.add(new Robot(x, y, 20));
    }

    /**
     * Adds a new ChaserRobot at a random non-overlapping position.
     */
    public void addChaserRobot() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 20));
        items.add(new ChaserRobot(x, y, 20));
    }

    /**
     * Adds a new BumpRobot at a random non-overlapping position.
     */
    public void addBumpRobot() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 20));
        items.add(new BumpRobot(x, y, 20));
    }

    /**
     * Adds a new BeamRobot at a random non-overlapping position.
     */
    public void addBeamRobot() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 20));
        items.add(new BeamRobot(x, y, 20));
    }

    /**
     * Adds a new Obstacle at a random non-overlapping position.
     */
    public void addObstacle() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 15));
        items.add(new Obstacle(x, y, 15));
    }

    /**
     * Moves all Robot-derived items in the arena by invoking their move() methods.
     */
    public void moveRobots() {
        for (ArenaItem item : items) {
            if (item instanceof Robot robot) {
                robot.move(this);
            }
        }
    }

    /**
     * Finds and returns the topmost ArenaItem at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate to search.
     * @param y The y-coordinate to search.
     * @return The ArenaItem at the specified location, or null if none found.
     */
    public ArenaItem findItemAt(double x, double y) {
        for (int i = items.size() - 1; i >= 0; i--) {
            ArenaItem item = items.get(i);
            double dist = Math.sqrt(Math.pow(x - item.getX(), 2) + Math.pow(y - item.getY(), 2));
            if (dist <= item.getRadius()) {
                return item;
            }
        }
        return null;
    }

    /**
     * Checks if placing a new item at (x, y) with radius 'radius' would overlap existing items.
     *
     * @param x      The x-coordinate of the new position.
     * @param y      The y-coordinate of the new position.
     * @param radius The radius of the item being placed.
     * @return True if overlapping occurs, false otherwise.
     */
    private boolean isOverlapping(double x, double y, double radius) {
        ArenaItem tempItem = new Robot(x, y, radius); // Using Robot as a generic ArenaItem for overlap checking
        for (ArenaItem item : items) {
            if (isColliding(tempItem, item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether two ArenaItems are colliding based on their distances.
     *
     * @param a The first ArenaItem.
     * @param b The second ArenaItem.
     * @return True if the two items' bounding circles intersect, false otherwise.
     */
    private boolean isColliding(ArenaItem a, ArenaItem b) {
        double distance = a.calculateDistance(b);
        return distance < (a.getRadius() + b.getRadius());
    }

    /**
     * Checks if placing an item at (x, y) with radius 'radius' would cause a collision
     * with existing items, excluding the item with the specified robotId.
     *
     * @param x        The x-coordinate of the new position.
     * @param y        The y-coordinate of the new position.
     * @param radius   The radius of the item being placed.
     * @param robotId  The unique identifier of the item being moved (to exclude from collision check).
     * @return True if a collision would occur, false otherwise.
     */
    public boolean isColliding(double x, double y, double radius, int robotId) {
        for (ArenaItem item : items) {
            // If the item is the same robot, skip
            if (item instanceof Robot r) {
                if (r.getId() == robotId) {
                    continue;
                }
            }
            double dx = x - item.getX();
            double dy = y - item.getY();
            double distSq = dx * dx + dy * dy;
            double combinedRadius = radius + item.getRadius();
            if (distSq < combinedRadius * combinedRadius) {
                return true; // Collision detected
            }
        }
        return false; // No collision
    }

    /**
     * Checks if any obstacle is within the combined radius of the robot's sensor.
     *
     * @param robot The Robot to check against obstacles.
     * @return True if an obstacle is nearby, false otherwise.
     */
    public boolean isObstacleNearby(Robot robot) {
        for (ArenaItem item : items) {
            if (item instanceof Obstacle obstacle) {
                double distance = robot.calculateDistance(obstacle);
                if (distance <= (robot.getRadius() + robot.getSensorRange() + obstacle.getRadius())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Renders all items onto the given GraphicsContext.
     *
     * @param gc The GraphicsContext to draw on.
     */
    public void drawItems(GraphicsContext gc) {
        // First draw all unselected items
        for (ArenaItem item : items) {
            if (item != selectedItem) {
                item.draw(gc);
            }
        }

        // Draw selected item last with highlight
        if (selectedItem != null) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(
                    selectedItem.getX() - selectedItem.getRadius() - 5,
                    selectedItem.getY() - selectedItem.getRadius() - 5,
                    (selectedItem.getRadius() + 5) * 2,
                    (selectedItem.getRadius() + 5) * 2
            );
            selectedItem.draw(gc);
        }
    }

    /**
     * Draws the arena, including the grid and all items.
     *
     * @param gc The GraphicsContext to draw on.
     */
    public void drawArena(GraphicsContext gc) {
        // Clear and draw grid
        gc.clearRect(0, 0, width, height);
        drawGrid(gc);

        // Draw all items
        drawItems(gc);
    }

    /**
     * Draws a grid on the arena for better visualization.
     *
     * @param gc The GraphicsContext of the arena.
     */
    private void drawGrid(GraphicsContext gc) {
        // Draw grid lines
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        double spacing = 20;

        for (double x = 0; x <= width; x += spacing) {
            gc.strokeLine(x, 0, x, height);
        }

        for (double y = 0; y <= height; y += spacing) {
            gc.strokeLine(0, y, width, y);
        }

        // Draw border
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Serializes the arena dimensions and each ArenaItem to a String.
     *
     * @return A String representing the serialized state of the arena.
     */
    public String saveArenaState() {
        StringBuilder sb = new StringBuilder();
        sb.append(width).append(" ").append(height).append("\n");
        for (ArenaItem item : items) {
            sb.append(item.getClass().getSimpleName()).append(" ")
                    .append(item.getX()).append(" ")
                    .append(item.getY()).append(" ")
                    .append(item.getRadius()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Deserializes the arena dimensions and items from a String.
     *
     * @param data The serialized state of the arena.
     */
    public void loadArenaState(String data) {
        if (data == null || data.isBlank()) {
            System.err.println("No data to load.");
            return;
        }
        items.clear();
        selectedItem = null; // Reset selection
        hoveredItem = null;  // Reset hovered item
        String[] lines = data.split("\n");

        String[] dimensions = lines[0].split(" ");
        if (dimensions.length != 2) {
            System.err.println("Invalid arena dimensions.");
            return;
        }

        width = Double.parseDouble(dimensions[0]);
        height = Double.parseDouble(dimensions[1]);

        for (int i = 1; i < lines.length; i++) {
            String[] details = lines[i].split(" ");
            if (details.length != 4) {
                System.err.println("Invalid item format at line " + (i + 1));
                continue;
            }
            String type = details[0];
            double x = Double.parseDouble(details[1]);
            double y = Double.parseDouble(details[2]);
            double radius = Double.parseDouble(details[3]);

            switch (type) {
                case "Robot"       -> items.add(new Robot(x, y, radius));
                case "ChaserRobot" -> items.add(new ChaserRobot(x, y, radius));
                case "BeamRobot"   -> items.add(new BeamRobot(x, y, radius));
                case "Obstacle"    -> items.add(new Obstacle(x, y, radius));
                case "BumpRobot"   -> items.add(new BumpRobot(x, y, radius));
                case "SmartRobot"  -> items.add(new SmartRobot(x, y, radius));
                default            -> System.err.println("Unknown item type: " + type);
            }
        }
    }

    /**
     * Provides a status string listing all items by class name and (x, y) coordinates.
     *
     * @return A String representing the current status of the arena.
     */
    public String getStatus() {
        StringBuilder status = new StringBuilder();
        for (ArenaItem item : items) {
            status.append(item.getClass().getSimpleName())
                    .append(" at (")
                    .append(Math.round(item.getX())).append(", ")
                    .append(Math.round(item.getY())).append(")\n");
        }
        return status.toString();
    }

    /**
     * Deletes the currently selected item from the arena, if any.
     */
    public void deleteSelectedItem() {
        if (selectedItem != null) {
            items.remove(selectedItem);
            selectedItem = null;
        }
    }

    /**
     * Deletes a specific ArenaItem from the arena.
     *
     * @param item The ArenaItem to be deleted.
     */
    public void deleteItem(ArenaItem item) {
        if (items.contains(item)) {
            items.remove(item);
            if (item.equals(selectedItem)) {
                selectedItem = null;
            }
        }
    }

    /**
     * Checks if the provided line intersects ANY obstacle in the arena.
     *
     * @param line A line to test (e.g., a "whisker" from a robot).
     * @return True if the line intersects any obstacle, false otherwise.
     */
    public boolean intersectsAnyObstacle(Line line) {
        for (ArenaItem item : items) {
            if (item instanceof Obstacle obstacle) {
                if (intersectsSquare(line, obstacle)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method to determine if a line intersects a square Obstacle.
     *
     * @param line     The line to test for intersection.
     * @param obstacle The Obstacle to check against.
     * @return True if the line intersects the obstacle, false otherwise.
     */
    private boolean intersectsSquare(Line line, Obstacle obstacle) {
        double ox = obstacle.getX();
        double oy = obstacle.getY();
        double r  = obstacle.getRadius();

        double left   = ox - r;
        double right  = ox + r;
        double top    = oy - r;
        double bottom = oy + r;

        Line topEdge    = new Line(left,  top,    right,  top);
        Line bottomEdge = new Line(left,  bottom, right,  bottom);
        Line leftEdge   = new Line(left,  top,    left,   bottom);
        Line rightEdge  = new Line(right, top,    right,  bottom);

        return  line.findintersection(topEdge)
                || line.findintersection(bottomEdge)
                || line.findintersection(leftEdge)
                || line.findintersection(rightEdge);
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    /**
     * Retrieves the list of all ArenaItems in the arena.
     *
     * @return An ArrayList of ArenaItems.
     */
    public ArrayList<ArenaItem> getItems() {
        return items;
    }

    /**
     * Retrieves the currently selected ArenaItem.
     *
     * @return The selected ArenaItem, or null if none is selected.
     */
    public ArenaItem getSelectedItem() {
        return selectedItem;
    }

    /**
     * Sets the currently selected ArenaItem.
     *
     * @param item The ArenaItem to select, or null to clear selection.
     */
    public void setSelectedItem(ArenaItem item) {
        this.selectedItem = item;
    }

    /**
     * Retrieves the currently hovered ArenaItem.
     *
     * @return The hovered ArenaItem, or null if none is hovered.
     */
    public ArenaItem getHoveredItem() {
        return hoveredItem;
    }

    /**
     * Sets the currently hovered ArenaItem.
     *
     * @param item The ArenaItem to set as hovered, or null to clear hover.
     */
    public void setHoveredItem(ArenaItem item) {
        this.hoveredItem = item;
    }

    /**
     * Retrieves the width of the arena.
     *
     * @return The width of the arena.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Retrieves the height of the arena.
     *
     * @return The height of the arena.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Provides information about the selected item as a String.
     *
     * @return A String containing details of the selected item, or "None" if no item is selected.
     */
    public String getSelectedItemInfo() {
        if (selectedItem != null) {
            return selectedItem.toString();
        }
        return "None";
    }
}
