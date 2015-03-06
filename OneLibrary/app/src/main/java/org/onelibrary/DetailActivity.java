package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //assert if network is ok
        /*ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(DetailActivity.this, "Unconnected to network.", Toast.LENGTH_SHORT).show();
        }*/

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        String content = null;
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("message_item");
            if (bundle == null) {
                content = "Failure to load.";
            }else {
                content = bundle.getString("title")+"\n\n"
                        +bundle.getString("pubdate")+"\n\n"
                        +bundle.getString("content").replace('\n', ' ')
                        +"\n\nLink：\n"
                        +bundle.getString("link");
            }
        } else {
            content = "Failure to load.";
        }

        TextView textView = (TextView) findViewById(R.id.content);
        textView.setText(content);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
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
