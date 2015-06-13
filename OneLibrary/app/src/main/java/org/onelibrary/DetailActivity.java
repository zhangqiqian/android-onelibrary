package org.onelibrary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;

public class DetailActivity extends Activity {

    private static final String LOG_TAG = "DetailActivity";

    private MessageDataManager manager;
    MessageItem message;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        Bundle item = intent.getBundleExtra("message");
        int id = item.getInt("id");
        int message_id = item.getInt("message_id");
        manager = new MessageDataManager(getBaseContext());
        message = manager.getMessage(id);

        if (message != null && (message.getContent().isEmpty() || message.getContent().contentEquals(""))){
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                progressDialog = ProgressDialog.show(DetailActivity.this, "", getString(R.string.loading), true, false);
                new LoadMessageTask().execute(id, message_id);
            }else{
                Toast.makeText(DetailActivity.this, R.string.network_disconnected, Toast.LENGTH_SHORT).show();
            }
        }

        message = manager.getMessage(id);
        renderDetail(message);

    }

    private void renderDetail(MessageItem item){
        //title
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(item.getTitle());

        //category
        if(!item.getCategory().isEmpty()){
            TextView categoryView = (TextView) findViewById(R.id.category);
            categoryView.setText("["+item.getCategory()+"]");
        }

        //publish date
        if(!item.getPubdate().isEmpty()) {
            TextView pubdateView = (TextView) findViewById(R.id.pubdate);
            pubdateView.setText(item.getPubdate());
        }

        //content
        TextView contentView = (TextView) findViewById(R.id.content);
        if(!item.getContent().isEmpty()){
            contentView.setText(item.getContent());
        }

        //Tags
        if(!item.getTags().isEmpty()) {
            TextView tagsView = (TextView) findViewById(R.id.tags);
            tagsView.setText("Tags: "+item.getTags());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoadMessageTask extends AsyncTask<Integer, Void, MessageItem> {

        @Override
        protected MessageItem doInBackground(Integer...params) {
            //get remote message, and save to db.
            manager = new MessageDataManager(getBaseContext());
            return manager.getMessageDetail(getBaseContext(), params[0], params[1]);
        }

        @Override
        protected void onPostExecute(MessageItem result) {
            if(result != null){
                Log.d(LOG_TAG, "AsyncTask result: " + result.toString());
                renderDetail(result);
                manager = new MessageDataManager(getBaseContext());
                result.setStatus(1);
                manager.updateMessage(result);
            }
            progressDialog.dismiss();
        }
    }
}
