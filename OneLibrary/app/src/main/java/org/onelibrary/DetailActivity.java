package org.onelibrary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailActivity extends Activity {

    private static final String LOG_TAG = "DetailActivity";

    private DbAdapter mDbAdapter;
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
        mDbAdapter = DbAdapter.getInstance(getBaseContext());
        manager = new MessageDataManager(getBaseContext());

        message = mDbAdapter.getMessage(id);

        if (message.getContent().isEmpty() || message.getContent().contentEquals("")){
            progressDialog = ProgressDialog.show(DetailActivity.this, "", getString(R.string.loading), true, false);
            new LoadMessageTask().execute(id, message_id);
        }

        message = mDbAdapter.getMessage(id);
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

    private MessageItem getMessageDetail(int id, int message_id){
        MessageItem item = new MessageItem();
        try {
            NetworkAdapter adapter = new NetworkAdapter(getBaseContext());

            Bundle params = new Bundle();
            params.putString("message_id", String.valueOf(message_id));

            Log.d(LOG_TAG, "Request params: " + params.toString());
            JSONObject result = adapter.request(getString(R.string.get_message_detail_url), params);
            if(result.getInt("errno") == 0){
                Log.d(LOG_TAG, "success to get message detail: " + result.getString("result"));

                JSONObject messageResult = result.getJSONObject("result");
                item.setId(id);
                item.setTitle(messageResult.getString("title"));
                item.setAuthor(messageResult.getString("author"));
                item.setContent(messageResult.getString("content"));
                item.setCategory(messageResult.getString("category"));
                item.setTags(messageResult.getString("tags"));
                item.setLink(messageResult.getString("link"));

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(messageResult.getLong("pubdate") *1000);
                String pub_date = format.format(date);
                item.setPubdate(pub_date);
                item.setStatus(1);
                item.setCtime(Calendar.getInstance());
            }else{
                Log.d(LOG_TAG, "failure: " + result.getString("errmsg"));
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return item;
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoadMessageTask extends AsyncTask<Integer, Void, MessageItem> {

        @Override
        protected MessageItem doInBackground(Integer...params) {
            //get remote message, and save to db.
            return getMessageDetail(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(MessageItem result) {
            if(result != null){
                Log.d(LOG_TAG, "AsyncTask result: " + result.toString());
                renderDetail(result);
                manager = new MessageDataManager(mDbAdapter);
                result.setStatus(1);
                manager.updateMessage(result);
            }
            progressDialog.dismiss();
        }
    }
}
