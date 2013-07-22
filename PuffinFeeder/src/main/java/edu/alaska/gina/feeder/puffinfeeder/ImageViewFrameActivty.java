package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import java.util.ArrayList;

/**
 * Activity that displays a full image while allowing navigation via 2 buttons across the bottom.
 * Created by bobby on 7/1/13.
 */
public class ImageViewFrameActivty extends SherlockFragmentActivity implements View.OnClickListener {
    protected ArrayList<String> urls = new ArrayList<String>();
    protected ArrayList<String> titles = new ArrayList<String>();
    protected String feed;
    protected int position;

    Button newer;
    Button older;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_image_view_frame);

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle args = getIntent().getExtras();
        urls = decodeBundle(args, "url");
        titles = decodeBundle(args, "title");
        position = args.getInt("position");
        feed = args.getString("feed_name");

        newer = (Button) findViewById(R.id.navigation_newer);
        older = (Button) findViewById(R.id.navigation_older);

        newer.setOnClickListener(this);
        older.setOnClickListener(this);

        newImage(position);
        endOfLine();
    }

    public void newImage(int newPos) {
        ImageViewerFragment iFrag = new ImageViewerFragment();
        Bundle info = new Bundle();
        info.putString("image_url", urls.get(newPos));
        info.putString("bar_title", feed + " - " + titles.get(newPos));
        iFrag.setArguments(info);

        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.image_content_frame, iFrag).commit();

        position = newPos;
    }

    public ArrayList<String> decodeBundle(Bundle encoded, String key) {
        ArrayList<String> decoded = new ArrayList<String>();

        for (int i = 0; encoded.getString("image_" + key + "_" + i) != null; i++)
            decoded.add(encoded.getString("image_" + key + "_" + i));

        return decoded;
    }

    public void endOfLine() {
        if (position <= 0) {
            newer.setVisibility(View.GONE);
            newer.setClickable(false);
            older.setClickable(true);
            older.setVisibility(View.VISIBLE);
        }

        else if (position >= urls.size() - 1) {
            newer.setVisibility(View.VISIBLE);
            newer.setClickable(true);
            older.setClickable(false);
            older.setVisibility(View.GONE);
        }

        else {
            newer.setVisibility(View.VISIBLE);
            newer.setClickable(true);
            older.setClickable(true);
            older.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation_newer:
                if (position > 0)
                    newImage(position - 1);
                break;
            case R.id.navigation_older:
                if (position < urls.size() - 1)
                    newImage(position + 1);
                break;
        }
        endOfLine();
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
        super.onBackPressed();
    }
}