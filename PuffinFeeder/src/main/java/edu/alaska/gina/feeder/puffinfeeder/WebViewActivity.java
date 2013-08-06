package edu.alaska.gina.feeder.puffinfeeder;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle url = getIntent().getExtras();
        WebView screen = (WebView) findViewById(R.id.info_web_view);
        this.getActionBar().setTitle(url.getString("title", "Page not found - GitHub"));
        screen.loadUrl(url.getString("url", "https://github.com/404"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
