package edu.moravian.csci299.gravitysnake;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    /** The Spinner menu, music switch, and high score, description, and difficulty textViews */
    private SeekBar difficultySelector;
    private SwitchCompat music;
    private TextView highScore, description, difficulty;

    /** The media player for playing music */
    private MediaPlayer mediaPlayer;

    /** List of difficulties and a hashmap of them with their descriptions */
    List<String> difficulties;
    private HashMap<String, String> descriptions;

    /** Access to Shared Preferences */
    private SharedPreferences sharedPreferences;
    public static final String SHARED_PREF_FILE = "SharedPreferences";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up shared preferences and its editor
        sharedPreferences = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);

        // Get the difficulty spinner, music switch, high score textview, and description textview
        difficultySelector = findViewById(R.id.difficulty_select);
        difficulties = Arrays.asList("Beginner", "Easy", "Little Harder", "Extreme", "God Mode");
        difficulty = findViewById(R.id.difficulty);
        music = findViewById(R.id.music);
        highScore = findViewById(R.id.high_score);
        description = findViewById(R.id.description);

        // Populate the descriptions HashMap with difficulties and their descriptions
        addDifficultyDescriptions();

        // Set all listeners
        difficultySelector.setOnSeekBarChangeListener(this);
        music.setOnCheckedChangeListener(this);
        findViewById(R.id.start_button).setOnClickListener(this);

        loadData();  // Load any saved data from the previous gameplay

        // Create MediaPlayer and start music if music is checked
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.booamf);
        mediaPlayer.setLooping(true);
        if (music.isChecked()) mediaPlayer.start();
    }

    /**
     * When the start button is clicked to start the GameActivity
     * @param v The play game button
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("difficulty", difficultySelector.getProgress());
        startActivity(intent);
    }

    /** When the activity stops, release the media player. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    /** When this activity is resumed, load in shared preferences. */
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * Save the state of the music and start or stop the music from playing
     * @param buttonView The music switch
     * @param isChecked Whether or not the music switch is set
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        sharedPreferences.edit().putBoolean("musicOn", isChecked).apply();
        if (mediaPlayer != null) {
            if (isChecked) {
                mediaPlayer.start();
            } else {
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
            }
        }
    }

    /**
     * Add difficulties as keys to a hashMap with their descriptions as the values
     */
    private void addDifficultyDescriptions() {
        descriptions = new HashMap<>();
        descriptions.put("Beginner", "You suck!");
        descriptions.put("Easy", "Weaksauce.");
        descriptions.put("Little Harder", "Break a sweat");
        descriptions.put("Extreme", "Fast as fuck boiii");
        descriptions.put("God Mode", "Don't bother...");
    }

    /**
     * Load in the past difficulty, high score, music state, and description of past difficulty
     */
    private void loadData() {
        String diff = sharedPreferences.getString("difficulty", "Beginner");
        int score = sharedPreferences.getInt(diff, 0);
        boolean isMusicOn = sharedPreferences.getBoolean("musicOn", true);

        difficulty.setText(diff);
        difficultySelector.setProgress(difficulties.indexOf(diff));
        highScore.setText(getString(R.string.high_score, String.valueOf(score)));
        music.setChecked(isMusicOn);
    }

    /**
     * When the seek bar's positions is changed, set the text for difficulty, description, and score
     * @param seekBar The Layout element
     * @param progress The progress of the seek bar
     * @param fromUser The user interacted with the seek bar
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String diff = difficulties.get(progress);
        String diffDescription = descriptions.get(diff);
        int score = sharedPreferences.getInt(diff, 0);

        difficulty.setText(diff);
        description.setText(diffDescription);
        highScore.setText(getString(R.string.high_score, String.valueOf(score)));
        sharedPreferences.edit().putString("difficulty", diff).apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

}