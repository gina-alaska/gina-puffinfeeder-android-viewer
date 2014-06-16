package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.octo.android.robospice.request.simple.IBitmapRequest;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * SpiceRequest for preview thumbnails.
 * Created by Bobby on 6/16/2014.
 */
public class ThumbsRequest implements IBitmapRequest {
    private final String thumbUrl;

    public ThumbsRequest(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @Override
    public Bitmap loadDataFromNetwork() throws Exception {
        /*
        byte[] img = getRestTemplate().getForObject(thumbUrl, byte[].class);
        return img != null ? BitmapFactory.decodeStream(new ByteArrayInputStream(img)) : null;
        */
        return null;
    }

    @Override
    public File getCacheFile() {
        return null;
    }
}
