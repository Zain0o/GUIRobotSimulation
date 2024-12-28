package RobotSim;


import RobotSim.Line;
/**
 * WhiskerSensor is responsible for detecting whether there's an obstacle
 * directly in front of a given (x, y) position, heading in a certain direction.
 * It uses the Line class for geometric line intersection with obstacle edges.
 */
public class WhiskerSensor {

    private double whiskerLength;  // length of the "sensor line" in front of the robot

    /**
     * Constructs a WhiskerSensor of given length.
     * @param length the length of the sensor line in pixels
     */
    public WhiskerSensor(double length) {
        this.whiskerLength = length;
    }

    /**
     * Checks if a "whisker" line from (x, y) forward in 'direction' degrees
     * intersects any obstacle edge in the arena.
     *
     * @param arena     The RobotArena containing obstacles
     * @param x         Robot's center x
     * @param y         Robot's center y
     * @param direction Robot's heading in degrees (0-360)
     * @return true if we detect an obstacle in front, false otherwise
     */
    public boolean detectObstacle(RobotArena arena, double x, double y, double direction) {
        // Convert direction to radians
        double radians = Math.toRadians(direction);

        // Endpoint of the whisker line
        double x2 = x + whiskerLength * Math.cos(radians);
        double y2 = y + whiskerLength * Math.sin(radians);

        // Construct the whisker line
        Line whisker = new Line(x, y, x2, y2);

        // Check collision with each obstacle
        for (ArenaItem item : arena.getItems()) {
            if (item instanceof Obstacle obstacle) {
                if (checkIntersectionWithSquare(whisker, obstacle)) {
                    return true;
                }
            }
        }
        return false; // no obstacle found in front
    }

    /**
     * Checks if the given whisker line intersects any of the 4 edges of a square obstacle.
     * @param whisker   The sensor line
     * @param obstacle  The Obstacle (square) in the arena
     * @return true if the whisker intersects an obstacle edge
     */
    private boolean checkIntersectionWithSquare(Line whisker, Obstacle obstacle) {
        double ox = obstacle.getX();
        double oy = obstacle.getY();
        double r  = obstacle.getRadius();

        // If your obstacle is drawn as a square with center (ox, oy) and side length = 2*r,
        // the corners are:
        double left   = ox - r;
        double right  = ox + r;
        double top    = oy - r;
        double bottom = oy + r;

        // Build lines for each edge
        Line topEdge    = new Line(left,  top,    right,  top);
        Line bottomEdge = new Line(left,  bottom, right,  bottom);
        Line leftEdge   = new Line(left,  top,    left,   bottom);
        Line rightEdge  = new Line(right, top,    right,  bottom);

        // If whisker intersects any edge, we consider that a hit
        return  (whisker.findintersection(topEdge))    ||
                (whisker.findintersection(bottomEdge)) ||
                (whisker.findintersection(leftEdge))   ||
                (whisker.findintersection(rightEdge));
    }

    /**
     * Returns the sensor length (for debugging or display).
     */
    public double getWhiskerLength() {
        return whiskerLength;
    }

    /**
     * Sets a new whisker length if needed.
     */
    public void setWhiskerLength(double length) {
        this.whiskerLength = length;
    }
}
