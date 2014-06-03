package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.BitmapRequest;

import java.io.File;


public class FullscreenImageViewerActivity extends Activity {
    private static final int UI_HIDE_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE;
    SpiceManager manager = new SpiceManager(JsonSpiceService.class);
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image_viewer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.start(this);
        this.findViewById(R.id.fullscreen_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSystemUiVisibility(UI_HIDE_OPTIONS);
            }
        });

        Bundle b = getIntent().getExtras();
        url = b.getString("image_url_" + b.getInt("position") + "_2");
        networkRequest();

        this.getWindow().getDecorView().setSystemUiVisibility(UI_HIDE_OPTIONS);
    }

    @Override
    protected void onPause() {
        manager.shouldStop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        manager.shouldStop();
        super.onStop();
    }

    private void networkRequest() {
        BitmapRequest br = new BitmapRequest(url, new File(getCacheDir().getAbsolutePath() + "file.jpg"));
        manager.execute(br, new BitmapRequestListener());
    }

    private class BitmapRequestListener implements RequestListener<Bitmap> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(getBaseContext(), "Image Request Fail!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            ((ImageView)findViewById(R.id.fullscreen_content)).setImageBitmap(bitmap);
        }
    }
}
