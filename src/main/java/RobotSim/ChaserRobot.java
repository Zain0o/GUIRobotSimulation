package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The ChaserRobot class represents a robot that chases the nearest robot in the arena.
 * It inherits from the Robot class and overrides the move behavior.
 */
public class ChaserRobot extends Robot {

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
     * Draws the chaser robot as a distinct red circle with a black border.
     * @param gc The GraphicsContext used for drawing
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw the chaser robot
        gc.setFill(Color.RED);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw the robot's outline
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Moves the chaser robot toward the nearest robot in the arena.
     * @param arena The RobotArena instance
     */
    @Override
    public void move(RobotArena arena) {
        Robot nearestRobot = findNearestRobot(arena);

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
