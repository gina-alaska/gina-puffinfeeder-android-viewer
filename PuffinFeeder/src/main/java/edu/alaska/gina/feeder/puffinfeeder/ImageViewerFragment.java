package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;
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
    protected String image_url;
    protected String title;
    protected WebView image_frame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
        image_frame.getSettings().setUseWideViewPort(true);

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
            image_url = extra.getString("image_url");
            title = extra.getString("bar_title");
        }
        else {
            Log.d("Puffin Feeder", "No Image URL. Please Fix that...");
            Toast.makeText(getActivity(), "No Image URL. Please fix that...", Toast.LENGTH_SHORT).show();
            return;
        }

        getSherlockActivity().getSupportActionBar().setTitle(title);

        getSherlockActivity().setSupportProgressBarVisibility(true);
        getSherlockActivity().setSupportProgressBarIndeterminate(true);
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        image_frame.loadUrl(image_url);
    }
}
