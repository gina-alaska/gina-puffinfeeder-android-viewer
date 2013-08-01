package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;

public class WebViewActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        WebView screen = (WebView) findViewById(R.id.credits_web_view);
        this.getSupportActionBar().setTitle("Credits and Licenses");
        screen.loadUrl("file:///android_res/raw/credits.html");
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
