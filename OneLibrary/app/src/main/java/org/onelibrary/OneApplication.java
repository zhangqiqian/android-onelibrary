package org.onelibrary;

import android.app.Application;
import android.util.Log;

import org.onelibrary.data.DbAdapter;

import java.lang.annotation.Target;

/**
 * Created by niko on 6/11/15.
 */
public class OneApplication extends Application {

    public static final String TAG = "OneApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        DbAdapter.getInstance(this);

        //register Location Updates
        Log.d(TAG, "---- register location updates ----");
        LocationService locationService = new LocationService(this);
        locationService.registerLocationUpdates();

        //set scheduling alarm
        Log.d(TAG, "---- set scheduling alarm ----");
        AlarmReceiver alarm = new AlarmReceiver();
        alarm.setAlarm(this);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
