package edu.alaska.gina.feeder.puffinfeeder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Takes the information passed from the main activity, converts it into a REST
 * compatable URL, then displays the content at that link (the image) in a WebView.
 * Created by bobby on 6/14/13.
 */
public class ImageViewerActivity extends SherlockActivity {
    protected String image_url;
    protected String title;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            image_url = extra.getString("image_url");
            title = extra.getString("title");
        }
        else {
            Log.d("Puffin Feeder", "No Image URL. Please Fix that...");
            Toast.makeText(this, "No Image URL. Please fix that...", Toast.LENGTH_SHORT).show();
            return;
        }

        WebView image_frame = (WebView) findViewById(R.id.feed_image_webView);
        image_frame.loadUrl(image_url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}