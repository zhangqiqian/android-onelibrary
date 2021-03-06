package org.onelibrary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;

public class DetailActivity extends Activity {

    private static final String LOG_TAG = "DetailActivity";

    public final static String APP_STATUS = "app_status";
    public final static String STATUS_DATA_UPDATE = "data_update";

    private MessageDataManager manager;
    MessageItem message;
    private SharedPreferences prefStatus = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        prefStatus = getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);

        Intent intent = getIntent();
        Bundle item = intent.getBundleExtra("message");
        long id = item.getLong("id");
        long message_id = item.getLong("message_id");
        long publish_id = item.getLong("publish_id");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String domain = settings.getString("server_address", "http://115.28.223.203:8080");
        manager = new MessageDataManager(getBaseContext(), domain);
        message = manager.getMessage(id);
        if (message != null && (message.getContent().isEmpty() || message.getContent().contentEquals(""))){
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                progressDialog = ProgressDialog.show(DetailActivity.this, "", getString(R.string.loading), true, false);
                new LoadMessageTask().execute(id, publish_id, message_id);
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

        //Link
        if(!item.getLink().isEmpty()) {
            TextView linkView = (TextView) findViewById(R.id.link);
            linkView.setText("Link: "+item.getLink());
            linkView.setAutoLinkMask(Linkify.ALL);
            linkView.setMovementMethod(LinkMovementMethod.getInstance());
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoadMessageTask extends AsyncTask<Long, Void, MessageItem> {

        @Override
        protected MessageItem doInBackground(Long...params) {
            //get remote message, and save to db.
            return manager.getMessageDetail(getBaseContext(), params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(MessageItem result) {
            if(result != null){
                Log.d(LOG_TAG, "AsyncTask result: " + result.toString());
                renderDetail(result);
                result.setStatus(1);
                manager.updateMessage(result);
                //notify main to update UI.
                if (prefStatus == null){
                    prefStatus = getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);
                }
                prefStatus.edit().putBoolean(STATUS_DATA_UPDATE, true).apply();
            }else{
                Toast.makeText(DetailActivity.this, R.string.access_failure, Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }
}
