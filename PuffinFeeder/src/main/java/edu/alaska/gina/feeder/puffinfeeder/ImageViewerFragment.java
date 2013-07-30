package edu.alaska.gina.feeder.puffinfeeder;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment containing WebView that displays full-sized image.
 * Created by bobby on 7/1/13.
 */
public class ImageViewerFragment extends SherlockFragment{
    protected String image_url_small;
    protected String image_url_med;
    protected String image_url_large;
    protected String title;
    protected WebView image_frame;
    protected SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        image_frame = (WebView) getActivity().findViewById(R.id.fragment_feed_image_webview);
        image_frame.getSettings().setBuiltInZoomControls(true);
        image_frame.getSettings().setLoadWithOverviewMode(true);


        image_frame.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                getSherlockActivity().setSupportProgress(newProgress * 100);
                if (newProgress >= 0)
                    getSherlockActivity().setSupportProgressBarIndeterminate(false);
                if (newProgress >= 100) {
                    getSherlockActivity().setSupportProgressBarVisibility(false);
                    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                }
            }
        });

        Bundle extra = getArguments();
        if (extra != null) {
            image_url_small = extra.getString("image_url_small");
            image_url_med = extra.getString("image_url_med");
            image_url_large = extra.getString("image_url_large");
            title = extra.getString("bar_title");
        }
        else {
            Log.d(getString(R.string.app_tag), "No Image URL. Please Fix that...");
            Toast.makeText(getActivity(), "No Image URL. Please fix that...", Toast.LENGTH_SHORT).show();
            return;
        }

        getSherlockActivity().getSupportActionBar().setTitle(title);

        getSherlockActivity().setSupportProgressBarVisibility(true);
        getSherlockActivity().setSupportProgressBarIndeterminate(true);
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                loadme(pickLoadSize());
            }
        });

        loadme(pickLoadSize());
    }

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

    private String pickLoadSize() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo nf = cm.getActiveNetworkInfo();

        if (nf != null) {
            if (sharedPreferences.getBoolean("pref_smart_sizing_on/off", true) && isMetered(nf))
                return sharedPreferences.getString("pref_smart_sizing_size", "small");
            else
                return sharedPreferences.getString("pref_viewer_image_size", "med");
        }
        else {
            Log.d(getString(R.string.app_tag), "NetworkInfo null!");
            return sharedPreferences.getString("pref_viewer_image_size", "med");
        }
    }

    private void loadme(String size) {
        if (size.equals("small"))
            image_frame.getSettings().setUseWideViewPort(false);
        else
            image_frame.getSettings().setUseWideViewPort(true);

        image_frame.loadUrl(getImageUrl(size));
    }

    private String getImageUrl(String sizeString) {
        if (sizeString.equals("small"))
            return image_url_small;
        if (sizeString.equals("med"))
            return image_url_med;
        if (sizeString.equals("large"))
            return image_url_large;

        return null;
    }
}
