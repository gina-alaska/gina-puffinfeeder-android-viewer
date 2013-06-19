package edu.alaska.gina.feeder.puffinfeeder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

/**
 * Takes the information passed from the main activity, converts it into a REST
 * compatable URL, then displays the content at that link (the image) in a WebView.
 * Created by bobby on 6/14/13.
 */
public class ImageViewerActivity extends Activity {
    String image_url;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extra = getIntent().getExtras();
        if (extra != null)
            image_url = extra.getString("image_url");
        else {
            Log.d("Puffin Feeder", "No Image URL. Please Fix that...");
            return;
        }

        WebView image_frame = (WebView) findViewById(R.id.feed_image_webView);
        image_frame.loadUrl(image_url);
    }
}