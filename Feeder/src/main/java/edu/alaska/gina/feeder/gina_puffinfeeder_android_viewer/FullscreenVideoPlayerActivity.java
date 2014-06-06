package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Activity for playing videos in full screen.
 * Created by Bobby Signor on 6/6/2014.
 */
public class FullscreenVideoPlayerActivity extends Activity {
    private static final int UI_HIDE_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE;
    private MediaController MC;
    private VideoView movie;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        movie.seekTo(savedInstanceState.getInt("seek_pos"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video_player);

        movie = (VideoView) findViewById(R.id.fullscreen_content);
        movie.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        if (getIntent().getExtras() != null)
            movie.setVideoPath(getIntent().getExtras().getString("url"));
        else
            movie.setVideoPath("http://feeder.gina.alaska.edu/feeds/radar-uaf-barrow-seaice-images/movies/3464_radar-uaf-barrow-seaice-images_2014-6-5_1-day-animation.webm");

        MC = new MediaController(this);
        MC.setAnchorView(movie);

        movie.setMediaController(MC);
        movie.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSystemUiVisibility(UI_HIDE_OPTIONS);
            }
        });

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSystemUiVisibility(UI_HIDE_OPTIONS);
            }
        });

        this.getWindow().getDecorView().setSystemUiVisibility(UI_HIDE_OPTIONS);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("seek_pos", movie.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }
}
