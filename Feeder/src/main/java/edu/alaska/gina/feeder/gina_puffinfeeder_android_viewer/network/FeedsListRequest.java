package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Category;

/**
 * Created by Bobby on 6/6/2014.
 */
public class FeedsListRequest extends SpringAndroidSpiceRequest<Category> {
    private final String endpoint;

    public FeedsListRequest(String endpoint) {
        super(Category.class);
        this.endpoint = endpoint;
    }

    @Override
    public Category loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(endpoint, Category.class);
    }
}
