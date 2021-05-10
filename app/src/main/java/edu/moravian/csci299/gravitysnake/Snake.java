package edu.moravian.csci299.gravitysnake;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.moravian.csci299.gravitysnake.Util.*;

/**
 * A complete Snake. This keeps track of all of the body parts and their
 * movement. There are several methods for dealing with collisions with the
 * snake as well.
 *
 * NOTE: This class is complete, but you may to need look over the public
 * methods to use them.
 */
public class Snake {
    /** Radius of each body piece in dp */
    public final static float BODY_PIECE_SIZE_DP = 15f;

    /** Distance that is moved each actual movement, in dp */
    public final static float STEP_DISTANCE_DP = 2.5f;

    /**
     * The points that make up the body. The point at position 0 is the head.
     * Each of these points are stored using units of pixels
     */
    private final List<PointF> body = new ArrayList<>();

    /**
     * The distance to be travelled. This is needed since the snake actually
     * moves in discrete steps instead of continuously.
     */
    private double distXToTravel = 0.0, distYToTravel = 0.0;

    /**
     * The number of body pieces to add while the snake is moving forward.
     */
    private int piecesToAdd;

    /**
     * Converts dp to px, for example, BODY_PIECE_SIZE_DP will always be
     * multiplied by this value.
     */
    private final float dpToPxFactor;

    /**
     * Create the snake with the given initial position.
     * @param initial the initial position
     * @param dpToPxFactor the factor to convert dp to px
     * @param startingLength the initial length of the snake (added over time)
     */
    public Snake(PointF initial, float dpToPxFactor, int startingLength) {
        body.add(initial);
        this.dpToPxFactor = dpToPxFactor;
        this.piecesToAdd = startingLength;
    }

    /**
     * @return the length of the snake, including any pieces yet to be added
     */
    public int getLength() { return body.size() + piecesToAdd; }

    /**
     * @return the list of body points currently in the snake
     */
    public List<PointF> getBody() { return Collections.unmodifiableList(body); }

    /**
     * Moves the snake forward.
     * @param direction the direction of movement, in radians
     * @param distance the distance of the movement, in pixels
     */
    public void move(double direction, double distance) {
        // Update the distance to be travelled
        distXToTravel += Math.cos(direction) * distance;
        distYToTravel += Math.sin(direction) * distance;

        // Move the snake as much of the distance as possible
        final double stepDist = STEP_DISTANCE_DP * dpToPxFactor; // distance of each step
        double distTotal = Math.hypot(distYToTravel, distXToTravel); // total distance to travel
        if (distTotal >= stepDist) {
            double angle = Math.atan2(distYToTravel, distXToTravel); // angle to travel at
            double stepXDist = stepDist * Math.cos(angle); // step distance in X direction
            double stepYDist = stepDist * Math.sin(angle); // step distance in Y direction
            while (distTotal >= stepDist) { // while the distance to travel is at least one step
                // Remove this distance from the remaining distance to travel
                distTotal -= stepDist;

                // Create and add the new head to the start of the body
                PointF newHead = new PointF(body.get(0).x, body.get(0).y);
                newHead.offset((float) stepXDist, (float) stepYDist);
                body.add(0, newHead);

                // Remove the tail (if there are no pieces to be added)
                if (piecesToAdd == 0) {
                    body.remove(body.size() - 1);
                } else {
                    piecesToAdd -= 1; // the tail is the new piece
                }
            }

            // Update the remaining distance
            distXToTravel = distTotal * Math.cos(angle);
            distYToTravel = distTotal * Math.cos(angle);
        }
    }

    /**
     * Increases the length of the snake by 1. This doesn't take effect right
     * away, but only after the snake has moved far enough for the new body
     * part to be placed.
     * @param amount the amount to increase the length by
     */
    public void increaseLength(int amount) { piecesToAdd += amount; }

    /**
     * Checks if the snake head intersects itself. This check allows for
     * significant overlap. First off, the first several section immediately
     * following the head don't count at all. After that, the body would have
     * to be overlapping by 75% for it to count.
     *
     * @return true if the snake intersections itself
     */
    public boolean headIntersectsSelf() {
        // For more aggressive/accurate it should be "2 *" in the next line, not "0.5 *"
        return anyWithinRange(body, body.get(0), 0.5 * BODY_PIECE_SIZE_DP * dpToPxFactor, 20);
    }

    /**
     * Checks if the snake head intersects the given circular item.
     * @param location the location of the item, in px
     * @param radius the radius of the item, in px
     * @return true if the snake intersections the given circular item
     */
    public boolean headIntersectsItem(PointF location, float radius) {
        return withinRange(body.get(0), location, BODY_PIECE_SIZE_DP * dpToPxFactor + radius);
    }

    /**
     * Checks if the snake head intersects any of the given circular items.
     * @param locations the locations of the items, in px
     * @param radius the radius of the items, in px
     * @return true if the snake intersections any of the given circular items
     */
    public boolean headIntersectsAnyItem(List<PointF> locations, float radius) {
        return anyWithinRange(locations, body.get(0), BODY_PIECE_SIZE_DP * dpToPxFactor + radius);
    }

    /**
     * Checks if the snake head is "out of bounds" of a rectangle that goes
     * from 0,0 to the given width and height. The snake is only out-of-bounds
     * when the middle of the head leaves.
     * @param width the width of the bounds in px
     * @param height the height of the bounds in px
     * @return true if the snake is out of bounds
     */
    public boolean headIsOutOfBounds(int width, int height) {
        PointF head = body.get(0);
        return head.x < 0 || head.y < 0 || head.x >= width || head.y >= height;

        // If forcing whole head in bounds:
        //final double size = BODY_PIECE_SIZE_DP * dpToPxFactor;
        //return head.x < size || head.y < size || head.x + size > width || head.y + size > height;
    }

    /**
     * Checks if the snake head or body intersects the given circular item.
     * @param location the location of the item, in px
     * @param radius the radius of the item, in px
     * @return true if the snake intersections the given circular item
     */
    public boolean bodyIntersectsItem(PointF location, float radius) {
        return anyWithinRange(body, location, BODY_PIECE_SIZE_DP * dpToPxFactor + radius);
    }

    /**
     * Checks if any point in the list is within range of a point.
     * @return true if withinRange(a, b, range) is true for any of the points in the list
     */
    private static boolean anyWithinRange(List<PointF> pts, PointF point, double range) {
        return pts.stream().anyMatch(pt -> withinRange(pt, point, range));
        // Same as:
        //for (PointF pt : pts) { if (withinRange(pt, point, range)) { return true; } }
        //return false;
    }

    /**
     * Checks if any point in the list (after the first `skip` elements) is within range of a point.
     * @return true if withinRange(a, b, range) is true for any of the points in the list
     *         (after skipping).
     */
    private static boolean anyWithinRange(List<PointF> pts, PointF point, double range, int skip) {
        return pts.stream().skip(skip).anyMatch(pt -> withinRange(pt, point, range));
        // Same as:
        //for (int i = skip; i < pts.size(); i++) { if (withinRange(pts.get(i), point, range)) { return true; } }
        //return false;
    }
}
