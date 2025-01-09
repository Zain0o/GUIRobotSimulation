package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class BumpRobot extends Robot {
    private boolean hasCollision = false;  // renamed from isColliding to avoid conflict
    private int collisionCooldown = 0;
    private static final int COLLISION_RECOVERY_TIME = 30;
    private static final Color NORMAL_COLOR = Color.DARKGREEN;
    private static final Color COLLISION_COLOR = Color.RED;

    public BumpRobot(double x, double y, double radius) {
        super(x, y, radius);
        speed = 3.0;
    }

    @Override
    public void move(RobotArena arena) {
        double oldX = x;
        double oldY = y;

        double radians = Math.toRadians(direction);
        double newX = x + speed * Math.cos(radians);
        double newY = y + speed * Math.sin(radians);

        // Use ArenaItem parameters for collision check
        if (!arena.isColliding(newX, newY, radius, getId())) {
            x = newX;
            y = newY;
        } else {
            hasCollision = true;
            handleCollision(oldX, oldY);
        }

        super.handleBoundaryCollision(arena);

        if (collisionCooldown > 0) {
            collisionCooldown--;
            if (collisionCooldown == 0) {
                hasCollision = false;
            }
        }
    }

    private void handleCollision(double oldX, double oldY) {
        x = oldX;
        y = oldY;
        collisionCooldown = COLLISION_RECOVERY_TIME;
        direction += 180 + (Math.random() - 0.5) * 30;
        direction = normalizeDirection(direction);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (collisionCooldown > 0) {
            drawCollisionEffect(gc);
        }

        gc.setFill(hasCollision ? COLLISION_COLOR : NORMAL_COLOR);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        double dirRads = Math.toRadians(direction);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(x, y,
                x + radius * Math.cos(dirRads),
                y + radius * Math.sin(dirRads));

        drawBumpSensors(gc);
        super.drawWheels(gc);
    }

    private void drawCollisionEffect(GraphicsContext gc) {
        double pulseSize = radius * (1 + 0.3 * (COLLISION_RECOVERY_TIME - collisionCooldown) / COLLISION_RECOVERY_TIME);
        RadialGradient gradient = new RadialGradient(
                0, 0, x, y, pulseSize,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 0, 0, 0.3)),
                new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(gradient);
        gc.fillOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
    }

    private void drawBumpSensors(GraphicsContext gc) {
        gc.setFill(hasCollision ? Color.RED : Color.DARKGRAY);
        double sensorSize = radius * 0.2;

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45 + direction);
            double sx = x + (radius - sensorSize/2) * Math.cos(angle);
            double sy = y + (radius - sensorSize/2) * Math.sin(angle);
            gc.fillOval(sx - sensorSize/2, sy - sensorSize/2, sensorSize, sensorSize);
        }
    }

    @Override
    public String toString() {
        return String.format("BumpRobot at (%.0f,%.0f)%s",
                x, y, hasCollision ? " [COLLISION]" : "");
    }
}