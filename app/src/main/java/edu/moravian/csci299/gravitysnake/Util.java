package edu.moravian.csci299.gravitysnake;

import android.graphics.PointF;

/**
 * Utilities for use by other classes.
 *
 * NOTE: This class is complete, but you may to need look over the public
 * methods to use them.
 */
public class Util {
    /**
     * Checks if two points are closer than a certain range of each other.
     * @param a the first point
     * @param b the first point
     * @param range the maximum distance allowed between the points
     * @return true if the distance from a to b is less than range
     */
    public static boolean withinRange(PointF a, PointF b, double range) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return dx*dx + dy*dy < range*range;
    }
}
