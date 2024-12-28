package RobotSim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * BeamRobot extends Robot to add multiple beam sensors that can detect obstacles
 * at different angles relative to the robot's heading.
 */
public class BeamRobot extends Robot {
    private static final int NUM_BEAMS = 5;  // Number of beam sensors
    private static final double BEAM_LENGTH = 100;  // Length of each beam
    private static final double BEAM_SPREAD = 90;  // Total angle spread of beams in degrees
    private ArrayList<Line> beams;  // Store the beam lines
    private ArrayList<Boolean> beamDetections;  // Store detection status for each beam

    public BeamRobot(double x, double y, double radius) {
        super(x, y, radius);
        beams = new ArrayList<>();
        beamDetections = new ArrayList<>();

        // Initialize beam arrays
        for (int i = 0; i < NUM_BEAMS; i++) {
            beams.add(new Line(0, 0, 0, 0));  // Will be updated in updateBeams()
            beamDetections.add(false);
        }
    }

    /**
     * Updates the positions and angles of all beam sensors based on robot position
     * and direction.
     */
    private void updateBeams() {
        double startAngle = direction - BEAM_SPREAD/2;
        double angleStep = BEAM_SPREAD / (NUM_BEAMS - 1);

        for (int i = 0; i < NUM_BEAMS; i++) {
            double beamAngle = Math.toRadians(startAngle + i * angleStep);
            double endX = x + BEAM_LENGTH * Math.cos(beamAngle);
            double endY = y + BEAM_LENGTH * Math.sin(beamAngle);
            beams.get(i).setLine(x, y, endX, endY);
        }
    }

    @Override
    public void move(RobotArena arena) {
        updateBeams();  // Update beam positions

        // Check each beam for collisions
        boolean collision = false;
        for (int i = 0; i < NUM_BEAMS; i++) {
            beamDetections.set(i, arena.intersectsAnyObstacle(beams.get(i)));
            if (beamDetections.get(i)) {
                collision = true;
            }
        }

        // If any beam detects an obstacle, adjust direction
        if (collision) {
            // Find the beam with the most clear path
            int clearest = findClearestDirection();
            if (clearest != -1) {
                // Turn towards the clearest direction
                double targetAngle = direction - BEAM_SPREAD/2 +
                        (BEAM_SPREAD / (NUM_BEAMS - 1)) * clearest;
                direction = targetAngle;
            } else {
                // All beams blocked, reverse direction
                direction += 180;
            }
            direction = normalizeDirection(direction);
        }

        // Continue with normal movement
        super.move(arena);
    }

    /**
     * Finds the beam index with the clearest path (no obstacle detection)
     * @return index of clearest beam, or -1 if all beams detect obstacles
     */
    private int findClearestDirection() {
        for (int i = 0; i < NUM_BEAMS; i++) {
            if (!beamDetections.get(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw the base robot first
        super.draw(gc);

        // Draw each beam
        updateBeams();  // Ensure beams are in current position
        for (int i = 0; i < NUM_BEAMS; i++) {
            Line beam = beams.get(i);

            // Set color based on detection status
            if (beamDetections.get(i)) {
                gc.setStroke(Color.RED);  // Detected obstacle
            } else {
                gc.setStroke(Color.GREEN);  // Clear path
            }

            gc.setLineWidth(2);
            gc.strokeLine(beam.getX1(), beam.getY1(), beam.getX2(), beam.getY2());
        }

        // Draw a small indicator showing this is a beam robot
        gc.setFill(Color.YELLOW);
        gc.fillOval(x - radius/3, y - radius/3, radius/1.5, radius/1.5);
    }
}