package edu.alaska.gina.feeder.puffinfeeder;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Activity that displays a full image while allowing navigation via 2 buttons across the bottom.
 * Created by bobby on 7/1/13.
 */
public class ImageViewFrameActivty extends Activity implements View.OnClickListener {
    protected ArrayList<String[]> urls = new ArrayList<String[]>();
    protected ArrayList<String> titles = new ArrayList<String>();
    protected ArrayList<DateTime> times = new ArrayList<DateTime>();
    protected String feed;
    protected String description;
    protected String infoUrl;
    protected int position;

    protected int numSizes = 3;
    protected Toast toasty;

    protected Button newer;
    protected Button older;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_image_view_frame);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        Bundle args = getIntent().getExtras();
        urls = build3SizeArrayStructure(decodeUrlBundle(args, "url"));
        titles = decodeBundle(args, "title");
        times = parseTimeStrings_ISO8601(decodeBundle(args, "time"));
        position = args.getInt("position");
        feed = args.getString("feed_name");
        description = args.getString("description");
        infoUrl = args.getString("info");

        newer = (Button) findViewById(R.id.navigation_newer);
        older = (Button) findViewById(R.id.navigation_older);

        newer.setOnClickListener(this);
        older.setOnClickListener(this);

        toasty = Toast.makeText(this, "blargh", Toast.LENGTH_LONG);
        newImage(position);
        endOfLine();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewer, menu);
        return true;
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
                finish();
                return true;
            case R.id.action_open_preferences:
                this.startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            case R.id.action_display_short_description:
                DateTime t;
                if (feed.equals("Barrow Radar") || feed.equals("Barrow Radar GeoTIF") || feed.equals("Barrow Webcam"))
                    t = times.get(position).withZone(DateTimeZone.forID("America/Anchorage"));
                else
                    t = times.get(position).withZone(DateTimeZone.forID("UTC"));
                Bundle x = new Bundle();

                x.putString("description", buildDescription(t));
                x.putString("title", titles.get(position));

                ShortDescriptionFragment dFrag = new ShortDescriptionFragment();
                dFrag.setArguments(x);

                dFrag.show(getFragmentManager(), "description_dialog");
                return true;
            case R.id.action_display_short_feed_description:
                Bundle info = new Bundle();
                info.putString("description", description);
                info.putString("title", feed);
                info.putString("url", infoUrl);

                ShortDescriptionFragment dFrag2 = new ShortDescriptionFragment();
                dFrag2.setArguments(info);

                dFrag2.show(getFragmentManager(), "description_dialog");

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        toasty.cancel();
    }

    public void newImage(int newPos) {
        ImageViewerFragment iFrag = new ImageViewerFragment();
        Bundle info = new Bundle();
        info.putString("image_url_small", urls.get(newPos)[0]);
        info.putString("image_url_med", urls.get(newPos)[1]);
        info.putString("image_url_large", urls.get(newPos)[2]);
        info.putString("bar_title", feed + " - " + titles.get(newPos));
        iFrag.setArguments(info);

        toasty.setText(findTimeDifference(times.get(newPos)));
        toasty.show();
        Log.d(getString(R.string.app_tag), findTimeDifference(times.get(newPos)));
        getFragmentManager().beginTransaction().replace(R.id.image_content_frame, iFrag).commit();

        position = newPos;
    }

    public ArrayList<DateTime> parseTimeStrings_ISO8601(ArrayList<String> s) {
        ArrayList<DateTime> d = new ArrayList<DateTime>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern(getString(R.string.ISO8601_pattern));

        for (String t : s)
            d.add(formatter.parseDateTime(t));

        return d;
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

    public String buildDescription(DateTime timeDate) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        sb.append("Processed " + findTimeDifference(timeDate) + "\n");

        sb.append("Date Processed: ");
        sb.append(timeDate.monthOfYear().getAsText(Locale.getDefault()) + " ");
        sb.append(timeDate.dayOfMonth().getAsText(Locale.getDefault()) + ", ");
        sb.append(timeDate.yearOfEra().getAsText(Locale.getDefault()) + "\n");

        sb.append("Time Processed: ");
        temp.append(timeDate.hourOfDay().getAsText(Locale.getDefault()));
        if (temp.length() < 2) {
            while (temp.length() < 2)
                temp.insert(0, "0");
        }
        sb.append(temp);
        sb.append(":");

        temp = new StringBuilder();
        temp.append(timeDate.minuteOfHour().getAsText(Locale.getDefault()));
        if (temp.length() < 2) {
            while (temp.length() < 2)
                temp.insert(0, "0");
        }
        sb.append(temp + " ");

        sb.append(timeDate.getZone().toTimeZone().getDisplayName());

        return sb.toString();
    }

    private String findTimeDifference(DateTime pic) {
        DateTime now = new DateTime(System.currentTimeMillis());
        Period diff = new Period(pic, now);
        PeriodFormatter toastFormatter = new PeriodFormatterBuilder().appendDays().appendSuffix(" day", " days").appendSeparator(" and ").printZeroAlways().appendHours().appendSuffix(" hour", " hours").appendLiteral(" ago.").toFormatter();
        return toastFormatter.print(diff);
    }
}