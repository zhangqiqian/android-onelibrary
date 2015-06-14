package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.onelibrary.ui.processbutton.ProgressGenerator;
import org.onelibrary.ui.processbutton.iml.ActionProcessButton;

public class RegisterActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public final static String SESSION_INFO = "session_info";
    public final static String USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);

        //assert if network is ok
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(RegisterActivity.this, "Unconnected to network.", Toast.LENGTH_SHORT).show();
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

                //TODO sign up a user
                SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
                session.edit().putString(USERNAME, editEmail.getText().toString()).apply();
            }
        });
    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "Success to sign in system", Toast.LENGTH_LONG).show();
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
}
