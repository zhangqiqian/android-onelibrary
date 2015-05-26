package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class LoginActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public static final String TAG = "LoginActivity";

    public final static String SESSION_INFO = "session_info";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";
    public final static String IS_LOGIN = "is_login";
    public final static String LAST_LOGIN = "last_login_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
        String username = session.getString(USERNAME, "");

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);

        //assert if network is ok
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(LoginActivity.this, "Network disconnect.", Toast.LENGTH_SHORT).show();
        }

        //TODO for test
        session.edit().putBoolean(IS_LOGIN, true).apply();

        if (username.length() > 0){
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
                startActivity(intent);
            }
        });
    }

    @Override
    public void onComplete() {
        //Toast.makeText(this, "Success to sign in.", Toast.LENGTH_LONG).show();
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

                session.edit().putString(USERNAME, params[0].getString(USERNAME)).putString(PASSWORD, params[0].getString(PASSWORD)).putBoolean(IS_LOGIN, true).apply();
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
            Log.i(TAG, "Login result: " + result);
            if(result){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(LoginActivity.this, "Failure to login.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
