package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Feed;

import java.util.ArrayList;

/**
 * List Adapter for Feeds Array
 * Created by Bobby on 6/26/2014.
 */
public class FeedsAdapter extends BaseAdapter {
    private final Context mContext;
    private final ArrayList<Feed> feeds;

    public FeedsAdapter(Context mContext, ArrayList<Feed> feeds) {
        this.mContext = mContext;
        this.feeds = feeds;
    }

    @Override
    public int getCount() {
        return feeds.size();
    }

    @Override
    public Object getItem(int position) {
        return feeds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = new TextView(mContext);

        ((TextView) convertView).setText(feeds.get(position).title);

        return convertView;
    }
}
