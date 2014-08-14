package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.alaska.gina.feeder.android.core.data.Feed;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.R;

/**
 * List Adapter for Feeds Array
 * Created by Bobby on 6/26/2014.
 */
public class FeedsAdapter extends BaseAdapter {
    private final ArrayList<Feed> feeds;
    private final LayoutInflater inflater;

    public FeedsAdapter(Context context, ArrayList<Feed> feeds) {
        this.feeds = feeds;
        this.inflater = LayoutInflater.from(context);
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
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.feed_item_layout, null);
            holder.videoIndicator = (ImageView) convertView.findViewById(R.id.videoIndicator);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.feedTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.feeds.get(position).category != null && (this.feeds.get(position).category.equalsIgnoreCase("movie") || this.feeds.get(position).category.equalsIgnoreCase("movies")))
            holder.videoIndicator.setVisibility(View.VISIBLE);
        else
            holder.videoIndicator.setVisibility(View.INVISIBLE);

        holder.feedTitle.setText(feeds.get(position).title);
        return convertView;
    }

    private class ViewHolder {
        TextView feedTitle;
        ImageView videoIndicator;
    }
}
