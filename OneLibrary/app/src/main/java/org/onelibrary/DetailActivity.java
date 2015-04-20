package org.onelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
        Intent intent = getIntent();

        Bundle item = intent.getBundleExtra("message_item");
        //title
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(item.getCharSequence("title"));
        //publish date
        TextView categoryView = (TextView) findViewById(R.id.category);
        categoryView.setText("["+item.getCharSequence("category")+"]");
        //publish date
        TextView pubdateView = (TextView) findViewById(R.id.pubdate);
        pubdateView.setText(item.getCharSequence("pubdate"));
        //content
        TextView contentView = (TextView) findViewById(R.id.content);
        contentView.setText("Source: " + item.getCharSequence("content"));
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
