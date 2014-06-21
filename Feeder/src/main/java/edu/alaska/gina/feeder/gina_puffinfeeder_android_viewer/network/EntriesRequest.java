package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import android.util.Log;
import android.widget.Toast;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Entry;
import org.springframework.web.client.RestTemplate;

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
        //return getRestTemplate().getForObject(this.endpoint + "?page=" + "1", Entry[].class);
        RestTemplate r = getRestTemplate();
        if (r == null)
            Log.d("feeder-debug", "RestTemplate is null!");
        return r.getForObject(this.endpoint, Entry[].class);
    }
}
