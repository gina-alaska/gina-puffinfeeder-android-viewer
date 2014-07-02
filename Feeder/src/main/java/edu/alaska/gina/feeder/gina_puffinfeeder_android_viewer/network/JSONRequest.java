package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * A class that handles all JSON requests generically.
 * Created by Bobby on 6/26/2014.
 */
public class JSONRequest<T> extends SpringAndroidSpiceRequest<T> {
    private final String url;
    private final Class<T> clazz;

    public JSONRequest(Class<T> clazz, String url) {
        super(clazz);
        this.clazz = clazz;
        this.url = url;
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(this.url, this.clazz);
    }
}
