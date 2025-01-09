package RobotSim;

import javafx.scene.canvas.GraphicsContext;

/**
 * An abstract class representing any item in the arena (e.g., Robot, Obstacle).
 * Each item has:
 * <ul>
 *   <li>An (x, y) center position.</li>
 *   <li>A bounding circle radius.</li>
 *   <li>A unique integer ID (auto-incremented).</li>
 *   <li>Flags for selection and highlighting.</li>
 * </ul>
 *
 * Subclasses must implement the {@link #draw(GraphicsContext)} method to define
 * how they visually appear on screen.
 */
public abstract class ArenaItem {
    protected double x, y;        // Center coordinates
    protected double radius;      // Bounding circle radius

    protected static int idCounter = 0;  // Auto-increment ID source
    protected final int id;             // Unique ID for this item

    protected boolean isSelected = false;    // If the item is selected
    protected boolean isHighlighted = false; // If the item is highlighted (e.g., hovered)

    /**
     * Constructs a new ArenaItem with a given center (x, y) and bounding radius.
     *
     * @param x      The x-coordinate of the item's center.
     * @param y      The y-coordinate of the item's center.
     * @param radius The bounding circle radius of the item.
     */
    public ArenaItem(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.id = idCounter++;
    }

    /**
     * Subclasses must provide a way to draw themselves onto a given {@link GraphicsContext}.
     *
     * @param gc The graphics context on which to draw.
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Calculates the distance between this item and another item.
     * If {@code other} is {@code null}, returns {@link Double#MAX_VALUE}.
     *
     * @param other Another ArenaItem (may be {@code null}).
     * @return The Euclidean distance between centers, or {@code Double.MAX_VALUE} if {@code other} is null.
     */
    public double calculateDistance(ArenaItem other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Checks if this item intersects (collides with) another item.
     * Intersection is determined by whether the distance between centers
     * is less than the sum of their radii.
     *
     * @param other Another ArenaItem.
     * @return {@code true} if the two items' bounding circles intersect, otherwise {@code false}.
     */
    public boolean intersects(ArenaItem other) {
        if (other == null) {
            return false;
        }
        double distance = calculateDistance(other);
        return distance < (this.radius + other.radius);
    }

    /**
     * Checks whether the given point (px, py) lies inside this item's bounding circle.
     *
     * @param px The x-coordinate of the test point.
     * @param py The y-coordinate of the test point.
     * @return {@code true} if (px, py) is within this item's radius, otherwise {@code false}.
     */
    public boolean containsPoint(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= radius;
    }

    /**
     * Marks this item as selected.
     *
     * @param selected {@code true} to select, {@code false} to deselect.
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    /**
     * Marks this item as highlighted (e.g., hovered).
     *
     * @param highlighted {@code true} to highlight, {@code false} otherwise.
     */
    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
    }

    /**
     * Provides a string representation of the ArenaItem.
     * Subclasses can override this method to include more detailed information.
     *
     * @return A string containing the item's class name, ID, and position.
     */
    @Override
    public String toString() {
        return String.format("%s (ID: %d) - Position: (%.2f, %.2f)",
                this.getClass().getSimpleName(), id, x, y);
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    /**
     * @return The x-coordinate of this item's center.
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of this item's center.
     *
     * @param x The new x-coordinate.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return The y-coordinate of this item's center.
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of this item's center.
     *
     * @param y The new y-coordinate.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return The radius of this item's bounding circle.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of this item's bounding circle.
     *
     * @param radius The new radius.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return The unique ID of this item.
     */
    public int getId() {
        return id;
    }

    /**
     * @return {@code true} if this item is marked as selected.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * @return {@code true} if this item is highlighted (e.g., hovered).
     */
    public boolean isHighlighted() {
        return isHighlighted;
    }
}
