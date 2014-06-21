package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter;

import android.content.Context;
import android.view.ViewGroup;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.octo.android.robospice.request.simple.IBitmapRequest;
import com.octo.android.robospice.spicelist.SpiceListItemView;
import com.octo.android.robospice.spicelist.simple.BitmapSpiceManager;
import com.octo.android.robospice.spicelist.simple.SpiceArrayAdapter;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.R;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Entry;

import java.io.File;
import java.util.ArrayList;

/**
 * ArrayAdapter for API v2 thumbnail view.
 * Uses Robospice for networking and caching.
 * Created by Bobby on 6/16/2014.
 */
public class EntriesAdapter extends SpiceArrayAdapter<Entry> {
    public EntriesAdapter(Context context, BitmapSpiceManager spiceManagerBinary, ArrayList<Entry> entries) {
        super(context, spiceManagerBinary, entries);
    }

    //TODO Make new thumbnail layout
    @Override
    public SpiceListItemView<Entry> createView(Context context, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public IBitmapRequest createRequest(Entry entry, int i, int i2, int i3) {
        return new BitmapRequest(entry.preview_url, new File(getContext().getCacheDir().getAbsolutePath() + getContext().getString(R.string.image_cache)));
    }
}
