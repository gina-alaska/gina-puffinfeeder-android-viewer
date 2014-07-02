package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.alaska.gina.feeder.android.core.data.Feed;

/**
 * List Adapter for Feeds Array
 * Created by Bobby on 6/26/2014.
 */
public class FeedsAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Feed> feeds;
    private final LayoutInflater inflater;

    public FeedsAdapter(Context context, ArrayList<Feed> feeds) {
        this.context = context;
        this.feeds = feeds;
        this.inflater = LayoutInflater.from(this.context);
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
        if (convertView == null) {
            convertView = this.inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        ((TextView) convertView).setText(feeds.get(position).title);
        //convertView.setPadding(16, 16, 16, 16);
        //((TextView) convertView).setGravity(android.R.attr.layout_alignLeft);

        return convertView;
    }
}
