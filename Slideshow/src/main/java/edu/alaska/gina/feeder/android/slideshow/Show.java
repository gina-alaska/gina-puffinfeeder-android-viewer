package edu.alaska.gina.feeder.android.slideshow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.octo.android.robospice.spicelist.simple.BitmapSpiceManager;

import java.io.File;

import edu.alaska.gina.feeder.android.core.data.Entry;
import edu.alaska.gina.feeder.android.slideshow.network.JSONRequest;
import edu.alaska.gina.feeder.android.slideshow.network.JsonSpiceService;

/**
 * Feeder automated slideshow application.
 */
public class Show extends Activity {
    private String baseURL = "http://feeder-web-dev.x.gina.alaska.edu/feeds/snpp-day-night-band/entries.json";

    private ViewFlipper contentView;
    private ImageView image1, image2;
    private View progressBar;
    private SpiceManager jsonManager = new SpiceManager(JsonSpiceService.class);
    private BitmapSpiceManager imageManager = new BitmapSpiceManager();
    private Entry[] contentData;

    private Thread timer = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d("slideshow_debug", "Timer reset.");
            timerThreadRunning = true;
            contentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("slideshow_debug", "Timer complete.");
                    timerDone = true;
                    flipView();
                }
            }, DurationInMillis.ONE_MINUTE / 2);
        }
    });

    /**
     * Image that is currently being downloaded.
     */
    private int current = 0;

    /**
     * Flags indicating whether the timer, download, and timer thread are running or complete.
     */
    private boolean timerDone = true, downloadDone = false, timerThreadRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        if (timerThreadRunning)
            this.timer.interrupt();

        timerDone = true;
        downloadDone = false;
        timerThreadRunning = false;

        this.contentView = (ViewFlipper) findViewById(R.id.flipper);
        this.image1 = (ImageView) this.contentView.findViewById(R.id.image1);
        this.image2 = (ImageView) this.contentView.findViewById(R.id.image2);
        this.progressBar = this.findViewById(R.id.loadingIndicator);

        this.contentView.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        this.contentView.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
        if (!this.imageManager.isStarted())
            this.imageManager.start(this);

        UIDDialogue d = new UIDDialogue();
        d.show(getFragmentManager(), "uid_dialogue");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
        if (!this.imageManager.isStarted())
            this.imageManager.start(this);

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (jsonManager.isStarted())
            jsonManager.shouldStop();
        if (imageManager.isStarted())
            imageManager.shouldStop();

        if (timerThreadRunning)
            this.timer.interrupt();
    }

    private void flipView() {
        Log.d("slideshow_debug", "Starting flipView method.");
        if (this.progressBar.getVisibility() == View.VISIBLE) {
            Log.d("slideshow_debug", "ProgressBar removal starting.");
            this.progressBar.setAlpha(1f);
            this.progressBar.animate().alpha(0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progressBar.setVisibility(View.GONE);
                            contentView.setVisibility(View.VISIBLE);
                            Log.d("slideshow_debug", "ProgressBar is gone.");
                        }
                    });
        }

        Log.d("slideshow_debug", "timerDone = " + timerDone);
        Log.d("slideshow_debug", "downloadDone = " + downloadDone);

        if (this.timerDone && this.downloadDone) {
            this.contentView.showNext();

            this.timerDone = false;
            this.downloadDone = true;

            Log.d("slideshow_debug", "Flipping triggered & flags reset.");

            if (!timerThreadRunning) {
                Log.d("slideshow_debug", "Timer started");
                timer.start();
            } else {
                timer.run();
            }
            tryRequestNextImage();
        }
    }

    private void tryRequestNextImage() {
        if (contentData != null && current < contentData.length) {
            this.imageManager.execute(new BitmapRequest(this.contentData[current++].preview_url + "?size=2048x2048", new File(getCacheDir().getAbsolutePath() + "images.cache")), new ImageListener());
        } else {
            this.jsonManager.execute(new JSONRequest<Entry[]>(Entry[].class, baseURL), "slideshow_entries", DurationInMillis.ALWAYS_EXPIRED, new EntriesRequestListener());
            //TODO Figure out what was supposed to be on this line.
        }
    }

    private void loadIntoView(Bitmap bitmap) {
        if (this.image1.getVisibility() != View.VISIBLE) {
            this.image1.setImageBitmap(bitmap);
        } else {
            this.image2.setImageBitmap(bitmap);
        }
    }

    private class EntriesRequestListener implements RequestListener<Entry[]> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d("slideshow_debug", "Entries download fail!");
            tryRequestNextImage();
        }

        @Override
        public void onRequestSuccess(Entry[] entries) {
            contentData = entries;
            current = 0;
            tryRequestNextImage();
        }
    }

    private class ImageListener implements RequestListener<Bitmap> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d("slideshow_debug", "Image download fail!");
            tryRequestNextImage();
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            downloadDone = true;
            loadIntoView(bitmap);
            flipView();
        }
    }

    private class UIDDialogue extends DialogFragment {
        private View v;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            v = getActivity().getLayoutInflater().inflate(R.layout.uid_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Enter Code:").setView(v)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (v.findViewById(R.id.newUID) != null)
                                baseURL = "/slideshow/" + ((EditText) v.findViewById(R.id.newUID)).getText();
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    //
                                }
                            });
                            dismiss();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    tryRequestNextImage();
                                }
                            });
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
