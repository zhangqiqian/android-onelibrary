package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.ui.processbutton.ProgressGenerator;
import org.onelibrary.ui.processbutton.iml.ActionProcessButton;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;

public class RegisterActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public final static String TAG = "RegisterActivity";

    private ConnectivityManager cm;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);
        final EditText confirmPassword = (EditText) findViewById(R.id.editConfirmPassword);

        //assert if network is ok
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(RegisterActivity.this, R.string.network_disconnected, Toast.LENGTH_SHORT).show();
        }

        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        final ActionProcessButton btnSignUp = (ActionProcessButton) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSignUp.setMode(ActionProcessButton.Mode.ENDLESS);
                progressGenerator.start(btnSignUp);
                btnSignUp.setEnabled(false);
                editEmail.setEnabled(false);
                editPassword.setEnabled(false);
                confirmPassword.setEnabled(false);

                Bundle params = new Bundle();
                params.putString("username", editEmail.getText().toString());
                params.putString("password", editPassword.getText().toString());
                params.putString("repassword", confirmPassword.getText().toString());
                new SignupTask().execute(params); //sign up
            }
        });
    }

    @Override
    public void onComplete() {
        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);
        final EditText confirmPassword = (EditText) findViewById(R.id.editConfirmPassword);
        final ActionProcessButton btnSignUp = (ActionProcessButton) findViewById(R.id.btnSignUp);

        btnSignUp.setEnabled(true);
        editEmail.setEnabled(true);
        editPassword.setEnabled(true);
        confirmPassword.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_register, menu);
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

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class SignupTask extends AsyncTask<Bundle, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Bundle...params) {
            JSONObject result = null;
            try {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()){
                    String domain = settings.getString("server_address", "http://192.168.1.105");
                    NetworkAdapter adapter = new NetworkAdapter(getBaseContext());
                    result = adapter.request(domain + getString(R.string.signup_url), params[0]);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "Sign up result: " + result.toString());
            try {
                if(result != null && result.getInt("errno") == 0){
                    setResult(RESULT_OK);
                    finish();
                }else{
                    Toast.makeText(RegisterActivity.this, getString(R.string.signup_failure)+" "+result.getString("errmsg"), Toast.LENGTH_LONG).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }


        }
    }
}
