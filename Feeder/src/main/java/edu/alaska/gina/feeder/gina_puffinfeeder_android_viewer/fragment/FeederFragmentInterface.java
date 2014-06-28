package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragment;

import com.octo.android.robospice.request.listener.RequestListener;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JSONRequest;

/**
 * Interface for description changes & network requests from fragments.
 * Created by Bobby on 6/27/2014.
 */
public interface FeederFragmentInterface {
    public void networkRequest(JSONRequest request, String cacheKey, RequestListener listener);
    public void setDescription(String description);
}
