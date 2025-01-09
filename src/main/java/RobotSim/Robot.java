package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The Robot class represents a mobile robot within the RobotArena.
 * It includes a "whisker" line sensor to detect obstacles,
 * a translucent sensor range circle ("beam"), and two small black "wheels" (or treads).
 * The robot can move autonomously, detect collisions, and respond to user interactions.
 */
public class Robot extends ArenaItem {

    // Sensor and movement properties
    protected double sensorRange = 50;     // Original circle-based sensor range
    protected double speed = 2.0;          // Movement speed (pixels per frame)
    protected double direction = Math.random() * 360; // Random initial direction in degrees

    // Whisker (line-based sensor) properties
    private double whiskerLength = 40;     // Length of the whisker line

    /**
     * Constructs a Robot with a specified position and radius.
     *
     * @param x      The X-coordinate of the robot's center.
     * @param y      The Y-coordinate of the robot's center.
     * @param radius The radius of the robot's bounding circle.
     */
    public Robot(double x, double y, double radius) {
        super(x, y, radius);
    }

    /**
     * Draws the robot on the given GraphicsContext.
     * The robot is rendered as:
     * 1. A blue circle representing the body.
     * 2. Two small black circles representing wheels.
     * 3. A translucent light blue circle indicating sensor range.
     * 4. A black line ("whisker") indicating the sensor direction.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // 1) Draw the robot body (blue circle)
        gc.setFill(Color.BLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // 2) Draw two "wheels" on the left and right sides
        gc.setFill(Color.BLACK);
        double wheelRadius = radius * 0.3; // Adjust as needed for bigger/smaller wheels

        // Left wheel (around left edge of the robot body)
        gc.fillOval(
                (x - radius) - wheelRadius,
                y - wheelRadius,
                wheelRadius * 2,
                wheelRadius * 2
        );

        // Right wheel (around right edge of the robot body)
        gc.fillOval(
                (x + radius) - wheelRadius,
                y - wheelRadius,
                wheelRadius * 2,
                wheelRadius * 2
        );

        // 3) Draw the sensor range circle (translucent light blue)
        gc.setFill(Color.LIGHTBLUE);
        gc.setGlobalAlpha(0.3);
        gc.fillOval(x - sensorRange, y - sensorRange, sensorRange * 2, sensorRange * 2);
        gc.setGlobalAlpha(1.0); // Reset alpha

        // 4) Draw the whisker line (black line indicating sensor direction)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        double radians = Math.toRadians(direction);
        double whiskerEndX = x + whiskerLength * Math.cos(radians);
        double whiskerEndY = y + whiskerLength * Math.sin(radians);
        gc.strokeLine(x, y, whiskerEndX, whiskerEndY);
    }

    /**
     * Draws additional wheels or treads. Subclasses can utilize this method
     * to render their specific wheel designs.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    protected void drawWheels(GraphicsContext gc) {
        gc.save();  // Save current transform
        gc.translate(x, y);
        gc.rotate(direction);

        // Draw treads
        gc.setFill(Color.BLACK);
        double treadWidth = radius * 0.3;
        double treadLength = radius * 1.8;

        // Left tread
        gc.fillRect(-radius - treadWidth, -treadLength / 2, treadWidth, treadLength);
        // Right tread
        gc.fillRect(radius, -treadLength / 2, treadWidth, treadLength);

        // Optional: draw tread lines for visual detail
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        for (double yy = -treadLength / 2; yy < treadLength / 2; yy += treadLength / 6) {
            gc.strokeLine(-radius - treadWidth, yy, -radius, yy);
            gc.strokeLine(radius, yy, radius + treadWidth, yy);
        }

        gc.restore();  // Restore original transform
    }

    /**
     * Moves the robot within the arena.
     * The movement involves:
     * 1. Checking the whisker sensor for obstacles. If a collision is detected, the robot reverses direction.
     * 2. Updating the (x, y) position based on speed and direction.
     * 3. Handling boundary collisions by bouncing off walls.
     * 4. Optionally checking the circle-based sensor to avoid obstacles.
     *
     * @param arena The RobotArena instance managing the arena.
     */
    public void move(RobotArena arena) {
        // 1) Whisker sensor check for obstacle collision
        if (detectWhiskerCollision(arena)) {
            direction += 180; // Reverse direction upon collision
            direction = normalizeDirection(direction);
        }

        // 2) Calculate movement deltas based on current direction and speed
        double radians = Math.toRadians(direction);
        double deltaX = speed * Math.cos(radians);
        double deltaY = speed * Math.sin(radians);
        x += deltaX;
        y += deltaY;

        // 3) Handle boundary collisions (bounce off walls)
        handleBoundaryCollision(arena);

        // 4) Optional: Circle-based obstacle avoidance (additional collision check)
        if (arena.isObstacleNearby(this)) {
            direction += 180; // Reverse direction to avoid obstacle
            direction = normalizeDirection(direction);
        }
    }

    /**
     * Handles boundary collisions by bouncing the robot off the arena walls.
     * If the robot hits a horizontal wall, it inverts its vertical direction.
     * If it hits a vertical wall, it inverts its horizontal direction.
     *
     * @param arena The RobotArena instance managing the arena.
     */
    public void handleBoundaryCollision(RobotArena arena) {
        boolean bounced = false;

        // Check collision with left or right walls
        if (x <= radius) {
            x = radius; // Clamp to boundary
            direction = 180 - direction;
            bounced = true;
        } else if (x >= arena.getWidth() - radius) {
            x = arena.getWidth() - radius; // Clamp to boundary
            direction = 180 - direction;
            bounced = true;
        }

        // Check collision with top or bottom walls
        if (y <= radius) {
            y = radius; // Clamp to boundary
            direction = 360 - direction;
            bounced = true;
        } else if (y >= arena.getHeight() - radius) {
            y = arena.getHeight() - radius; // Clamp to boundary
            direction = 360 - direction;
            bounced = true;
        }

        if (bounced) {
            direction = normalizeDirection(direction);
        }
    }

    /**
     * Detects if the whisker line in front of the robot intersects any obstacle in the arena.
     *
     * @param arena The RobotArena instance managing the arena.
     * @return True if a collision is detected, false otherwise.
     */
    private boolean detectWhiskerCollision(RobotArena arena) {
        double radians = Math.toRadians(direction);
        double whiskerEndX = x + whiskerLength * Math.cos(radians);
        double whiskerEndY = y + whiskerLength * Math.sin(radians);
        Line whisker = new Line(x, y, whiskerEndX, whiskerEndY);
        return arena.intersectsAnyObstacle(whisker);
    }

    /**
     * Normalizes the direction angle to ensure it remains within [0, 360) degrees.
     *
     * @param dir The direction angle to normalize.
     * @return The normalized direction angle.
     */
    protected double normalizeDirection(double dir) {
        dir %= 360;
        if (dir < 0) {
            dir += 360;
        }
        return dir;
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------

    /**
     * @return The sensor range of the robot.
     */
    public double getSensorRange() {
        return sensorRange;
    }

    /**
     * Sets the sensor range of the robot.
     *
     * @param sensorRange The new sensor range.
     */
    public void setSensorRange(double sensorRange) {
        this.sensorRange = sensorRange;
    }

    /**
     * @return The current movement speed of the robot.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Sets the movement speed of the robot.
     *
     * @param speed The new speed value.
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return The current movement direction of the robot in degrees.
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Sets the movement direction of the robot.
     *
     * @param direction The new direction in degrees.
     */
    public void setDirection(double direction) {
        this.direction = normalizeDirection(direction);
    }

    /**
     * @return The length of the whisker line sensor.
     */
    public double getWhiskerLength() {
        return whiskerLength;
    }

    /**
     * Sets the length of the whisker line sensor.
     *
     * @param whiskerLength The new whisker length.
     */
    public void setWhiskerLength(double whiskerLength) {
        this.whiskerLength = whiskerLength;
    }

    /**
     * @return The sensor range of the robot.
     */
    public double getSensorRangeValue() {
        return sensorRange;
    }

    /**
     * Sets the sensor range of the robot.
     *
     * @param sensorRange The new sensor range.
     */
    public void setSensorRangeValue(double sensorRange) {
        this.sensorRange = sensorRange;
    }

    /**
     * @return The current speed of the robot.
     */
    public double getCurrentSpeed() {
        return speed;
    }

    /**
     * Sets the current speed of the robot.
     *
     * @param speed The new speed value.
     */
    public void setCurrentSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return The current movement direction of the robot in degrees.
     */
    public double getCurrentDirection() {
        return direction;
    }

    /**
     * Sets the current movement direction of the robot.
     *
     * @param direction The new direction in degrees.
     */
    public void setCurrentDirection(double direction) {
        this.direction = normalizeDirection(direction);
    }

    /**
     * @return The current length of the whisker line sensor.
     */
    public double getCurrentWhiskerLength() {
        return whiskerLength;
    }

    /**
     * Sets the current length of the whisker line sensor.
     *
     * @param whiskerLength The new whisker length.
     */
    public void setCurrentWhiskerLength(double whiskerLength) {
        this.whiskerLength = whiskerLength;
    }

    /**
     * Provides a string representation of the Robot.
     *
     * @return A string containing the robot's type, ID, and position.
     */
    @Override
    public String toString() {
        return String.format("%s (ID: %d)\nPosition: (%.2f, %.2f)\nSpeed: %.2f\nDirection: %.2f°",
                this.getClass().getSimpleName(),
                getId(),
                getX(),
                getY(),
                speed,
                direction);
    }
}
