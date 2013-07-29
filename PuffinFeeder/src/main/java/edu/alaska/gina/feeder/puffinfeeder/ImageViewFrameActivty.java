package edu.alaska.gina.feeder.puffinfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import java.util.ArrayList;

/**
 * Activity that displays a full image while allowing navigation via 2 buttons across the bottom.
 * Created by bobby on 7/1/13.
 */
public class ImageViewFrameActivty extends SherlockFragmentActivity implements View.OnClickListener {
    protected ArrayList<String[]> urls = new ArrayList<String[]>();
    protected ArrayList<String> titles = new ArrayList<String>();
    protected String feed;
    protected int position;

    protected int numSizes = 3;

    protected Button newer;
    protected Button older;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_image_view_frame);

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle args = getIntent().getExtras();
        urls = build3SizeArrayStructure(decodeUrlBundle(args, "url"));
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
        info.putString("image_url_small", urls.get(newPos)[0]);
        info.putString("image_url_med", urls.get(newPos)[1]);
        info.putString("image_url_large", urls.get(newPos)[2]);
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

    public ArrayList<String> decodeUrlBundle(Bundle encoded, String key) {
        ArrayList<String> decoded = new ArrayList<String>();
        this.numSizes = encoded.getInt("num_image_sizes");

        for (int i = 0; encoded.getString("image_" + key + "_" + i + "_0") != null; i++) {
            for (int j = 0; j < numSizes; j++)
                decoded.add(encoded.getString("image_" + key + "_" + i + "_" + j));
        }

        return decoded;
    }

    public ArrayList<String[]> build3SizeArrayStructure(ArrayList<String> unstructured) {
        ArrayList<String[]> export = new ArrayList<String[]>();

        for (int i = 0; i < unstructured.size(); i += 3)
            export.add(new String[]{unstructured.get(i), unstructured.get(i + 1), unstructured.get(i + 2)});

        return export;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
                return true;
            case R.id.action_open_preferences:
                this.startActivity(new Intent(this, PreferencesActivity.class));
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