package org.onelibrary;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;

import java.util.List;

public class LocationService extends Service implements LocationListener {

    public static final String TAG = "LocationService";

    public final static String APP_STATUS = "app_status";
    public final static String STATUS_DATA_UPDATE = "data_update";

    private Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    DbAdapter mDbAdapter;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private SharedPreferences preferences = null;
    private String domain = null;

    public LocationService() {
        this.mContext = getBaseContext();
    }

    public LocationService(Context context) {
        this.mContext = context;
        getLastLocation();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        preferences = mContext.getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "------ onCreate ------");
        mContext = getBaseContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "------ onStartCommand ------");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "------ onDestroy ------");
        super.onDestroy();
    }

    public void registerLocationUpdates() {
        try {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
            //两个参数,一个是key，就是在PreferenceActivity的xml中设置的,另一个是取不到值时的默认值
            String intervalTime = settings.getString("update_frequency", "1");
            int intervalMinutes = Integer.parseInt(intervalTime);
            if(intervalMinutes == -1){
                Log.d(TAG, "---- never register location updates ----");
                return;
            }
            long interval = intervalMinutes > 0 ? intervalMinutes * 60 * 1000 : MIN_TIME_BW_UPDATES;
            Log.d(TAG, "---- get update frequency: " + intervalMinutes + " minutes");

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            interval,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(TAG, "Register location updates, locationService is from network.");
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            interval,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(TAG, "Register location updates, GPS Enabled, and locationService is from gps.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getLastLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER, this, null);

                    Log.d(TAG, "Get last location, GPS Enabled, and locationService is from gps.");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            return location;
                        }
                    }
                }

                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestSingleUpdate(
                            LocationManager.NETWORK_PROVIDER,
                            this,
                            null);

                    Log.d(TAG, "Get last location, LocationService is from network.");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            return location;
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopLocationService(){
        if(locationManager != null){
            locationManager.removeUpdates(LocationService.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);

        // Setting Dialog Title
        //alertDialog.setTitle(R.string.location_alert_title);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.location_alert_content);

        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.location_alert_settings_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.location_alert_cancel_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "position has changed!");
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d(TAG, "a new position: " + longitude + ", " + latitude);

        new LoadMessagesTask().execute(longitude, latitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoadMessagesTask extends AsyncTask<Double, Void, List<MessageItem>> {

        @Override
        protected List<MessageItem> doInBackground(Double...params) {
            //get remote message, and save to db.
            if(domain == null){
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                domain = settings.getString("server_address", "http://115.28.223.203:8080");
            }
            MessageDataManager manager = new MessageDataManager(mContext, domain);
            return manager.getRemoteMessages(mContext, params[0], params[1], 1, 3, 1);
        }

        @Override
        protected void onPostExecute(List<MessageItem> result) {
            mDbAdapter = DbAdapter.getInstance(mContext);
            if(domain == null){
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                domain = settings.getString("server_address", "http://115.28.223.203:8080");
            }
            MessageDataManager manager = new MessageDataManager(mContext, domain);
            int size = result.size();
            for (MessageItem item : result){
                if(mDbAdapter.messageIsExist(item)){
                    size--;
                }else{
                    manager.addMessage(item);
                }
            }
            if(size > 0){
                if (preferences == null){
                    preferences = mContext.getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);
                }
                preferences.edit().putBoolean(STATUS_DATA_UPDATE, true).apply();
            }
        }
    }
}
