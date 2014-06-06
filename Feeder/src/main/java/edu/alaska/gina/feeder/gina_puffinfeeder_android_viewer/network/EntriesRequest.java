package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Entry;

/**
 * Created by Bobby on 6/6/2014.
 */
public class EntriesRequest extends SpringAndroidSpiceRequest<Entry[]> {
    private final String endpoint;

    public EntriesRequest(String endpoint) {
        super(Entry[].class);
        this.endpoint = endpoint;
    }

    @Override
    public Entry[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(this.endpoint + "?page=" + "1", Entry[].class);
    }
}
