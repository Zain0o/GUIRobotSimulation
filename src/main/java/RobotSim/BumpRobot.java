package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * BumpRobot implements a simple contact-based sensor system.
 * It changes appearance and behavior when it collides with obstacles or walls.
 * The robot visually shows its collision state and provides feedback through
 * color changes and visual effects.
 */
public class BumpRobot extends Robot {
    private boolean isColliding = false;
    private int collisionCooldown = 0;
    private static final int COLLISION_RECOVERY_TIME = 30;
    private double collisionAngle = 0;
    private static final Color NORMAL_COLOR = Color.DARKGREEN;
    private static final Color COLLISION_COLOR = Color.RED;

    /**
     * Constructs a BumpRobot with specified position and size.
     */
    public BumpRobot(double x, double y, double radius) {
        super(x, y, radius);
        speed = 3.0; // Slightly faster than base robot
    }

    @Override
    public void move(RobotArena arena) {
        // Store current position
        double oldX = x;
        double oldY = y;

        // Try to move
        double radians = Math.toRadians(direction);
        x += speed * Math.cos(radians);
        y += speed * Math.sin(radians);

        // Check for collisions
        isColliding = false;
        for (ArenaItem item : arena.getItems()) {
            if (item != this && calculateDistance(item) < (radius + item.getRadius())) {
                isColliding = true;
                collisionAngle = Math.toDegrees(Math.atan2(y - item.getY(), x - item.getX()));
                handleCollision(oldX, oldY);
                break;
            }
        }

        // Boundary checks
        if (x <= radius || x >= arena.getWidth() - radius ||
                y <= radius || y >= arena.getHeight() - radius) {
            isColliding = true;
            handleCollision(oldX, oldY);
        }

        // Update collision cooldown
        if (collisionCooldown > 0) {
            collisionCooldown--;
        }
    }

    /**
     * Handles collision response including position reset and direction change.
     */
    private void handleCollision(double oldX, double oldY) {
        // Move back to previous position
        x = oldX;
        y = oldY;

        // Set collision state
        collisionCooldown = COLLISION_RECOVERY_TIME;

        // Change direction based on collision
        direction = collisionAngle + 180 + (Math.random() - 0.5) * 30;
        direction = normalizeDirection(direction);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw collision effect if recently collided
        if (collisionCooldown > 0) {
            double pulseSize = radius * (1 + 0.3 * (COLLISION_RECOVERY_TIME - collisionCooldown)
                    / COLLISION_RECOVERY_TIME);
            RadialGradient gradient = new RadialGradient(
                    0, 0, x, y, pulseSize,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 0, 0, 0.3)),
                    new Stop(1, Color.TRANSPARENT)
            );
            gc.setFill(gradient);
            gc.fillOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
        }

        // Draw robot body
        gc.setFill(isColliding ? COLLISION_COLOR : NORMAL_COLOR);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw direction indicator
        double dirRads = Math.toRadians(direction);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(x, y,
                x + radius * Math.cos(dirRads),
                y + radius * Math.sin(dirRads));

        // Draw bump sensors
        drawBumpSensors(gc);

        // Draw treads/wheels
        drawTreads(gc);
    }

    /**
     * Draws the bump sensors around the robot's perimeter.
     */
    private void drawBumpSensors(GraphicsContext gc) {
        gc.setFill(isColliding ? Color.RED : Color.DARKGRAY);
        double sensorSize = radius * 0.2;

        // Draw 8 bump sensors around the perimeter
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45 + direction);
            double sx = x + (radius - sensorSize/2) * Math.cos(angle);
            double sy = y + (radius - sensorSize/2) * Math.sin(angle);
            gc.fillOval(sx - sensorSize/2, sy - sensorSize/2,
                    sensorSize, sensorSize);
        }
    }

    /**
     * Draws the robot's tank-like treads.
     */
    private void drawTreads(GraphicsContext gc) {
        gc.save();  // Save current transform
        gc.translate(x, y);
        gc.rotate(direction);

        // Draw treads
        gc.setFill(Color.BLACK);
        double treadWidth = radius * 0.3;
        double treadLength = radius * 1.8;

        // Left tread
        gc.fillRect(-radius - treadWidth, -treadLength/2,
                treadWidth, treadLength);
        // Right tread
        gc.fillRect(radius, -treadLength/2,
                treadWidth, treadLength);

        // Draw tread details (tracks)
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        for (double y = -treadLength/2; y < treadLength/2; y += treadLength/6) {
            // Left tread marks
            gc.strokeLine(-radius - treadWidth, y, -radius, y);
            // Right tread marks
            gc.strokeLine(radius, y, radius + treadWidth, y);
        }

        gc.restore();  // Restore original transform
    }

    @Override
    public String toString() {
        return "BumpRobot at (" + Math.round(x) + "," + Math.round(y) +
                ")" + (isColliding ? " [COLLISION]" : "");
    }
}