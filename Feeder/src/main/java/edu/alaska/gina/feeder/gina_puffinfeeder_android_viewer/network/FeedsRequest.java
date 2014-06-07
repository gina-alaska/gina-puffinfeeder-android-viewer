package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Feed;

/**
 * Created by Bobby on 6/6/2014.
 */
public class FeedsRequest extends SpringAndroidSpiceRequest<Feed[]> {
    private final String endpoint;

    public FeedsRequest(String endpoint) {
        super(Feed[].class);
        this.endpoint = endpoint;
    }

    @Override
    public Feed[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(endpoint, Feed[].class);
    }
}
