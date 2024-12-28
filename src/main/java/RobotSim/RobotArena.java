package RobotSim;

import RobotSim.Line;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * The RobotArena class manages the items (robots and obstacles) in the arena and their interactions.
 * It provides methods to add new items, render them, and handle their movement and collisions.
 */
public class RobotArena {

    private final ArrayList<ArenaItem> items;
    private double width;
    private double height;
    private ArenaItem selectedItem;

    public RobotArena(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Arena dimensions must be positive.");
        }
        this.width = width;
        this.height = height;
        items = new ArrayList<>();
    }

    /** Clears all items (e.g., for "New Arena"). */
    public void clearArena() {
        items.clear();
        selectedItem = null;
    }

    public void addRobot() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 20));
        items.add(new Robot(x, y, 20));
    }

    public void addChaserRobot() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 20));
        items.add(new ChaserRobot(x, y, 20));
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

    public void addObstacle() {
        double x, y;
        do {
            x = Math.random() * width;
            y = Math.random() * height;
        } while (isOverlapping(x, y, 15));
        items.add(new Obstacle(x, y, 15));
    }

    /**
     * Moves all Robot-derived items in the arena by calling their move() methods.
     */
    public void moveRobots() {
        for (ArenaItem item : items) {
            if (item instanceof Robot robot) {
                robot.move(this);
            }
        }
    }

    /**
     * Finds an arena item that covers the given (x, y) position (e.g., for mouse clicks).
     * Returns the topmost item if multiple overlap. Otherwise returns null if none found.
     */
    public ArenaItem findItemAt(double x, double y) {
        // We'll check from front to back, so the last item in the list
        // is considered "on top."
        for (int i = items.size() - 1; i >= 0; i--) {
            ArenaItem item = items.get(i);
            double dist = Math.sqrt(Math.pow(x - item.getX(), 2) + Math.pow(y - item.getY(), 2));
            // If the click is within the radius of the item
            if (dist <= item.getRadius()) {
                return item;
            }
        }
        return null;
    }

    /**
     * Checks if placing a new item at (x, y) with radius 'radius' would overlap existing items.
     */
    private boolean isOverlapping(double x, double y, double radius) {
        Robot tempRobot = new Robot(x, y, radius);
        for (ArenaItem item : items) {
            if (isColliding(tempRobot, item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether two ArenaItems are colliding based on their distances.
     */
    private boolean isColliding(ArenaItem a, ArenaItem b) {
        double distance = a.calculateDistance(b);
        return distance < (a.getRadius() + b.getRadius());
    }

    /**
     * Legacy method: checks if any obstacle is within the combined radius of the robot's sensor.
     * (Used by Robot's simpler "radius-based" detection.)
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
            // Draw selection highlight
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(
                    selectedItem.getX() - selectedItem.getRadius() - 5,
                    selectedItem.getY() - selectedItem.getRadius() - 5,
                    (selectedItem.getRadius() + 5) * 2,
                    (selectedItem.getRadius() + 5) * 2
            );

            // Draw the selected item
            selectedItem.draw(gc);
        }
    }

    /**
     * Serializes the arena dimensions and each ArenaItem to a String.
     * Used for saving to a file.
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
     * Used for loading from a file.
     */
    public void loadArenaState(String data) {
        if (data == null || data.isBlank()) {
            System.err.println("No data to load.");
            return;
        }
        items.clear();
        selectedItem = null; // Reset selection
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
                default            -> System.err.println("Unknown item type: " + type);
            }
        }
    }

    /**
     * Returns a status string listing all items by class name and (x, y).
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
     * Delete the currently selected item (if any).
     */
    public void deleteSelectedItem() {
        if (selectedItem != null) {
            items.remove(selectedItem);
            selectedItem = null;
        }
    }

    /**
     * Checks if the provided line intersects ANY obstacle in the arena.
     *
     * @param line A line to test (e.g. a "whisker" from a robot).
     * @return true if line intersects any obstacle, false if it does not.
     */
    public boolean intersectsAnyObstacle(Line line) {
        for (ArenaItem item : items) {
            if (item instanceof Obstacle obstacle) {
                if (intersectsSquare(line, obstacle)) {
                    return true;  // as soon as we find one, we can return true
                }
            }
        }
        return false;
    }

    /**
     * Helper method to determine if a line intersects a square Obstacle.
     * @param line      The line to test
     * @param obstacle  The square obstacle (center (ox, oy), side length = 2*radius)
     * @return true if intersection is found
     */
    private boolean intersectsSquare(Line line, Obstacle obstacle) {
        double ox = obstacle.getX();
        double oy = obstacle.getY();
        double r  = obstacle.getRadius();

        // Square edges, if obstacle is drawn from (ox-r, oy-r) to (ox+r, oy+r).
        double left   = ox - r;
        double right  = ox + r;
        double top    = oy - r;
        double bottom = oy + r;

        // Build lines for each edge
        Line topEdge    = new Line(left,  top,    right,  top);
        Line bottomEdge = new Line(left,  bottom, right,  bottom);
        Line leftEdge   = new Line(left,  top,    left,   bottom);
        Line rightEdge  = new Line(right, top,    right,  bottom);

        // If the line intersects any edge, we say it intersects the obstacle
        return  line.findintersection(topEdge)    ||
                line.findintersection(bottomEdge) ||
                line.findintersection(leftEdge)   ||
                line.findintersection(rightEdge);
    }

    // Getters and setters
    public ArrayList<ArenaItem> getItems() {
        return items;
    }

    public ArenaItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(ArenaItem item) {
        this.selectedItem = item;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}