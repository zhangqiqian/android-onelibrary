package org.onelibrary;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

    public final static String APP_STATUS = "app_status";
    public final static String STATUS_DATA_UPDATE = "data_update";
    public final static String STATUS_NOTIFICATION = "is_notified";

    private SharedPreferences pref;

    int AUTO_REFRESH_INTERVAL = 10 * 1000; //10 seconds.
    private Handler mHandler = new Handler();

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (pref == null){
                pref = getSharedPreferences(APP_STATUS, 0);
            }
            Boolean isUpdated = pref.getBoolean(STATUS_DATA_UPDATE, false);

            Log.d(TAG, "---- Timer: updated? " + isUpdated);
            if(isUpdated){
                refreshListView();
                pref.edit().putBoolean(STATUS_DATA_UPDATE, false).apply();
                Log.d(TAG, "---- refresh updated");
            }
            mHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences(APP_STATUS, 0);

        Log.d(TAG, "-------------- onCreate ------------");
        mHandler.postDelayed(updateTimerThread, 0);

        LocationService locationService = new LocationService(MainActivity.this);
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
            Log.d(TAG, "Login status: " + isLogin + ", interval: " + interval);
            if (!isLogin || interval > 600){
                String username = preferences.getString(USERNAME, "");
                String password = preferences.getString(PASSWORD, "");
                if(username.isEmpty() || password.isEmpty()){
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    //logout
                    startActivity(intent);
                } else {
                    //auto login
                    Bundle params = new Bundle();
                    params.putString("username", username);
                    params.putString("password", password);
                    new LoginTask().execute(params);
                }
            }
        }else{
            Toast.makeText(MainActivity.this, R.string.network_disconnected, Toast.LENGTH_SHORT).show();
        }

        if (savedInstanceState == null) {
            refreshListView();
        }

    }

    @Override
    public void onResume(){
        Log.d(TAG, "---- onResume ---");
        refreshListView();

        if (pref == null){
            pref = getSharedPreferences(APP_STATUS, 0);
        }
        pref.edit().putBoolean(STATUS_NOTIFICATION, false).apply();
        Log.d(TAG, "---- set notification to false ---");

        super.onResume();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "---- onStop ---");
        if (pref == null){
            pref = getSharedPreferences(APP_STATUS, 0);
        }
        pref.edit().putBoolean(STATUS_NOTIFICATION, true).apply();
        Log.d(TAG, "---- set notification to true ---");

        super.onStop();
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
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    private void refreshListView(){
        Log.d(TAG, "---- refresh List View ---");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SwipeRefreshListFragmentFragment fragment = new SwipeRefreshListFragmentFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commitAllowingStateLoss();
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
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }
            return is_ok;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "Auto login result: " + result);
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

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
