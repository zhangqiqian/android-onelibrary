package org.onelibrary;

import android.app.Application;
import android.util.Log;

import java.lang.annotation.Target;

/**
 * Created by niko on 6/11/15.
 */
public class OneApplication extends Application {

    public static final String TAG = "OneApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        //register Location Updates
        Log.d(TAG, "---- register location updates ----");
        LocationService locationService = new LocationService(this);
        if(!locationService.canGetLocation()){
            locationService.showSettingsAlert();
        }
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
