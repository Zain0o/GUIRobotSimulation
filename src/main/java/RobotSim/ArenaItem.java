package RobotSim;

import javafx.scene.canvas.GraphicsContext;

/**
 * The ArenaItem class serves as an abstract base for all items in the RobotArena.
 * Each item has a position (x, y) and a radius, and must implement a method to draw itself on the canvas.
 */
public abstract class ArenaItem {

    protected double x; // X-coordinate of the item
    protected double y; // Y-coordinate of the item
    protected double radius; // Radius of the item

    /**
     * Constructs an ArenaItem with a specified position and radius.
     * @param x The X-coordinate of the item
     * @param y The Y-coordinate of the item
     * @param radius The radius of the item
     */
    public ArenaItem(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Abstract method to draw the item on the canvas.
     * Each subclass must implement this method to define its visual representation.
     * @param gc The GraphicsContext used for drawing
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Calculates the distance from this item to another item.
     * @param other The other ArenaItem
     * @return The distance between the two items
     */
    public double calculateDistance(ArenaItem other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    // Getters and Setters
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
