package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * The Robot class represents a mobile robot within the RobotArena.
 * It includes enhanced visualization features such as improved wheels, sensors,
 * metallic effects, direction indicators, and selection highlights.
 * The robot can move autonomously, detect collisions, and respond to user interactions.
 */
public class Robot extends ArenaItem {

    // Sensor and movement properties
    protected double sensorRange = 50;     // Original circle-based sensor range
    protected double speed = 2.0;          // Movement speed (pixels per frame)
    protected double direction = Math.random() * 360; // Random initial direction in degrees

    // Whisker (line-based sensor) properties
    private double whiskerLength = 40;     // Length of the whisker line

    // Selection state
    private boolean isSelected = false;    // Indicates if the robot is selected

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
     * Draws the robot on the given GraphicsContext with enhanced visualization.
     * The robot is rendered as:
     * 1. A blue circle with a metallic effect representing the body.
     * 2. Wheels with detailed treads.
     * 3. Enhanced sensors including sensor range and whiskers.
     * 4. A direction indicator.
     * 5. A selection highlight if the robot is selected.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body with metallic effect
        gc.setFill(Color.web("#3498db"));
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw metallic gradient overlay
        RadialGradient gradient = new RadialGradient(
                0, 0, x, y, radius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE.deriveColor(0, 1, 1, 0.3)),
                new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(gradient);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw wheels
        drawWheels(gc);

        // Draw enhanced sensors
        drawEnhancedSensors(gc);

        // Draw direction indicator
        drawDirectionIndicator(gc);

        // Draw selection highlight if selected
        if (isSelected) {
            drawSelectionEffect(gc);
        }
    }

    /**
     * Draws wheels with detailed treads and rotation based on direction.
     * Renamed to 'drawWheels' and made protected so it can be accessed by child classes.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    protected void drawWheels(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(direction);

        // Draw treads
        double treadWidth = radius * 0.3;
        double treadLength = radius * 1.8;
        gc.setFill(Color.DARKGRAY);

        // Left tread with detail
        gc.fillRoundRect(-radius - treadWidth, -treadLength / 2,
                treadWidth, treadLength, 5, 5);

        // Right tread with detail
        gc.fillRoundRect(radius, -treadLength / 2,
                treadWidth, treadLength, 5, 5);

        // Tread details
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (double i = -treadLength / 2; i < treadLength / 2; i += treadLength / 8) {
            // Left tread lines
            gc.strokeLine(-radius - treadWidth, i, -radius, i);
            // Right tread lines
            gc.strokeLine(radius, i, radius + treadWidth, i);
        }

        gc.restore();
    }

    /**
     * Draws enhanced sensors including sensor range and multiple whiskers.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    private void drawEnhancedSensors(GraphicsContext gc) {
        // Draw sensor range indicator
        gc.setFill(Color.LIGHTBLUE);
        gc.setGlobalAlpha(0.2);
        gc.fillOval(x - sensorRange, y - sensorRange,
                sensorRange * 2, sensorRange * 2);
        gc.setGlobalAlpha(1.0);

        // Draw whisker sensors
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        double radians = Math.toRadians(direction);

        // Center whisker
        drawWhisker(gc, radians);
        // Left whisker
        drawWhisker(gc, radians - Math.PI / 6);
        // Right whisker
        drawWhisker(gc, radians + Math.PI / 6);
    }

    /**
     * Draws a single whisker line based on the given angle.
     *
     * @param gc     The GraphicsContext used for drawing.
     * @param angle  The angle in radians for the whisker direction.
     */
    private void drawWhisker(GraphicsContext gc, double angle) {
        double whiskerEndX = x + whiskerLength * Math.cos(angle);
        double whiskerEndY = y + whiskerLength * Math.sin(angle);
        gc.strokeLine(x, y, whiskerEndX, whiskerEndY);
    }

    /**
     * Draws a direction indicator to show the robot's current facing direction.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    private void drawDirectionIndicator(GraphicsContext gc) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);
        double indicatorLength = radius * 1.2;
        double radians = Math.toRadians(direction);
        double endX = x + indicatorLength * Math.cos(radians);
        double endY = y + indicatorLength * Math.sin(radians);
        gc.strokeLine(x, y, endX, endY);
    }

    /**
     * Draws a selection highlight around the robot when it is selected.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    private void drawSelectionEffect(GraphicsContext gc) {
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        gc.strokeOval(x - radius - 5, y - radius - 5,
                (radius + 5) * 2, (radius + 5) * 2);
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
     * Sets the selection state of the robot.
     *
     * @param selected True if the robot is selected, false otherwise.
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    /**
     * @return The selection state of the robot.
     */
    public boolean isSelected() {
        return isSelected;
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
