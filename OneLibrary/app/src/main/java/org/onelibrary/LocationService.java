package org.onelibrary;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.LocationDataManager;
import org.onelibrary.data.LocationEntry;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;

import java.util.Calendar;
import java.util.List;

public class LocationService extends Service implements LocationListener {

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
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

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
        Log.d("Location Service", "------ onCreate ------");
        mContext = getBaseContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("Location Service", "------ onStartCommand ------");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d("Location Service", "------ onDestroy ------");
        super.onDestroy();
    }

    public void registerLocationUpdates() {
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
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("LocationService", "LocationService is from network.");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("LocationService", "GPS Enabled, and locationService is from gps.");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
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
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    Log.d("LocationService", "LocationService is from network.");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        Log.d("LocationService", "GPS Enabled, and locationService is from gps.");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
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
    public void stopUsingGPS(){
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

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
        Log.d("Location Service", "position has changed!");
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d("Location Service", "a new position: " + longitude + ", " + latitude);

        mDbAdapter = new DbAdapter(mContext);
        LocationDataManager manager = new LocationDataManager(mDbAdapter);
        LocationEntry entry = new LocationEntry("Location", longitude, latitude, Calendar.getInstance());
        manager.addPoint(entry);

        /*MessageDataManager mssageManager = new MessageDataManager(mDbAdapter);
        List<MessageItem> messageItems = mssageManager.getRemoteMessages(getBaseContext(), longitude, latitude);

        int size = messageItems.size();
        for (MessageItem item : messageItems){
            if(mDbAdapter.messageIsExist(item)){
                size--;
            }else{
                mssageManager.addMessage(item);
            }
        }
        Toast.makeText(getBaseContext(), "Location has been changed, updated " + size + " message(s).", Toast.LENGTH_LONG).show();
        */
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
        Log.d("Location Service", "onBind");
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
            mDbAdapter = new DbAdapter(getBaseContext());
            MessageDataManager manager = new MessageDataManager(mDbAdapter);
            return manager.getRemoteMessages(getBaseContext(), params[0], params[1]);
        }

        @Override
        protected void onPostExecute(List<MessageItem> result) {
            mDbAdapter = new DbAdapter(getBaseContext());
            MessageDataManager manager = new MessageDataManager(mDbAdapter);
            int size = result.size();
            for (MessageItem item : result){
                if(mDbAdapter.messageIsExist(item)){
                    size--;
                }else{
                    manager.addMessage(item);
                }
            }
            Toast.makeText(getBaseContext(), "Location has been changed, updated " + size + " message(s).", Toast.LENGTH_LONG).show();
        }
    }
}
