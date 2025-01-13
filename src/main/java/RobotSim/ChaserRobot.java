package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * The ChaserRobot class represents a robot that chases the nearest robot in the arena.
 * It inherits from the Robot class and overrides the move and draw behaviors.
 */
public class ChaserRobot extends Robot {

    // New field to keep track of the current target robot
    private Robot targetRobot;

    /**
     * Constructs a ChaserRobot with a specified position and radius.
     * @param x The X-coordinate of the chaser robot
     * @param y The Y-coordinate of the chaser robot
     * @param radius The radius of the chaser robot
     */
    public ChaserRobot(double x, double y, double radius) {
        super(x, y, radius);
    }

    /**
     * Draws the chaser robot with enhanced visual effects.
     * @param gc The GraphicsContext used for drawing
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw targeting effect
        drawTargetingEffect(gc);

        // Draw robot body with gradient
        RadialGradient bodyGradient = new RadialGradient(
                0, 0, x, y, radius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED),
                new Stop(1, Color.DARKRED)
        );
        gc.setFill(bodyGradient);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw scanning effect
        double scanAngle = (System.currentTimeMillis() % 2000) / 2000.0 * 360;
        gc.save();
        gc.translate(x, y);
        gc.rotate(scanAngle);

        // Scanning beam
        gc.setStroke(Color.YELLOW);
        gc.setGlobalAlpha(0.4);
        gc.setLineWidth(radius / 2);
        gc.strokeLine(0, 0, radius * 2, 0);
        gc.setGlobalAlpha(1.0);
        gc.restore();

        // Draw target indicator if chasing
        if (targetRobot != null) {
            drawTargetLock(gc, targetRobot);
        }

        // Draw enhanced wheels
        drawChaserWheels(gc);
    }

    /**
     * Draws a pulsing targeting effect around the robot.
     * @param gc The GraphicsContext used for drawing
     */
    private void drawTargetingEffect(GraphicsContext gc) {
        double pulseSize = radius * 1.5 * (1 + 0.2 * Math.sin(System.currentTimeMillis() * 0.005));
        gc.setStroke(Color.RED);
        gc.setLineDashes(5);
        gc.setLineWidth(2);
        gc.strokeOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
        gc.setLineDashes(null);
    }

    /**
     * Draws a target lock indicator on the targeted robot.
     * @param gc The GraphicsContext used for drawing
     * @param target The targeted Robot
     */
    private void drawTargetLock(GraphicsContext gc, Robot target) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.setLineDashes(5);

        // Line to target
        gc.strokeLine(x, y, target.getX(), target.getY());

        // Target box
        double tx = target.getX();
        double ty = target.getY();
        double size = target.getRadius() * 1.5;
        gc.strokeRect(tx - size / 2, ty - size / 2, size, size);

        // Corner brackets
        double bracketSize = size * 0.3;
        drawCornerBracket(gc, tx, ty, size, bracketSize);
    }

    /**
     * Draws corner brackets for the target indicator box.
     * @param gc The GraphicsContext used for drawing
     * @param tx The X-coordinate of the target
     * @param ty The Y-coordinate of the target
     * @param size The size of the target box
     * @param bracketSize The size of the corner brackets
     */
    private void drawCornerBracket(GraphicsContext gc, double tx, double ty, double size, double bracketSize) {
        // Top left
        gc.strokeLine(tx - size / 2, ty - size / 2, tx - size / 2 + bracketSize, ty - size / 2);
        gc.strokeLine(tx - size / 2, ty - size / 2, tx - size / 2, ty - size / 2 + bracketSize);

        // Top right
        gc.strokeLine(tx + size / 2, ty - size / 2, tx + size / 2 - bracketSize, ty - size / 2);
        gc.strokeLine(tx + size / 2, ty - size / 2, tx + size / 2, ty - size / 2 + bracketSize);

        // Bottom left
        gc.strokeLine(tx - size / 2, ty + size / 2, tx - size / 2 + bracketSize, ty + size / 2);
        gc.strokeLine(tx - size / 2, ty + size / 2, tx - size / 2, ty + size / 2 - bracketSize);

        // Bottom right
        gc.strokeLine(tx + size / 2, ty + size / 2, tx + size / 2 - bracketSize, ty + size / 2);
        gc.strokeLine(tx + size / 2, ty + size / 2, tx + size / 2, ty + size / 2 - bracketSize);
    }

    /**
     * Draws enhanced wheels for the chaser robot.
     * @param gc The GraphicsContext used for drawing
     */
    private void drawChaserWheels(GraphicsContext gc) {
        // Example implementation: draw four wheels
        double wheelRadius = radius / 4;
        double offsetX = radius * 0.6;
        double offsetY = radius * 0.6;

        gc.setFill(Color.BLACK);
        gc.fillOval(x - offsetX - wheelRadius, y - offsetY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
        gc.fillOval(x + offsetX - wheelRadius, y - offsetY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
        gc.fillOval(x - offsetX - wheelRadius, y + offsetY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
        gc.fillOval(x + offsetX - wheelRadius, y + offsetY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
    }

    /**
     * Moves the chaser robot toward the nearest robot in the arena.
     * @param arena The RobotArena instance
     */
    @Override
    public void move(RobotArena arena) {
        // Find and set the nearest robot as the target
        Robot nearestRobot = findNearestRobot(arena);
        targetRobot = nearestRobot;

        if (nearestRobot != null) {
            // Calculate direction towards the nearest robot
            double dx = nearestRobot.getX() - this.x;
            double dy = nearestRobot.getY() - this.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                // Normalize and move towards the target
                this.x += (dx / distance) * speed;
                this.y += (dy / distance) * speed;

                // Prevent the chaser from overlapping with the target
                if (distance < this.radius + nearestRobot.getRadius()) {
                    this.x -= (dx / distance) * speed;
                    this.y -= (dy / distance) * speed;
                }
            }
        }

        // Boundary collision handling
        if (this.x <= radius || this.x >= arena.getWidth() - radius) {
            direction = 180 - direction;
            direction = normalizeDirection(direction);
        }
        if (this.y <= radius || this.y >= arena.getHeight() - radius) {
            direction = 360 - direction;
            direction = normalizeDirection(direction);
        }
    }

    /**
     * Finds the nearest robot to this chaser robot in the arena.
     * @param arena The RobotArena instance
     * @return The nearest Robot object, or null if no other robots exist
     */
    private Robot findNearestRobot(RobotArena arena) {
        Robot nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (ArenaItem item : arena.getItems()) {
            if (item instanceof Robot && !(item instanceof ChaserRobot)) {
                double distance = this.calculateDistance(item);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = (Robot) item;
                }
            }
        }

        return nearest;
    }
}
