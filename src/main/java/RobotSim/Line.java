package RobotSim;

/**
 * A simple geometry utility class for representing and operating on a line
 * between (x1, y1) and (x2, y2). Provides methods for checking intersections,
 * distance from a point, etc.
 */
public class Line {
    // Coordinates: (x1, y1) -> (x2, y2)
    private double x1, y1, x2, y2;

    /**
     * Constructs a Line from (x1, y1) to (x2, y2).
     */
    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * @return the x1 coordinate
     */
    public double getX1() {
        return x1;
    }

    /**
     * @return the y1 coordinate
     */
    public double getY1() {
        return y1;
    }

    /**
     * @return the x2 coordinate
     */
    public double getX2() {
        return x2;
    }

    /**
     * @return the y2 coordinate
     */
    public double getY2() {
        return y2;
    }

    /**
     * Optionally, you can change these coordinates at runtime.
     */
    public void setLine(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Calculates the length of the line segment.
     */
    public double length() {
        return distance(x1, y1, x2, y2);
    }

    /**
     * Static helper to compute distance between (ax, ay) and (bx, by).
     */
    public static double distance(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Checks if this line intersects another line `other`.
     * If they intersect, we return true; otherwise false.
     *
     * This algorithm uses standard line-line intersection math.
     *
     * @param other The other line
     * @return true if the lines intersect at a point on *both* segments; false otherwise
     */
    public boolean findintersection(Line other) {
        // Denominator for “two-line intersection” formula
        double denom = (other.y2 - other.y1) * (x2 - x1)
                - (other.x2 - other.x1) * (y2 - y1);
        if (denom == 0.0) {
            // Lines are parallel or coincident; no single intersection point
            return false;
        }

        // Calculate the “relative intersection” positions along each line
        double ua = ((other.x2 - other.x1) * (y1 - other.y1)
                - (other.y2 - other.y1) * (x1 - other.x1)) / denom;
        double ub = ((x2 - x1) * (y1 - other.y1)
                - (y2 - y1) * (x1 - other.x1)) / denom;

        // If 0 <= ua <= 1 and 0 <= ub <= 1, we have an intersection *within* both segments
        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            return true;
        }
        return false;
    }

    /**
     * Returns the (x,y) coordinates of the intersection point with `other`,
     * or null if no intersection.
     *
     * Useful if you actually need the intersection location, not just a boolean.
     */
    public double[] getIntersectionPoint(Line other) {
        double denom = (other.y2 - other.y1) * (x2 - x1)
                - (other.x2 - other.x1) * (y2 - y1);
        if (denom == 0.0) {
            return null;  // parallel or coincident
        }

        double ua = ((other.x2 - other.x1) * (y1 - other.y1)
                - (other.y2 - other.y1) * (x1 - other.x1)) / denom;
        double ub = ((x2 - x1) * (y1 - other.y1)
                - (y2 - y1) * (x1 - other.x1)) / denom;

        // Intersection only matters if it's on both segments
        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            // Intersection coordinates
            double ix = x1 + ua * (x2 - x1);
            double iy = y1 + ua * (y2 - y1);
            return new double[] { ix, iy };
        }
        return null;
    }

    /**
     * Computes the perpendicular distance from point (px, py) to this line segment.
     *
     * If the perpendicular from (px, py) doesn’t fall between this line’s endpoints,
     * then we return the smaller distance to either endpoint.
     */
    public double distanceFrom(double px, double py) {
        // Line length squared
        double len2 = (x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1);
        if (len2 == 0.0) {
            // The line is actually a single point:
            return distance(px, py, x1, y1);
        }

        // Consider the line param “t” from 0 to 1
        double t = ((px - x1) * (x2 - x1) + (py - y1)*(y2 - y1)) / len2;
        if (t < 0) {
            // Closest to (x1, y1)
            return distance(px, py, x1, y1);
        } else if (t > 1) {
            // Closest to (x2, y2)
            return distance(px, py, x2, y2);
        }
        // Projection point on the segment
        double projx = x1 + t * (x2 - x1);
        double projy = y1 + t * (y2 - y1);
        return distance(px, py, projx, projy);
    }

    @Override
    public String toString() {
        return "Line(" + x1 + "," + y1 + " -> " + x2 + "," + y2 + ")";
    }
}
