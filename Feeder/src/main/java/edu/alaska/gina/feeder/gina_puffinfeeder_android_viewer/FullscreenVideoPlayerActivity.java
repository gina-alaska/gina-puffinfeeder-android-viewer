package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

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
        movie.setOnSystemUiVisibilityChangeListener(new SysUiVisibilityListener());
        movie.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        Bundle e = getIntent().getExtras();
        if (e != null)
            movie.setVideoPath(e.getString("url"));
        else
            movie.setVideoPath("http://feeder-app-prod0.x.gina.alaska.edu/dragonfly/movies/2014/6/26/3585_webcam-uaf-barrow-seaice-images_2014-6-26_1-day-animation.mp4");

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

    private class SysUiVisibilityListener implements View.OnSystemUiVisibilityChangeListener {
        private int delay = Integer.parseInt(getResources().getString(R.string.hide_system_ui_delay));

        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            movie.setSystemUiVisibility(UI_HIDE_OPTIONS);
                        }
                    });
                }
            }, delay);
        }
    }
}
