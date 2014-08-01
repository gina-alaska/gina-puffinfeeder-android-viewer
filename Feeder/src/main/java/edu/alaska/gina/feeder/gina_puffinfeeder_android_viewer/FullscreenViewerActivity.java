package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import edu.alaska.gina.feeder.android.core.data.Entry;

@SuppressWarnings("ConstantConditions")
public class FullscreenViewerActivity extends Activity {
    private Entry entry;
    private GestureDetector touchDetector;

    private Handler hideUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hideUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_viewer);
        touchDetector = new GestureDetector(this, new TouchDetector());

        Bundle extras = getIntent().getExtras();
        this.entry = (Entry) extras.getSerializable("entry");
        WebView content = (WebView) findViewById(R.id.contentView);
        content.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
        content.getSettings().setSupportZoom(true);
        content.getSettings().setBuiltInZoomControls(true);
        content.getSettings().setDisplayZoomControls(false);
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return touchDetector.onTouchEvent(event);
            }
        });
        content.loadUrl(entry.url);

        if (getActionBar() != null)
            getActionBar().setTitle(extras.getString("feed-title"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        delayedHide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideUIHandler.removeMessages(0);
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

    private void delayedHide() {
        hideUIHandler.removeMessages(0);
        hideUIHandler.sendEmptyMessageDelayed(0, 2000);
    }

    private void hideUI() {
        hideUIHandler.removeMessages(0);
        getActionBar().hide();
    }

    private void showUI() {
        getActionBar().show();
    }

    private class TouchDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (getActionBar().isShowing()) {
                hideUI();
                return true;
            } else {
                showUI();
                delayedHide();
                return false;
            }
        }
    }
}
