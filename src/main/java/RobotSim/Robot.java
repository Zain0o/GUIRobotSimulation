package RobotSim;

import RobotSim.Line;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The Robot class represents a mobile robot in the RobotArena.
 * Now it includes a "whisker" line sensor to detect obstacles directly ahead,
 * plus two small black "wheels" on its sides.
 */
public class Robot extends ArenaItem {

    protected double sensorRange = 50;     // Original circle-based sensor range
    protected double speed = 2;           // Movement speed
    protected double direction = Math.random() * 360; // Random initial direction in degrees

    // New field: length of the whisker line
    private double whiskerLength = 40;

    /**
     * Constructs a Robot with a specified position and radius.
     *
     * @param x      The X-coordinate of the robot
     * @param y      The Y-coordinate of the robot
     * @param radius The radius of the robot
     */
    public Robot(double x, double y, double radius) {
        super(x, y, radius);
    }

    /**
     * Draws the robot as a blue circle (body), two small black "wheels",
     * a translucent sensor range circle ("beam"), and a "whisker" line.
     *
     * @param gc The GraphicsContext used for drawing
     */
    @Override
    public void draw(GraphicsContext gc) {
        // 1) Draw the robot body (blue circle)
        gc.setFill(Color.BLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // 2) Draw two "wheels" on the left and right sides
        gc.setFill(Color.BLACK);
        double wheelRadius = radius * 0.3; // adjust as needed for bigger/smaller wheels

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

        // 3) (Optional) Draw the sensor range circle
        gc.setFill(Color.LIGHTBLUE);
        gc.setGlobalAlpha(0.3);
        gc.fillOval(x - sensorRange, y - sensorRange, sensorRange * 2, sensorRange * 2);
        gc.setGlobalAlpha(1.0);

        // 4) Draw the whisker line (line-based sensor)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        double radians = Math.toRadians(direction);
        double x2 = x + whiskerLength * Math.cos(radians);
        double y2 = y + whiskerLength * Math.sin(radians);
        gc.strokeLine(x, y, x2, y2);
    }

    /**
     * Moves the robot within the arena. First checks a whisker sensor for obstacles
     * and, if found, reverses direction. Then does normal movement, boundary checks,
     * and optionally checks the circle-based sensor for obstacles.
     *
     * Removed @Override because ArenaItem doesn't define move(RobotArena).
     */
    public void move(RobotArena arena) {
        // 1) Whisker sensor check
        if (detectWhiskerCollision(arena)) {
            direction += 180;
            direction = normalizeDirection(direction);
        }

        // 2) Normal movement
        double radians = Math.toRadians(direction);
        double deltaX = speed * Math.cos(radians);
        double deltaY = speed * Math.sin(radians);

        x += deltaX;
        y += deltaY;

        // 3) Boundary collision checks
        if (x <= radius || x >= arena.getWidth() - radius) {
            direction = 180 - direction;
            direction = normalizeDirection(direction);
        }
        if (y <= radius || y >= arena.getHeight() - radius) {
            direction = 360 - direction;
            direction = normalizeDirection(direction);
        }

        // 4) (Optional) circle-based obstacle avoidance
        if (arena.isObstacleNearby(this)) {
            direction += 180;
            direction = normalizeDirection(direction);
        }
    }

    /**
     * Checks if the whisker line in front of the robot intersects any obstacle.
     */
    private boolean detectWhiskerCollision(RobotArena arena) {
        double radians = Math.toRadians(direction);
        double x2 = x + whiskerLength * Math.cos(radians);
        double y2 = y + whiskerLength * Math.sin(radians);
        Line whisker = new Line(x, y, x2, y2);
        return arena.intersectsAnyObstacle(whisker);
    }

    /**
     * Normalize direction into [0, 360).
     */
    protected double normalizeDirection(double dir) {
        dir %= 360;
        if (dir < 0) dir += 360;
        return dir;
    }

    // ------------------------------------------------
    // Getters and setters
    // ------------------------------------------------
    public double getSensorRange() {
        return sensorRange;
    }

    public void setSensorRange(double sensorRange) {
        this.sensorRange = sensorRange;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = normalizeDirection(direction);
    }

    public double getWhiskerLength() {
        return whiskerLength;
    }

    public void setWhiskerLength(double whiskerLength) {
        this.whiskerLength = whiskerLength;
    }
}
