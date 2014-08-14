package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

import roboguice.util.temp.Ln;

/**
 * A (slightly) custom implementation of SpiceManager that outputs less data to the Log than the default.
 * Created by Bobby on 8/14/2014.
 */
public class FeederSpiceManager extends SpiceManager {
    /**
     * Creates a {@link com.octo.android.robospice.SpiceManager}. Typically this occurs in the construction
     * of an Activity or Fragment. This method will check if the service to bind
     * to has been properly declared in AndroidManifest.
     *
     * @param spiceServiceClass the service class to bind to.
     */
    public FeederSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
        super(spiceServiceClass);
        Ln.getConfig().setLoggingLevel(Log.ERROR);
    }
}
