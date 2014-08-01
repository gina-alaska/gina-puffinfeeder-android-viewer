package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import edu.alaska.gina.feeder.android.core.data.Entry;

@SuppressWarnings("ConstantConditions")
public class FullscreenViewerActivity extends Activity implements View.OnTouchListener {
    private Entry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_viewer);

        Bundle extras = getIntent().getExtras();
        this.entry = (Entry) extras.getSerializable("entry");
        WebView content = (WebView) findViewById(R.id.contentView);
        content.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
        content.getSettings().setSupportZoom(true);
        content.getSettings().setBuiltInZoomControls(true);
        content.getSettings().setDisplayZoomControls(false);
        content.loadUrl(entry.url);

        if (getActionBar() != null)
            getActionBar().setTitle(extras.getString("feed-title"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fullscreen_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download:
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(this.entry.data_url));
                request.setTitle(getActionBar().getTitle() + "-" + entry.uid + ".jpg");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getActionBar().getTitle() + "-" + entry.uid + ".jpg");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                ((DownloadManager) this.getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
