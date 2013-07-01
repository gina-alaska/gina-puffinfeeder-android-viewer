package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

import java.util.ArrayList;

/**
 * Created by bobby on 7/1/13.
 */
public class ImageViewFrameActivty extends SherlockActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_frame);

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle args = getIntent().getExtras();
    }

    public ArrayList<String> decodeBundle(Bundle encoded) {
        ArrayList<String> decoded = new ArrayList<String>();

        for (int i = 0; encoded.getString("image_url_" + i) != null; i++)
            decoded.add(encoded.getString("image_url_" + i));

        return decoded;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
        super.onBackPressed();
    }
}