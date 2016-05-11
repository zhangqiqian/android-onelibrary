package org.onelibrary;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";

    private static long back_pressed;

    public final static String SESSION_INFO = "session_info";
    public final static String IS_LOGIN = "is_login";
    public final static String UID = "uid";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";
    public final static String LAST_LOGIN = "last_login_time";

    public final static String APP_STATUS = "app_status";
    public final static String STATUS_DATA_UPDATE = "data_update";
    public final static String IS_PROFILE_UPDATED = "is_profile_updated";

    private SharedPreferences pref;
    private SharedPreferences settings;
    private SharedPreferences session;
    private ConnectivityManager cm;

    private MessageDataManager messageManager = null;

    DownloadManager downManager ;
    private DownLoadCompleteReceiver receiver;

    private String domain;
    int AUTO_REFRESH_INTERVAL = 30 * 1000; //20 seconds.
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
        Log.d(TAG, "-------------- onCreate ------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        pref = getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);
        session = getSharedPreferences(SESSION_INFO, 0);

        mHandler.postDelayed(updateTimerThread, AUTO_REFRESH_INTERVAL);

        LocationService locationService = new LocationService(MainActivity.this);
        if(!locationService.canGetLocation()){
            locationService.showSettingsAlert();
        }

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        autoLogin();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        domain = settings.getString("server_address", "http://115.28.223.203:8080");

        Bundle params = new Bundle();
        new checkUpdateTask().execute(params);

        messageManager = new MessageDataManager(getBaseContext(), domain);

        if (savedInstanceState == null) {
            refreshListView();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        receiver = new DownLoadCompleteReceiver();
        registerReceiver(receiver, filter);

        Boolean isProfileUpdated = pref.getBoolean(IS_PROFILE_UPDATED, false);
        if(!isProfileUpdated){
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        }

    }

    @Override
    public void onResume(){
        Log.d(TAG, "---- onResume ---");
        Boolean isUpdated = pref.getBoolean(STATUS_DATA_UPDATE, false);
        if(isUpdated){
            refreshListView();
            pref.edit().putBoolean(STATUS_DATA_UPDATE, true).apply();
        }

        //cancel new notification
        /*NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        Log.d(TAG, "---- cancel new notification ----");*/

        autoLogin();

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        /*利用反射机制调用MenuBuilder的setOptionalIconsVisible方法设置mOptionalIconsVisible为true，
        * 给菜单设置图标时才可见
        */
        setIconEnable(menu, true);

        MenuItem item0 = menu.add(0, 1, 0, R.string.profile);
        item0.setIcon(android.R.drawable.ic_menu_info_details);
        item0.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem item1 = menu.add(0, 1, 0, R.string.action_location);
        item1.setIcon(android.R.drawable.ic_menu_mylocation);
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem item2 = menu.add(0, 1, 0, R.string.action_settings);
        item2.setIcon(android.R.drawable.ic_menu_preferences);
        item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem item4 = menu.add(0, 1, 0, R.string.menu_clear);
        item4.setIcon(android.R.drawable.ic_menu_delete);
        item4.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                messageManager.clearMessages();
                refreshListView();
                return false;
            }
        });

        MenuItem item3 = menu.add(0, 1, 0, R.string.action_logout);
        item3.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        item3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showLogoutAlert();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return super.onPrepareOptionsMenu(menu);
    }

    //enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效
    private void setIconEnable(Menu menu, boolean enable)
    {
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);

            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    private void refreshListView(){
        Log.d(TAG, "---- refresh List View ---");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SwipeRefreshListFragmentFragment fragment = new SwipeRefreshListFragmentFragment();
        transaction.replace(R.id.content_fragment, fragment);
        transaction.commitAllowingStateLoss();
    }


    private void autoLogin(){
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            Boolean isLogin = session.getBoolean(IS_LOGIN, false);
            long last_login_time = session.getLong(LAST_LOGIN, 0);
            long now = System.currentTimeMillis()/1000;
            long interval = now - last_login_time;
            Log.d(TAG, "Login status: " + isLogin + ", interval: " + interval);
            if (!isLogin || interval > 1800){
                String username = session.getString(USERNAME, null);
                String password = session.getString(PASSWORD, null);
                if(username == null || username.isEmpty() || password == null || password.isEmpty()){
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
                String domain = settings.getString("server_address", "http://115.28.223.203:8080");
                Log.d(TAG, "---- server domain settings: " + domain + " ----");

                NetworkAdapter adapter = new NetworkAdapter(getBaseContext());
                JSONObject result = adapter.request(domain + getString(R.string.login_url), params[0]);
                SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);

                if(result.getInt("errno") == 0){
                    is_ok = true;
                    long uid = result.getInt("uid");
                    long now = System.currentTimeMillis()/1000;
                    session.edit().putString(USERNAME, params[0].getString(USERNAME)).putString(PASSWORD, params[0].getString(PASSWORD)).putBoolean(IS_LOGIN, true).putLong(LAST_LOGIN, now).putLong(UID, uid).apply();
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
                //Toast.makeText(getBaseContext(), R.string.login_failure, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
        }else{
            Toast.makeText(getBaseContext(), R.string.backpressed_tip, Toast.LENGTH_LONG).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(receiver);
    }

    private void showLogoutAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);

        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        // Setting Dialog Message
        alertDialog.setMessage(R.string.logout_alert_tip);

        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.action_logout, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                session.edit().putBoolean(IS_LOGIN, false).putString(PASSWORD, null).putString(NetworkAdapter.PHPSESSID, null).putLong(LAST_LOGIN, 0).apply();
                pref.edit().putBoolean(STATUS_DATA_UPDATE, false).apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void showUpdateDialog(final String name, final String downloadUrl, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_DARK);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(getString(R.string.update_alert_title));
        builder.setMessage(description);
        builder.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                //设置在什么网络情况下进行下载
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                //设置通知栏标题
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                request.setTitle(name);
                request.setDescription(getString(R.string.update_source));
                request.setAllowedOverRoaming(false);
                //设置文件存放目录
                request.setDestinationInExternalFilesDir(getBaseContext(), Environment.DIRECTORY_DOWNLOADS, name);

                downManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                long id = downManager.enqueue(request);
            }
        });
        builder.setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class checkUpdateTask extends AsyncTask<Bundle, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Bundle...params) {
            JSONObject versionInfo = new JSONObject();
            try {
                String updateUrl = domain + getString(R.string.update_url);
                NetworkAdapter adapter = new NetworkAdapter(getBaseContext());

                JSONObject result = adapter.request(updateUrl, params[0]);

                if(result != null && result.getInt("errno") == 0) {
                    Log.d("Update", "Success to get update info. result: " + result.toString());
                    versionInfo = result.getJSONObject("result");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return versionInfo;
        }

        @Override
        protected void onPostExecute(JSONObject info) {
            try {
                String description = info.getString("description");
                String new_version = info.getString("version");
                String downloadUrl = info.getString("url");
                String name = info.getString("name");

                try{
                    PackageManager packageManager = getBaseContext().getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageInfo(getBaseContext().getPackageName(), 0);
                    String version = packageInfo.versionName;
                    if (!new_version.equals(version)) {
                        showUpdateDialog(name, downloadUrl, description);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e){
                e.printStackTrace();
            }

        }
    }

    private class DownLoadCompleteReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Intent installIntent = new Intent();
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.setAction(Intent.ACTION_VIEW);
                installIntent.setDataAndType(downManager.getUriForDownloadedFile(id),
                        "application/vnd.android.package-archive");
                startActivity(installIntent);
            }
        }
    }

}
