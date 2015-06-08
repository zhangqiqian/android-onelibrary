package org.onelibrary;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";

    private static long back_pressed;

    public final static String SESSION_INFO = "session_info";
    public final static String IS_LOGIN = "is_login";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";
    public final static String LAST_LOGIN = "last_login_time";

    AlarmReceiver alarm = new AlarmReceiver();

    LocationService locationService;
    /*private int interval = 5000; //5 seconds.
    private Handler mHandler;

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            // create class object
            locationService = new LocationService(getBaseContext());
            // check if GPS enabled
            if(locationService.canGetLocation()){
                double latitude = locationService.getLatitude();
                double longitude = locationService.getLongitude();
                // \n is for new line
                Toast.makeText(getApplicationContext(), "Your Location is \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            }else{
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                locationService.showSettingsAlert();
            }
            //mHandler.postDelayed(this, interval);
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*mHandler = new Handler();
        mHandler.postDelayed(updateTimerThread, 0);*/

        // create location object
        locationService = new LocationService(MainActivity.this);
        // check if location enabled
        if(!locationService.canGetLocation()){
            locationService.showSettingsAlert();
        }

        //assert if network is ok
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            SharedPreferences preferences = getSharedPreferences(SESSION_INFO, 0);
            Boolean isLogin = preferences.getBoolean(IS_LOGIN, false);
            long last_login_time = preferences.getLong(LAST_LOGIN, 0);
            long now = System.currentTimeMillis()/1000;
            long interval = now - last_login_time;
            if (!isLogin || interval > 300){
                //auto login
                String username = preferences.getString(USERNAME, "");
                String password = preferences.getString(PASSWORD, "");
                if(!(username.isEmpty() || password.isEmpty())){
                    Bundle params = new Bundle();
                    params.putString("username", username);
                    params.putString("password", password);
                    new LoginTask().execute(params);
                }
                alarm.setAlarm(this);
            }
        }else{
            Toast.makeText(MainActivity.this, "Network disconnect.", Toast.LENGTH_SHORT).show();
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SwipeRefreshListFragmentFragment fragment = new SwipeRefreshListFragmentFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_location) {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_logout) {
            SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
            Boolean isLogin = session.getBoolean(IS_LOGIN, false);
            if (isLogin){
                session.edit().putBoolean(IS_LOGIN, false).remove(PASSWORD).apply();
            }
            Intent intent = new Intent(MainActivity.this, IndexActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoginTask extends AsyncTask<Bundle, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Bundle...params) {
            boolean is_ok = false;
            try {
                NetworkAdapter adapter = new NetworkAdapter(getBaseContext());
                JSONObject result = adapter.request(getString(R.string.login_url), params[0]);
                SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);

                if(result.getInt("errno") == 0){
                    is_ok = true;
                    long now = System.currentTimeMillis()/1000;
                    session.edit().putString(USERNAME, params[0].getString(USERNAME)).putString(PASSWORD, params[0].getString(PASSWORD)).putBoolean(IS_LOGIN, true).putLong(LAST_LOGIN, now).apply();
                }else{
                    session.edit().putString(USERNAME, params[0].getString(USERNAME)).putBoolean(IS_LOGIN, false).apply();
                }
                return is_ok;
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }
            return is_ok;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i(TAG, "Auto login result: " + result);
            if(!result){
                Toast.makeText(getBaseContext(), "Failure to login.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
        }else{
            Toast.makeText(getBaseContext(), "Press once again to exit.", Toast.LENGTH_LONG).show();
        }
        back_pressed = System.currentTimeMillis();
    }
}
