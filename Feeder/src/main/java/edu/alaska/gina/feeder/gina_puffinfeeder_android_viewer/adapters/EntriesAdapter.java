package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;

import edu.alaska.gina.feeder.android.core.data.Entry;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.R;

/**
 * Adapter that places thumbnails off the feed into the primary GridView.
 * Created by bobby on 6/19/13.
 */
public class EntriesAdapter extends BaseAdapter {
    private final Context mContext;
    private final ArrayList<Entry> entries;
    private final LayoutInflater inflater;

    public EntriesAdapter(Context c, ArrayList<Entry> entries) {
        this.mContext = c;
        this.entries = entries;
        this.inflater = LayoutInflater.from(mContext);
    }

    private String formatDateTime(DateTime img) {
        Period diff = new Period(img, new DateTime(System.currentTimeMillis()));
        PeriodFormatter formatter;

        if (diff.getYears() > 0) {
            formatter = new PeriodFormatterBuilder().appendYears().appendSuffix(" year ago.", " years ago.").toFormatter();
        } else if (diff.getMonths() > 0) {
            formatter = new PeriodFormatterBuilder().appendMonths().appendSuffix(" month ago.", " months ago.").toFormatter();
        } else if (diff.getWeeks() > 0) {
            formatter = new PeriodFormatterBuilder().appendWeeks().appendSuffix(" week ago.", " weeks ago.").toFormatter();
        } else if (diff.getDays() > 0) {
            formatter = new PeriodFormatterBuilder().appendDays().appendSuffix(" day ago.", " days ago.").toFormatter();
        } else if (diff.getHours() > 0) {
            formatter = new PeriodFormatterBuilder().appendHours().appendSuffix(" hour ago.", " hours ago.").toFormatter();
        } else if (diff.getMinutes() > 0) {
            formatter = new PeriodFormatterBuilder().appendMinutes().appendSuffix(" minute ago.", " minutes ago.").toFormatter();
        } else {
            formatter = new PeriodFormatterBuilder().appendSeconds().appendSuffix(" second ago.", " seconds ago.").toFormatter();
        }

        return formatter.print(diff);
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.entry_item_layout, null);

            holder = new ViewHolder();
            holder.timestamp = (TextView) convertView.findViewById(R.id.entry_caption);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.entryImage);
            holder.star = (ImageView) convertView.findViewById(R.id.star);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        int h = Math.round(d.widthPixels / ((GridView) parent).getNumColumns());

        holder.timestamp.setText(formatDateTime(this.entries.get(position).event_at));

        if (this.entries.get(position).highlighted)
            holder.star.setVisibility(View.VISIBLE);

        Picasso.with(mContext)
                .load(entries.get(position).preview_url + "?size=" + h + "x" + (h + 50))
                .placeholder(R.drawable.image_placeholder)
                .resize(h, h)
                .centerCrop()
                .into(holder.thumbnail);

        convertView.setMinimumHeight(h);

        return convertView;
    }

    private static class ViewHolder {
        TextView timestamp;
        ImageView thumbnail;
        ImageView star;
    }
}
