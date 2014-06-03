package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.BitmapRequest;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;

public class FullscreenImageViewerActivity extends Activity {
    private static final int UI_HIDE_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE;
    SpiceManager manager = new SpiceManager(JsonSpiceService.class);
    PhotoViewAttacher photoAttacher;
    private String url;
    private DataFragment retained;
    private FragmentManager fragmentManager;
    private ImageView fullscreenImage;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image_viewer);

        fragmentManager = getFragmentManager();
        retained = (DataFragment) fragmentManager.findFragmentByTag("data");
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.findViewById(R.id.fullscreen_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setSystemUiVisibility(UI_HIDE_OPTIONS);
            }
        });

        fullscreenImage = (ImageView)findViewById(R.id.fullscreen_content);
        photoAttacher = new PhotoViewAttacher(fullscreenImage);
        photoAttacher.setOnPhotoTapListener(new PhotoTapListener());

        if (retained == null) {
            retained = new DataFragment();
            fragmentManager.beginTransaction().add(retained, "data").commit();

            Bundle b = getIntent().getExtras();
            url = b.getString("image_url_" + b.getInt("position") + "_2");
            networkRequest();
        } else {
            image = retained.image;
            fullscreenImage.setImageBitmap(image);
            photoAttacher.update();
        }

        this.getWindow().getDecorView().setSystemUiVisibility(UI_HIDE_OPTIONS);
    }

    @Override
    protected void onPause() {
        if (manager.isStarted()) {
            manager.cancelAllRequests();
            manager.shouldStop();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (manager.isStarted()) {
            manager.cancelAllRequests();
            manager.shouldStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        retained.image = this.image;
        if (photoAttacher != null)
            photoAttacher.cleanup();
    }

    private void networkRequest() {
        manager.start(this);
        BitmapRequest br = new BitmapRequest(url, new File(getCacheDir().getAbsolutePath() + "images.cache"));
        manager.execute(br, new BitmapRequestListener());
    }

    protected void navUp() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {
        @Override
        public void onPhotoTap(View view, float v, float v2) {
            finish();
        }
    }

    private class BitmapRequestListener implements RequestListener<Bitmap> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(getBaseContext(), "Image Request Fail!", Toast.LENGTH_LONG).show();
            manager.shouldStop();
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            image = retained.image = bitmap;
            fullscreenImage.setImageBitmap(bitmap);
            photoAttacher.update();

            if (manager.isStarted())
                manager.shouldStop();
        }
    }

    private class DataFragment extends Fragment {
        public Bitmap image;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
