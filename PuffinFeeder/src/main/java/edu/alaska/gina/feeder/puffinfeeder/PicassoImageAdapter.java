package edu.alaska.gina.feeder.puffinfeeder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter that puts feeder thumbnails into the primar
 * Created by bobby on 6/19/13.
 */
public class PicassoImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<FeedImage> mFeedImages;

    public PicassoImageAdapter(Context c, ArrayList<FeedImage> feedImages) {
        this.mContext = c;
        this.mFeedImages = feedImages;
    }

    @Override
    public int getCount() {
        return mFeedImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mFeedImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setPadding(0,0,0,0);
        imageView.setLayoutParams(new GridView.LayoutParams(350, 350));

        Picasso.with(mContext).load(mFeedImages.get(position).getThumbnail()).resize(350, 350).centerCrop().into(imageView);

        return imageView;
    }
}
