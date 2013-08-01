package edu.alaska.gina.feeder.puffinfeeder;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
    protected ConnectivityManager connectivityManager;

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

        connectivityManager = getConnectivityManager();

        image_frame.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                try {
                    getSherlockActivity().setSupportProgress(newProgress * 100);
                    if (newProgress >= 0)
                        getSherlockActivity().setSupportProgressBarIndeterminate(false);
                    if (newProgress >= 100) {
                        getSherlockActivity().setSupportProgressBarVisibility(false);
                        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                    }
                } catch (NullPointerException e) {
                    Log.d(getString(R.string.app_tag), "ProgressBar NullPointer!\n" + e.getStackTrace());
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
            if (isAdded())
                loadme(pickLoadSize());
            }
        });

        if (isAdded())
            loadme(pickLoadSize());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        image_frame.destroy();
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
        connectivityManager = getConnectivityManager();
        NetworkInfo nf = connectivityManager.getActiveNetworkInfo();

        if (nf != null) {
            if (isMetered(nf))
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

        image_frame.stopLoading();
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

    private ConnectivityManager getConnectivityManager() {
        if (isAdded())
            return (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        return this.connectivityManager;
    }
}
