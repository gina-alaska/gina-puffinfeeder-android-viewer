package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
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
    private final SpiceManager manager = new SpiceManager(JsonSpiceService.class);
    private PhotoViewAttacher photoAttacher;
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
            url = b.getString("image_url_" + b.getInt("position") + "_" + pickLoadSize(PreferenceManager.getDefaultSharedPreferences(this)));
            networkRequest();
        } else {
            image = retained.image;
            fullscreenImage.setImageBitmap(image);
            photoAttacher.update();
        }

        this.getWindow().getDecorView().setSystemUiVisibility(UI_HIDE_OPTIONS);
    }

    @Override
    protected void onRestart() {
        networkRequest();
        super.onRestart();
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
        if (!manager.isStarted())
            manager.start(this);

        manager.execute(new BitmapRequest(url, new File(getCacheDir().getAbsolutePath() + "images.cache")), new BitmapRequestListener());
    }

    /** Methods to choose size. */

    /**
     * Determines what sized image to load in the viewer.
     * @return URL of image to be loaded.
     */
    private String pickLoadSize(SharedPreferences sharedPreferences) {
        ConnectivityManager connectivityManager = getConnectivityManager();
        NetworkInfo nf = connectivityManager.getActiveNetworkInfo();

        if (nf != null) {
            if (isMetered(nf))
                return sharedPreferences.getString("pref_smart_sizing_size", "0");
            else
                return sharedPreferences.getString("pref_viewer_image_size", "1");
        }
        else {
            Log.d(getString(R.string.app_tag), "NetworkInfo null!");
            return sharedPreferences.getString("pref_viewer_image_size", "1");
        }
    }

    /**
     * Grabs the ConnectivityManager object representing the device's networks.
     * @return ConnectivityManager of the device.
     */
    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    /**
     * Determines if the network being used is a mobile network.
     * @param net1 The NetworkInfo object representing the network to be tested.
     * @return "true" if network is mobile (3/4G). "false" if it is not (wifi).
     */
    private boolean isMetered(NetworkInfo net1) {
        int type = net1.getType();
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
                return true;
            default:
                return false;
        }
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
            //manager.shouldStop();
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            image = retained.image = bitmap;
            fullscreenImage.setImageBitmap(bitmap);
            photoAttacher.update();
/*
            if (manager.isStarted())
                manager.shouldStop();*/
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
