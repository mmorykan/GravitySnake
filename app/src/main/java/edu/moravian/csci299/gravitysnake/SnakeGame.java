package edu.moravian.csci299.gravitysnake;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static edu.moravian.csci299.gravitysnake.Util.withinRange;

/**
 * The Snake Game. Keeps track of the snake, the food, walls, the score, and
 * all of the difficultly settings (speed, starting length, length increase per
 * food, wall placement probability). Knows how to draw everything.
 *
 * NOTE: This class is complete, but you may to need look over the public
 * methods to use them.
 */
public class SnakeGame {
    /** The one and only random number generator for any game */
    private final static Random random = new Random();

    /** Radius of each body part of the snake */
    public final static float SNAKE_SIZE_DP = 30;

    /** Radius of each food item in dp */
    public final static float FOOD_SIZE_DP = 20f;

    /** Radius of each wall item in dp */
    public final static float WALL_SIZE_DP = 20f;

    /** Touch "radius" in dp */
    public final static float TOUCH_SIZE_DP = 5;

    /** The width and height of the game, in px */
    private int width, height;

    /** If the game is over */
    private boolean gameOver = true;

    /** The snake moving around the game */
    private Snake snake;

    /** The direction the snake is moving */
    private double direction;

    /** Location of the current food, in px */
    private PointF food;

    /** Number of foods eaten (i.e. the score) */
    private int score = 0;

    /** Initial speed of the snake, in dp/frame */
    private double initialSpeed = 2.5;

    /** How much the speed increases each time a food is eaten */
    private double speedIncreasePerFood = 0.0;

    /** Speed of the snake, in dp/frame */
    private double speed = 2.5;

    /** Number of pieces the snake is at the beginning of each game */
    private int startingLength = 25;

    /** Number of pieces to add to the snake each time a food is eaten */
    private int lengthIncreasePerFood = 8;

    /** Probability to place a new wall each frame */
    private double wallPlacementProbability = 0.005;

    /** Locations of all of the walls, each in px */
    private final List<PointF> walls = new ArrayList<>();

    /**
     * Converts dp to px, for example, FOOD_SIZE_DP will always be multiplied
     * by this value.
     */
    private float dpToPxFactor = 1f;

    /**
     * @return true if the game has not yet been started ever
     */
    public boolean hasNotStarted() { return snake == null; }

    /**
     * @return the current score (number of foods eaten)
     */
    public int getScore() { return score; }

    /**
     * Set the factor for converting dp measurements to px. This is the size of
     * 1 dp in pixels.
     * @param dpToPxFactor the conversion factor to go from dp to px
     */
    public void setDpToPxFactor(float dpToPxFactor) {
        this.dpToPxFactor = dpToPxFactor;
    }

    /**
     * Start the game. Can also be used to start a new game if one has already begun.
     * @param width the width of the playing area in px
     * @param height the height of the playing area in px
     */
    public void startGame(int width, int height) {
        this.width = width;
        this.height = height;
        snake = new Snake(new PointF(width / 2f, height / 2f), dpToPxFactor, startingLength);
        speed = initialSpeed;
        score = 0;
        walls.clear();
        moveFood();
        gameOver = false;
    }

    /**
     * Get the status of the game. The game is over if no game has ever been
     * started or if the snake has died and a new game has not yet started.
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() { return gameOver; }

    /**
     * @return the current amount the snake length is increased per food
     */
    public int getLengthIncreasePerFood() { return lengthIncreasePerFood; }

    /**
     * @param lengthIncreasePerFood the new amount the snake length is increased per food
     */
    public void setLengthIncreasePerFood(int lengthIncreasePerFood) { this.lengthIncreasePerFood = lengthIncreasePerFood; }

    /**
     * @return the current starting length of the snake
     */
    public int getStartingLength() { return startingLength;  }

    /**
     * @param startingLength the new starting length of the snake
     */
    public void setStartingLength(int startingLength) {  this.startingLength = startingLength;  }

    /**
     * @return the current length of the snake (including any pieces yet to be added)
     */
    public int getCurrentLength() { return snake.getLength();  }

    /**
     * @return the initial speed of the snake in dp/frame
     */
    public double getInitialSpeed() { return initialSpeed; }

    /**
     * @param speed the new initial speed of the snake in dp/frame
     */
    public void setInitialSpeed(double speed) { this.initialSpeed = speed; }

    /**
     * @return the current speed increase per food eaten
     */
    public double getSpeedIncreasePerFood() { return speedIncreasePerFood; }

    /**
     * @param speedIncreasePerFood the new speed increase per food eaten
     */
    public void setSpeedIncreasePerFood(double speedIncreasePerFood) { this.speedIncreasePerFood = speedIncreasePerFood; }

    /**
     * @return the current speed of the snake in dp/frame
     */
    public double getCurrentSpeed() { return speed; }

    /**
     * @return the current wall placement probability (per frame)
     */
    public double getWallPlacementProbability() { return wallPlacementProbability; }

    /**
     * @param prob the new wall placement probability (per frame)
     */
    public void setWallPlacementProbability(double prob) { wallPlacementProbability = prob; }

    /**
     * Gets the current movement direction of the snake in radians.
     * @return the direction of the snake in radians, from -pi to pi.
     */
    public double getMovementDirection() { return direction; }

    /**
     * Sets the direction that the snake will move in the future.
     * @param angle the new direction of the snake, in radians, 0 is straight
     *              right along the positive X axis, pi/2 is straight down along
     *              the positive Y axis
     */
    public void setMovementDirection(double angle) { direction = angle; }

    /**
     * Update the game. This moves the snake, checks if the game is over (snake
     * hits itself, goes out of bounds, or hits a wall), checks if the snake
     * got the food, and possibly adds a new random wall piece.
     * @return true if the game is still going, false if the game is over
     */
    public boolean update() {
        if (gameOver) { return false; }

        // Move the snake
        snake.move(direction, speed * dpToPxFactor); // NOTE: this does not take into account the frame rate

        // Check if the snake has hit itself, gone out-of-bounds, or hit any of the walls
        if (snake.headIntersectsSelf() || snake.headIsOutOfBounds(width, height) ||
                snake.headIntersectsAnyItem(walls, WALL_SIZE_DP * dpToPxFactor)) {
            gameOver = true;
            return false;
        }

        // Check if the snake has "eaten" the food
        if (snake.headIntersectsItem(food, FOOD_SIZE_DP * dpToPxFactor)) {
            snake.increaseLength(lengthIncreasePerFood);
            speed += speedIncreasePerFood;
            moveFood();
            score++;
        }

        // Every so often add a new wall
        if (random.nextFloat() < wallPlacementProbability) { addWall(); }

        return true;
    }

    /**
     * "Touch" the game at a particular point. If the snake is touched
     * anywhere, the game is over. If the food is touched, it moves. If a wall
     * is touched, it is removed.
     * @param pt the touched point
     * @return true if the game is still going, false if the game is now over
     */
    public boolean touched(PointF pt) {
        if (gameOver) { return false; }

        // Game over if the snake is touched
        if (snake.bodyIntersectsItem(pt, TOUCH_SIZE_DP)) {
            gameOver = true;
            return false;
        }

        // Move the food if touched
        if (withinRange(pt, food, (FOOD_SIZE_DP + TOUCH_SIZE_DP) * dpToPxFactor)) {
            moveFood();
        }

        // Remove all walls within range of the touched point
        final double dist = (WALL_SIZE_DP + TOUCH_SIZE_DP) * dpToPxFactor;
        walls.removeIf(wall -> withinRange(wall, pt, dist));

        return true;
    }

    /** Move the food to a new random location. */
    private void moveFood() { food = randomPoint(FOOD_SIZE_DP * dpToPxFactor); }

    /** Add a new random wall to the game. */
    private void addWall() { walls.add(randomPoint(WALL_SIZE_DP * dpToPxFactor)); }

    /**
     * Create a new random point that lies completely within the bounds of the
     * world and is not near the snake head.
     * @param size the size of the item, in px
     */
    private PointF randomPoint(float size) {
        while (true) {
            PointF pt = new PointF(
                    random.nextFloat() * (width - 2*size) + size,
                    random.nextFloat() * (height - 2*size) + size
            );
            if (!snake.bodyIntersectsItem(pt, 2*size)) { return pt; }
         }
    }

    /**
     * @return list of all of the current snake body pieces
     */
    public List<PointF> getSnakeBodyLocations() { return snake.getBody(); }

    /**
     * @return the current location of the food
     */
    public PointF getFoodLocation() { return food; }

    /**
     * @return list of all of the current wall location
     */
    public List<PointF> getWallLocations() { return Collections.unmodifiableList(walls); }
}
