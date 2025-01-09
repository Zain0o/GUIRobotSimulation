package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * The SmartRobot class represents an advanced robot within the RobotArena.
 * It extends the basic Robot class with additional behaviors and attributes.
 */
public class SmartRobot extends Robot {
    private static final int NUM_SENSORS = 8;
    private static final double SENSOR_RANGE = 80;

    // -- NEW or MOVED FIELDS --
    protected double baseSpeed;  // declare baseSpeed so the compiler knows about it

    private final double[] sensorReadings = new double[NUM_SENSORS];
    private double learningRate = 0.1;
    private double[][] collisionMemory = new double[8][2]; // Stores dangerous directions
    private int collisionCount = 0;
    private boolean isAnalyzing = false;
    private double analyzingTimer = 0;

    /**
     * Constructs a SmartRobot with a specified position and radius.
     *
     * @param x      The X-coordinate of the robot's center.
     * @param y      The Y-coordinate of the robot's center.
     * @param radius The radius of the robot's bounding circle.
     */
    public SmartRobot(double x, double y, double radius) {
        super(x, y, radius);
        baseSpeed = 2.5;  // or define a default here
        speed = baseSpeed;
        initializeMemory();
    }

    /**
     * Initializes the collision memory for the SmartRobot.
     * Each sensor direction is initialized with zero danger.
     */
    private void initializeMemory() {
        for (int i = 0; i < 8; i++) {
            collisionMemory[i][0] = i * 45; // direction
            collisionMemory[i][1] = 0;      // danger level
        }
    }

    /**
     * Overrides the move method to include smart movement logic.
     *
     * @param arena The RobotArena instance managing the arena.
     */
    @Override
    public void move(RobotArena arena) {
        updateSensors(arena);

        if (isAnalyzing) {
            analyzeSurroundings();
        } else {
            moveSmartly(arena);
        }

        analyzingTimer += 0.016; // ~60fps
    }

    /**
     * Updates sensor readings by checking for obstacles in each sensor direction.
     *
     * @param arena The RobotArena instance managing the arena.
     */
    private void updateSensors(RobotArena arena) {
        for (int i = 0; i < NUM_SENSORS; i++) {
            double angle = i * (360.0 / NUM_SENSORS);
            double sensorX = x + SENSOR_RANGE * Math.cos(Math.toRadians(angle));
            double sensorY = y + SENSOR_RANGE * Math.sin(Math.toRadians(angle));
            Line sensorLine = new Line(x, y, sensorX, sensorY);

            sensorReadings[i] = arena.intersectsAnyObstacle(sensorLine) ? 1.0 : 0.0;
        }
    }

    /**
     * Analyzes surroundings by rotating to scan for safe directions.
     */
    private void analyzeSurroundings() {
        // Remain in "analyzing" mode for ~1 second
        isAnalyzing = analyzingTimer < 1.0;
        // Rotate slowly while analyzing
        direction += 2;
        direction = normalizeDirection(direction);
    }

    /**
     * Implements smart movement logic based on sensor readings and collision memory.
     *
     * @param arena The RobotArena instance managing the arena.
     */
    private void moveSmartly(RobotArena arena) {
        // Find safest direction using sensor data and collision memory
        double safestDirection = findSafestDirection();

        // Smoothly turn towards safest direction
        double angleDiff = normalizeAngle(safestDirection - direction);
        direction += angleDiff * 0.1;

        // Move forward if path is relatively clear
        double clearance = getForwardClearance();
        if (clearance > 0.5) {
            double moveAngle = Math.toRadians(direction);
            double newX = x + speed * clearance * Math.cos(moveAngle);
            double newY = y + speed * clearance * Math.sin(moveAngle);

            if (!arena.isColliding(newX, newY, radius, getId())) {
                x = newX;
                y = newY;
            } else {
                handleCollision(direction);
            }
        } else {
            isAnalyzing = true;
            analyzingTimer = 0;
        }
    }

    /**
     * Finds the safest direction based on sensor readings and collision memory.
     *
     * @return The angle in degrees representing the safest direction.
     */
    private double findSafestDirection() {
        double safestAngle = 0;
        double maxSafety = -1;

        for (int i = 0; i < 360; i += 15) {
            double safety = calculateSafetyScore(i);
            if (safety > maxSafety) {
                maxSafety = safety;
                safestAngle = i;
            }
        }
        return safestAngle;
    }

    /**
     * Calculates a safety score for a given angle based on sensor readings and collision memory.
     *
     * @param angle The angle in degrees to evaluate.
     * @return A safety score where higher values indicate safer directions.
     */
    private double calculateSafetyScore(double angle) {
        double score = 1.0;

        // Factor in sensor readings
        for (int i = 0; i < NUM_SENSORS; i++) {
            double sensorAngle = i * (360.0 / NUM_SENSORS);
            double angleDiff = Math.abs(normalizeAngle(sensorAngle - angle));
            if (angleDiff < 45 && sensorReadings[i] > 0) {
                score *= (1 - Math.cos(Math.toRadians(angleDiff))) * 0.5;
            }
        }

        // Factor in collision memory
        for (int i = 0; i < 8; i++) {
            double memoryAngle = collisionMemory[i][0];
            double dangerLevel = collisionMemory[i][1];
            double angleDiff = Math.abs(normalizeAngle(memoryAngle - angle));
            if (angleDiff < 45) {
                score *= (1 - dangerLevel * Math.cos(Math.toRadians(angleDiff)));
            }
        }

        return score;
    }

    /**
     * Retrieves the clearance in the forward direction based on sensor readings.
     *
     * @return A value between 0.0 and 1.0 indicating the level of clearance.
     */
    private double getForwardClearance() {
        int forwardSensor = (int)(direction / (360.0 / NUM_SENSORS)) % NUM_SENSORS;
        return 1.0 - sensorReadings[forwardSensor];
    }

    /**
     * Handles collision by updating collision memory and potentially entering analysis mode.
     *
     * @param collisionAngle The angle at which the collision occurred.
     */
    private void handleCollision(double collisionAngle) {
        collisionCount++;

        // Update collision memory
        int memoryIndex = (int)(collisionAngle / 45) % 8;
        collisionMemory[memoryIndex][1] =
                Math.min(1.0, collisionMemory[memoryIndex][1] + learningRate);

        // Trigger analysis mode after multiple collisions
        if (collisionCount > 3) {
            isAnalyzing = true;
            analyzingTimer = 0;
            collisionCount = 0;
        }
    }

    /**
     * Draws the SmartRobot on the given GraphicsContext.
     * It utilizes the inherited draw method and adds additional features.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw sensors and other SmartRobot-specific features
        drawSensors(gc);

        // Draw robot body (overridden to change color based on analyzing state)
        gc.setFill(isAnalyzing ? Color.PURPLE : Color.BLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw "brain" core
        gc.setFill(Color.YELLOW);
        gc.fillOval(x - radius / 2, y - radius / 2, radius, radius);

        // Draw direction indicator
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        double dirRads = Math.toRadians(direction);
        gc.strokeLine(x, y,
                x + radius * Math.cos(dirRads),
                y + radius * Math.sin(dirRads));

        // Draw analysis effect when analyzing
        if (isAnalyzing) {
            drawAnalysisEffect(gc);
        }

        // Draw wheels inherited from Robot
        drawWheels(gc);
    }

    /**
     * Draws sensor visualizations and indicators based on sensor readings and collision memory.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    private void drawSensors(GraphicsContext gc) {
        for (int i = 0; i < NUM_SENSORS; i++) {
            double angle = i * (360.0 / NUM_SENSORS);
            double sensorX = x + SENSOR_RANGE * Math.cos(Math.toRadians(angle));
            double sensorY = y + SENSOR_RANGE * Math.sin(Math.toRadians(angle));

            // Draw sensor line
            gc.setStroke(sensorReadings[i] > 0 ? Color.RED : Color.LIGHTBLUE);
            gc.setGlobalAlpha(0.3);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, sensorX, sensorY);

            // Draw danger level from memory
            int memoryIndex = (int)(angle / 45) % 8;
            double dangerLevel = collisionMemory[memoryIndex][1];
            if (dangerLevel > 0) {
                gc.setFill(Color.rgb(255, 0, 0, dangerLevel * 0.3));
                gc.fillOval(sensorX - 5, sensorY - 5, 10, 10);
            }
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Draws a pulsating analysis effect when the robot is in analyzing mode.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    private void drawAnalysisEffect(GraphicsContext gc) {
        double pulseSize = radius * 2 * (1 + 0.2 * Math.sin(analyzingTimer * 10));
        RadialGradient gradient = new RadialGradient(
                0, 0, x, y, pulseSize,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(128, 0, 128, 0.2)),
                new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(gradient);
        gc.fillOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
    }

    /**
     * Normalizes any angle into [0..360) range.
     */
    private double normalizeAngle(double angle) {
        angle %= 360.0;
        if (angle < 0) {
            angle += 360.0;
        }
        return angle;
    }

    /**
     * Provides a string representation of the SmartRobot.
     *
     * @return A string containing the robot's type, ID, and position.
     */
    @Override
    public String toString() {
        return String.format("SmartRobot (ID: %d)\nPosition: (%.2f, %.2f)\nAnalyzing: %s",
                getId(),
                x,
                y,
                isAnalyzing ? "Yes" : "No");
    }
}
