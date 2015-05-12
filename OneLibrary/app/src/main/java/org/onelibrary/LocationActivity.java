package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class LocationActivity extends Activity {

    private String TAG = "Location Demo";

    private TextView longitude;
    private TextView latitude;

    private final LocationListener locationListener = new LocationListener() {
        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        public void onLocationChanged(Location location) {
            // log it when the location changes
            if (location != null) {
                Log.i(TAG, "Location changed : Latitude: "
                        + location.getLatitude() + " Longitude: "
                        + location.getLongitude());
                longitude = (TextView)findViewById(R.id.longitude);
                latitude = (TextView)findViewById(R.id.latitude);

                //Location location = getLocation(this);
                longitude.setText("Longitude: "+location.getLongitude());
                latitude.setText("Latitude: "+location.getLatitude());
            }
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        public void onProviderDisabled(String provider) {
            Toast.makeText(getBaseContext(), provider+" is disabled.", Toast.LENGTH_LONG).show();
        }

        //  Provider被enable时触发此函数，比如GPS被打开
        public void onProviderEnabled(String provider) {
            Toast.makeText(getBaseContext(), provider + " is enabled.", Toast.LENGTH_LONG).show();
        }

        //Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    Button btnShowLocation;
    LocationService gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //获取GPS支持
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000 * 60, 50, locationListener);
        }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            //获取NETWORK支持
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000 * 60, 50, locationListener);
        }else{
            Toast.makeText(getBaseContext(), "Location is disabled.", Toast.LENGTH_LONG).show();
        }

        btnShowLocation = (Button) findViewById(R.id.locationBtn);

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new LocationService(LocationActivity.this);
                // check if GPS enabled
                if(gps.canGetLocation()){

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("static-access")
    private Location getLocation(Context context) {
        //You do not instantiate this class directly;
        //instead, retrieve it through:
        //Context.getSystemService(Context.LOCATION_SERVICE).
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        //获取GPS支持
        Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        if (location == null) {
            //获取NETWORK支持
            location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        }
        return location;
    }

}
