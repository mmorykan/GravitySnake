package edu.moravian.csci299.gravitysnake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.Arrays;
import java.util.List;

/**
 * The custom View for the Snake Game. This handles the user interaction and
 * sensor information for the snake game but has none of the game logic. That
 * is all within SnakeGame and Snake.
 *
 * NOTE: This class is where most of the work is required. You must document
 * *all* methods besides the constructors (this includes methods already
 * declared that don't have documentation). You will also need to add at least
 * a few methods to this class.
 */
public class SnakeGameView extends View implements SensorEventListener {
    /** The paints and drawables used for the different parts of the game */
    private final Paint scorePaint = new Paint();
    private final Paint gameOverPaint = new Paint();
    private final Paint snakePaint = new Paint();

    /**
     * Drawable for raccoon (Walls), grasshopper (Apples), and snake head, as well as their sizes
     */
    private final Drawable raccoon;
    private final double raccoonRadiusSize;
    private final Drawable grasshopper;
    private final double grasshopperRadiusSize;
    private final Drawable snake;
    private final double snakeRadiusSize;

    /** The metrics about the display to convert from dp and sp to px */
    private final DisplayMetrics displayMetrics;

    /** The snake game for the logic behind this view */
    private final SnakeGame snakeGame;
    Context gameActivity;

    /** Current difficulty, shared preferences, and current score */
    private int difficultyId;
    private final SharedPreferences sharedPreferences;
    private int currentHighScore;

    /**
     * Length increase per food, starting length, initial speed, speed increase per food,
     * and bomb placement probability.
     */
    private final List<List<Double>> difficultyOptions = Arrays.asList(
            Arrays.asList(2.0, 5.0, 1.0, 0.1, 0.0),
            Arrays.asList(2.5, 10.0, 1.5, 0.15, 0.001),
            Arrays.asList(3.0, 12.0, 2.0, 0.2, 0.0025),
            Arrays.asList(4.0, 15.0, 2.5, 0.3, 0.004),
            Arrays.asList(5.0, 20.0, 3.0, 0.5, 0.0075)
    );

    // Required constructors for making your own view that can be placed in a layout
    public SnakeGameView(Context context) { this(context, null);  }
    public SnakeGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Get the metrics for the display so we can later convert between dp, sp, and px
        displayMetrics = context.getResources().getDisplayMetrics();

        // Make the game
        snakeGame = new SnakeGame();
        gameActivity = getContext();
        sharedPreferences = gameActivity.getSharedPreferences(MainActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);

        // Get the drawables and set their "radius"
        raccoon = ResourcesCompat.getDrawable(getResources(), R.mipmap.raccoon_foreground, null);
        raccoonRadiusSize = 3*SnakeGame.WALL_SIZE_DP;
        grasshopper = ResourcesCompat.getDrawable(getResources(), R.mipmap.grasshopper_foreground, null);
        grasshopperRadiusSize = 2*SnakeGame.FOOD_SIZE_DP;
        snake = ResourcesCompat.getDrawable(getResources(), R.mipmap.snake_head_foreground, null);
        snakeRadiusSize = 3*SnakeGame.SNAKE_SIZE_DP;

        // This color is automatically painted as the background
        setBackgroundColor(0xFF228b22);

        // Setup all of the paints and drawables used for drawing later
        setPaint(scorePaint, Color.WHITE);
        setPaint(gameOverPaint, Color.WHITE);
        setTextOptions(scorePaint, 24);
        setTextOptions(gameOverPaint, 60);
        setPaint(snakePaint, Color.rgb(	144, 238, 144));
    }

    /**
     * Sets the paint color.
     * @param paint paint object for text or shape
     * @param color color for the paint
     */
    private void setPaint(Paint paint, int color) {
        paint.setColor(color);
    }

    /**
     * Sets text size and other text options.
     * @param paint paint object for text
     * @param size size of the text
     */
    private void setTextOptions(Paint paint, int size) {
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(spToPx(size));
        paint.setFakeBoldText(true);
    }

    /**
     * @return the snake game for this view
     */
    public SnakeGame getSnakeGame() { return snakeGame; }

    /**
     * Utility function to convert dp units to px units. All Canvas and Paint
     * function use numbers in px units but dp units are better for
     * inter-device support.
     * @param dp the size in dp (device-independent-pixels)
     * @return the size in px (pixels)
     */
    public float dpToPx(float dp) { return dp * displayMetrics.density; }

    /**
     * Utility function to convert sp units to px units. All Canvas and Paint
     * function use numbers in px units but sp units are better for
     * inter-device support, especially for text.
     * @param sp the size in sp (scalable-pixels)
     * @return the size in px (pixels)
     */
    public float spToPx(float sp) { return sp * displayMetrics.scaledDensity; }

    /**
     * @param difficulty the new difficulty for the game
     * Set length increase per food, starting length, initial speed, speed increase per food,
     * and bomb placement probability based off the difficulty.
     */
    public void setDifficulty(int difficulty) {
        difficultyId = difficulty;
        currentHighScore = sharedPreferences.getInt(getDifficulty(difficulty), 0);
        List<Double> currentDifficultyOptions = difficultyOptions.get(difficulty);
        snakeGame.setLengthIncreasePerFood(currentDifficultyOptions.get(0).intValue());
        snakeGame.setStartingLength(currentDifficultyOptions.get(1).intValue());
        snakeGame.setInitialSpeed(currentDifficultyOptions.get(2));
        snakeGame.setSpeedIncreasePerFood(currentDifficultyOptions.get(3));
        snakeGame.setWallPlacementProbability(currentDifficultyOptions.get(4));
    }

    /**
     * Maps the difficulty as an int (from 0-4) and returns the
     * difficulty as a string (Beginner, Easy, Little Harder,
     * Extreme, or God Mode).
     * @param difficulty difficulty as an int
     * @return difficulty as a string
     */
    private String getDifficulty(int difficulty) {
        String diff = "Beginner";
        if (difficulty == 1) diff = "Easy";
        else if (difficulty == 2) diff = "Little Harder";
        else if (difficulty == 3) diff = "Extreme";
        else if (difficulty == 4) diff = "God Mode";

        return diff;
    }

    /**
     * Once the view is laid out, we know the dimensions of it and can start
     * the game with the snake in the middle (if the game hasn't already
     * started). We also take this time to set the dp to px factor of the
     * snake.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // NOTE: this function is done for you
        super.onLayout(changed, left, top, right, bottom);

        if (snakeGame.hasNotStarted()) {

            snakeGame.startGame(right - left, bottom - top);
            snakeGame.setDpToPxFactor(displayMetrics.density);
        }
        invalidate();
    }

    /**
     * Draws all shapes, pictures, and text for the games. Draws circles for each
     * body part of the snake (and a picture of the snake's head), pictures for the
     * grasshopper and each raccoon, and the current score text. Also displays "Game
     * Over" if the game is over.
     * @param canvas the canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        postInvalidateOnAnimation(); //automatically invalidate every frame for continuous playback

        int score = snakeGame.getScore();
        if (currentHighScore < score)
            sharedPreferences.edit().putInt(getDifficulty(difficultyId), score).apply();
        snakeGame.update();

        drawSnake(canvas);
        drawFood(canvas);
        drawWalls(canvas);
        drawText(canvas);
    }

    /**
     * Draws each body part of the snake as circles, and then
     * sets the bounds of and draws the snake's head.
     * @param canvas the canvas
     */
    public void drawSnake(Canvas canvas) {
        for (PointF location: snakeGame.getSnakeBodyLocations()) {
            canvas.drawCircle(location.x, location.y, SnakeGame.SNAKE_SIZE_DP, snakePaint);
        }
        PointF snakeHead = snakeGame.getSnakeBodyLocations().get(0);
        float x = snakeHead.x, y = snakeHead.y;
        snake.setBounds((int)(x - snakeRadiusSize) + 25,
                (int)(y - snakeRadiusSize) + 10,
                (int)(x + snakeRadiusSize) + 25,
                (int)(y + snakeRadiusSize) + 10);
        snake.draw(canvas);
    }

    /**
     * Sets the bounds of and draws the food (grasshopper).
     * @param canvas the canvas
     */
    public void drawFood(Canvas canvas) {
        PointF foodLocation = snakeGame.getFoodLocation();
        float x = foodLocation.x, y = foodLocation.y;
        grasshopper.setBounds((int)(x - grasshopperRadiusSize),
                (int)(y - grasshopperRadiusSize),
                (int)(x + grasshopperRadiusSize),
                (int)(y + grasshopperRadiusSize));
        grasshopper.draw(canvas);
    }

    /**
     * Sets the bounds of and draws the walls (raccoons).
     * @param canvas the canvas
     */
    public void drawWalls(Canvas canvas) {
        for (PointF location : snakeGame.getWallLocations()) {
            float x = location.x, y = location.y;
            raccoon.setBounds((int)(x - raccoonRadiusSize),
                    (int)(y - raccoonRadiusSize),
                    (int)(x + raccoonRadiusSize),
                    (int)(y + raccoonRadiusSize));
            raccoon.draw(canvas);
        }
    }

    /**
     * Updates the current score and displays "Game Over"
     * if the game is over.
     * @param canvas the canvas
     */
    public void drawText(Canvas canvas) {
        canvas.drawText(Integer.toString(snakeGame.getScore()),
                (float)(displayMetrics.widthPixels / 2), displayMetrics.heightPixels - 1200,
                scorePaint);
        if (snakeGame.isGameOver()) {
            canvas.drawText("Game Over", (float)(displayMetrics.widthPixels / 2),
                    (float)(displayMetrics.heightPixels / 2), gameOverPaint);
        }
    }

    /**
     * When the screen is touched, checks if the snake, food, or wall
     * was touched. If the game is over, return to main activity.
     * @param event touching the screen event
     * @return always return true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (snakeGame.isGameOver()) {
            ((Activity)gameActivity).finish();
        }
        int action = event.getAction();
        PointF point = new PointF(event.getX(), event.getY());
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            snakeGame.touched(point);
        }
        invalidate();
        return true;
    }

    /**
     * Calculates the angle that the snake is looking based off the
     * event values and set the direction for the snake game.
     * @param event event that sensor has changed
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        snakeGame.setMovementDirection(Math.atan2(event.values[1], -event.values[0]));
    }

    /** Does nothing but must be provided. */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
