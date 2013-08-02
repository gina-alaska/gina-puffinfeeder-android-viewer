package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;

public class WebViewActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle url = getIntent().getExtras();
        WebView screen = (WebView) findViewById(R.id.credits_web_view);
        this.getSupportActionBar().setTitle(url.getString("title", "Page not found - GitHub"));
        screen.loadUrl(url.getString("url", "https://github.com/404"));
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
