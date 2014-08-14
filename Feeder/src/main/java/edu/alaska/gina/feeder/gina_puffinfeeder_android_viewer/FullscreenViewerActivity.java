package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.alaska.gina.feeder.android.core.data.Entry;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.mediaViewer.VideoEnabledWebChromeClient;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.mediaViewer.VideoEnabledWebView;

@SuppressWarnings("ConstantConditions")
public class FullscreenViewerActivity extends Activity implements View.OnTouchListener {
    private VideoEnabledWebView content;
    private GestureDetector touchDetector;
    private Entry entry;
    private FrameLayout frame;
    private String feedTitle;
    private String category;

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
        this.touchDetector = new GestureDetector(this, new TouchDetector());

        Bundle extras = getIntent().getExtras();
        this.category = extras.getString("feed-type");
        this.feedTitle = extras.getString("feed-title");
        this.entry = (Entry) extras.getSerializable("entry");

        //TODO Finish implementing new WebChromeClient
        FrameLayout fullscreenView = (FrameLayout) findViewById(R.id.fullscreenView);
        this.content = new VideoEnabledWebView(this);
        VideoEnabledWebChromeClient videoClient = new VideoEnabledWebChromeClient(this.content, fullscreenView, null, this.content) {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
            }
        };
        videoClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            }
        });

        this.frame = (FrameLayout) findViewById(R.id.contentView);
        this.content.getSettings().setJavaScriptEnabled(true);
        this.content.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
        this.content.getSettings().setSupportZoom(true);
        this.content.getSettings().setBuiltInZoomControls(true);
        this.content.getSettings().setDisplayZoomControls(false);
        this.content.setWebChromeClient(videoClient);
        this.content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(getString(R.string.app_tag) + "-input", "WebView onTouch");
                hideUIHandler.removeMessages(0);
                return touchDetector.onTouchEvent(event);
            }
        });
        if (savedInstanceState == null)
            this.content.loadUrl(entry.url);
        this.frame.addView(this.content);

        if (getActionBar() != null)
            getActionBar().setTitle(this.feedTitle);

        getWindow().getDecorView().setOnTouchListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.content.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideUI();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.content.saveState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideUIHandler.removeMessages(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fullscreen_viewer, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider shareItem = (ShareActionProvider) item.getActionProvider();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, entry.url);
        shareIntent.setType("text/plain");
        shareItem.setShareIntent(shareIntent);

        if (!this.category.equals("Image"))
            menu.findItem(R.id.action_download).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(getString(R.string.app_tag) + "-input", "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download:
                downloadData();
                return true;
            case R.id.action_details:
                showDetails();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadData() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(this.entry.data_url));
        request.setTitle(entry.data_url.substring(entry.data_url.lastIndexOf('/') + 1));
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, entry.data_url.substring(entry.data_url.lastIndexOf('/') + 1));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        ((DownloadManager) this.getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
    }

    private void showDetails() {
        DetailsDialog d = new DetailsDialog();
        d.show(getFragmentManager(), "details");
    }

    private void delayedHide() {
        hideUIHandler.removeMessages(0);
        hideUIHandler.sendEmptyMessageDelayed(0, 4000);
    }

    private void hideUI() {
        hideUIHandler.removeMessages(0);
        getActionBar().hide();
    }

    private void showUI() {
        getActionBar().show();
        delayedHide();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(getString(R.string.app_tag) + "-input", "Activity onTouch");
        this.hideUIHandler.removeMessages(0);
        return false;
    }

    private class TouchDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (getActionBar().isShowing()) {
                hideUI();
                return true;
            } else {
                showUI();
                return false;
            }
        }
    }

    public static class DetailsDialog extends DialogFragment {
        private Holder details;

        @Override
        public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_details, null);
            DateTimeFormatter dateFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
            this.details = (Holder) getFragmentManager().findFragmentByTag("details_info");
            if (this.details == null) {
                this.details = new Holder();
                this.details.innerEntry = ((FullscreenViewerActivity) getActivity()).entry;
                getFragmentManager().beginTransaction().add(this.details, "details_info");
            }

            ((TextView) v.findViewById(R.id.timestamp)).setText(dateFormat.print(this.details.innerEntry.event_at));
            if (this.details.innerEntry.highlighted) {
                ((TextView) v.findViewById(R.id.description)).setText(this.details.innerEntry.highlight_description);
                v.findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                v.findViewById(R.id.description).setVisibility(View.VISIBLE);
            }
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setCustomTitle(null);
            b.setView(v);
            return b.create();
        }

        public static class Holder extends Fragment {
            public Entry innerEntry;

            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setRetainInstance(true);
            }
        }
    }
}
