package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class Obstacle extends ArenaItem {
    private final Color baseColor = Color.RED.darker();
    private double rotation = 0;
    private boolean isColliding = false;
    private int collisionTimer = 0;

    public Obstacle(double x, double y, double radius) {
        super(x, y, radius);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Create gradient for 3D effect
        LinearGradient gradient = new LinearGradient(
                x - radius, y - radius,
                x + radius, y + radius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, baseColor.brighter()),
                new Stop(1, baseColor.darker())
        );

        // Draw shadow
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(0.2);
        gc.fillRect(x - radius + 3, y - radius + 3, radius * 2, radius * 2);
        gc.setGlobalAlpha(1.0);

        // Draw obstacle with gradient
        gc.setFill(gradient);
        gc.fillRect(x - radius, y - radius, radius * 2, radius * 2);

        // Draw border
        gc.setStroke(isColliding ? Color.YELLOW : Color.BLACK);
        gc.setLineWidth(isColliding ? 2 : 1);
        gc.strokeRect(x - radius, y - radius, radius * 2, radius * 2);

        // Draw warning stripes when colliding
        if (isColliding) {
            drawWarningStripes(gc);
        }

        // Update collision state
        if (collisionTimer > 0) {
            collisionTimer--;
            if (collisionTimer == 0) {
                isColliding = false;
            }
        }
    }

    private void drawWarningStripes(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(rotation);

        double stripeWidth = radius * 0.4;
        gc.setFill(Color.YELLOW);
        gc.setGlobalAlpha(0.5);

        for (double i = -radius; i < radius * 2; i += stripeWidth * 2) {
            gc.fillRect(-radius + i, -radius, stripeWidth, radius * 2);
        }

        gc.setGlobalAlpha(1.0);
        gc.restore();

        rotation = (rotation + 2) % 360;
    }

    public void registerCollision() {
        isColliding = true;
        collisionTimer = 10;
    }

    @Override
    public String toString() {
        return String.format("Obstacle at (%.0f,%.0f)", x, y);
    }
}