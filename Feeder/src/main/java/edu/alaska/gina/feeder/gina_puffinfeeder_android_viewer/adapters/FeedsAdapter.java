package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapters;

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
            //convertView = this.inflater.inflate(R.layout.feed_item_layout, null);
            convertView = this.inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
/*
        if (feeds.get(position).preview_url != null) {
            Picasso.with(this.context)
                    .load(feeds.get(position).preview_url + "?size=50x50")
                    .placeholder(R.drawable.image_placeholder)
                    .into((ImageView) convertView.findViewById(R.id.feedPreviewImage));
        }

        ((TextView) convertView.findViewById(R.id.feedTitle)).setText(feeds.get(position).title);
*/
        ((TextView) convertView).setText(feeds.get(position).title);
        return convertView;
    }

    private class ViewHolder {
        //For use only if list gets more detailed.
    }
}
