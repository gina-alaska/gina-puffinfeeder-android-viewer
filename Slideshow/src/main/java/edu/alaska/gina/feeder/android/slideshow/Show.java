package edu.alaska.gina.feeder.android.slideshow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
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
    private String baseURL = "http://feeder-web-dev.x.gina.alaska.edu/feeds/snpp-truecolor/entries.json";
    private ViewFlipper contentView;
    private ImageView image1, image2;
    private View progressBar;
    private SpiceManager jsonManager = new SpiceManager(JsonSpiceService.class);
    private BitmapSpiceManager imageManager = new BitmapSpiceManager();
    private Entry[] contentData;

    /**
     * Image that is currently being downloaded.
     */
    private int current = 0;

    /**
     * Boolean values indicating whether downloading and the timer are done.
     */
    private boolean timerDone = true, downloadDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        this.contentView = (ViewFlipper) findViewById(R.id.flipper);
        this.image1 = (ImageView) this.contentView.findViewById(R.id.image1);
        this.image2 = (ImageView) this.contentView.findViewById(R.id.image2);
        this.progressBar = this.findViewById(R.id.loadingIndicator);

        this.contentView.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        this.contentView.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO If no UID, display dialogue to set one

        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
        if (!this.imageManager.isStarted())
            this.imageManager.start(this);

        tryRequestNextImage();
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
    protected void onPause() {
        super.onPause();

        if (jsonManager.isStarted())
            jsonManager.shouldStop();
        if (imageManager.isStarted())
            imageManager.shouldStop();
    }

    private void flipView() {
        if (this.progressBar.getVisibility() == View.VISIBLE) {
            this.progressBar.setVisibility(View.GONE);
            this.contentView.setVisibility(View.VISIBLE);
        }

        if (this.timerDone && this.downloadDone) {
            contentView.showNext();

            //TODO Reset the timer
        }

        tryRequestNextImage();
    }

    private void tryRequestNextImage() {
        this.downloadDone = false;
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
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            downloadDone = true;
            loadIntoView(bitmap);
            flipView();
        }
    }
}
