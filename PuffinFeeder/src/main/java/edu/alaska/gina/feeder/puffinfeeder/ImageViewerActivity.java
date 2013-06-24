package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Takes the information passed from the main activity, converts it into a REST
 * compatible URL, then displays the content at that link (the image) in a WebView.
 * Created by bobby on 6/14/13.
 */
public class ImageViewerActivity extends SherlockActivity {
    protected String image_url;
    protected String title;
    protected WebView image_frame;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            image_url = extra.getString("image_url");
            title = extra.getString("bar_title");
        }
        else {
            Log.d("Puffin Feeder", "No Image URL. Please Fix that...");
            Toast.makeText(this, "No Image URL. Please fix that...", Toast.LENGTH_SHORT).show();
            return;
        }

        getSupportActionBar().setTitle(title);
        WebView image_frame = (WebView) findViewById(R.id.feed_image_webView);
        image_frame.getSettings().setBuiltInZoomControls(true);
        image_frame.getSettings().setLoadWithOverviewMode(true);
        image_frame.getSettings().setUseWideViewPort(true);
        image_frame.loadUrl(image_url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}