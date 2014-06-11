package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Feed;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.FeedsList;

/**
 * Created by Bobby on 6/6/2014.
 */
public class FeedsListRequest extends SpringAndroidSpiceRequest<FeedsList> {
    private final String endpoint;

    public FeedsListRequest(String endpoint) {
        super(FeedsList.class);
        this.endpoint = endpoint;
    }

    @Override
    public FeedsList loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(endpoint, FeedsList.class);
    }
}
