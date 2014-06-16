package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import com.octo.android.robospice.request.simple.IBitmapRequest;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.octo.android.robospice.spicelist.SpiceListItemView;
import com.octo.android.robospice.spicelist.simple.BitmapSpiceManager;
import com.octo.android.robospice.spicelist.simple.SpiceArrayAdapter;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Entry;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.ThumbsRequest;

/**
 * Created by Bobby on 6/16/2014.
 */
public class EntriesAdapter extends SpiceArrayAdapter<Entry> {
    public EntriesAdapter(Context context, BitmapSpiceManager spiceManagerBinary, Entry.List entries) {
        super(context, spiceManagerBinary, entries.getEntries());
    }

    @Override
    public SpiceListItemView<Entry> createView(Context context, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public IBitmapRequest createRequest(Entry entry, int i, int i2, int i3) {
        return new ThumbsRequest(entry.preview_url);
    }
}
