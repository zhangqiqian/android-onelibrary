package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.ui.processbutton.ProgressGenerator;
import org.onelibrary.ui.processbutton.iml.ActionProcessButton;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.lang.reflect.Method;

public class LoginActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public static final String TAG = "LoginActivity";

    public final static String SESSION_INFO = "session_info";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";
    public final static String IS_LOGIN = "is_login";
    public final static String LAST_LOGIN = "last_login_time";

    private SharedPreferences settings;
    private ConnectivityManager cm;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
        String username = session.getString(USERNAME, null);

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);

        //assert if network is ok
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(LoginActivity.this, R.string.network_disconnected, Toast.LENGTH_SHORT).show();
        }

        if (username != null && username.length() > 0){
            editEmail.setText(username);
        }
        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        final ActionProcessButton btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
                progressGenerator.start(btnSignIn);
                btnSignIn.setEnabled(false);
                editEmail.setEnabled(false);
                editPassword.setEnabled(false);

                Bundle params = new Bundle();
                params.putString("username", editEmail.getText().toString());
                params.putString("password", editPassword.getText().toString());
                new LoginTask().execute(params); //Login
            }
        });

        Button regButton = (Button)findViewById(R.id.regBtn);
        regButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    public void onComplete() {
        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);
        final ActionProcessButton btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);

        btnSignIn.setEnabled(true);
        editEmail.setEnabled(true);
        editPassword.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_login, menu);
        //return true;
        setIconEnable(menu, true);

        MenuItem item1 = menu.add(0, 1, 0, R.string.action_settings);
        item1.setIcon(android.R.drawable.ic_menu_preferences);
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem item2 = menu.add(0, 1, 0, R.string.action_logout);
        item2.setIcon(android.R.drawable.ic_menu_preferences);
        item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，it is RESULT_OK
            case RESULT_OK:
                Toast.makeText(LoginActivity.this, R.string.signup_success, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
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
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()){
                    String domain = settings.getString("server_address", "http://115.28.223.203:8080");
                    Log.d(TAG, "---- server domain settings: " + domain + " ----");

                    NetworkAdapter adapter = new NetworkAdapter(getBaseContext());
                    JSONObject result = adapter.request(domain + getString(R.string.login_url), params[0]);
                    SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);

                    if(result != null && result.getInt("errno") == 0){
                        is_ok = true;
                        long now = System.currentTimeMillis()/1000;
                        session.edit().putString(USERNAME, params[0].getString(USERNAME)).putString(PASSWORD, params[0].getString(PASSWORD)).putBoolean(IS_LOGIN, true).putLong(LAST_LOGIN, now).apply();
                    }else{
                        session.edit().putString(USERNAME, params[0].getString(USERNAME)).putBoolean(IS_LOGIN, false).apply();
                    }
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
            Log.d(TAG, "Login result: " + result);
            if(result){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(LoginActivity.this, R.string.login_failure, Toast.LENGTH_LONG).show();
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

}
