package org.onelibrary;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.data.DatabaseAdapter;
import org.onelibrary.data.MessageItem;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends Activity {

    private static final String LOG_TAG = "DetailActivity";

    private DatabaseAdapter mDbAdapter;
    private MessageItem message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        Bundle item = intent.getBundleExtra("message");
        int id = item.getInt("id");
        int message_id = item.getInt("message_id");

        try {
            mDbAdapter = new DatabaseAdapter(getBaseContext());
            mDbAdapter.openReadDB();
            Cursor cursor = mDbAdapter.getMessage(id);

            String content = cursor.getString(5);
            if (content.isEmpty() || content.contentEquals("")){
                new LoadMessageTask().execute(id, message_id);
            }

            cursor = mDbAdapter.getMessage(id);
            message = new MessageItem();

            message.setId(cursor.getInt(0));
            message.setPublishId(cursor.getInt(1));
            message.setMessageId(cursor.getInt(2));
            message.setTitle(cursor.getString(3));
            message.setAuthor(cursor.getString(4));
            message.setContent(cursor.getString(5));
            message.setCategory(cursor.getString(6));
            message.setLink(cursor.getString(7));
            message.setTags(cursor.getString(8));
            message.setPubdate(cursor.getString(9));

            mDbAdapter.close();
        }catch (SQLException e){
            mDbAdapter.close();
        }finally {
            mDbAdapter.close();
        }

        renderDetail(message);
    }

    private void renderDetail(MessageItem item){
        //title
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(item.getTitle());

        //publish date
        TextView categoryView = (TextView) findViewById(R.id.category);
        categoryView.setText("["+item.getCategory()+"]");

        //publish date
        TextView pubdateView = (TextView) findViewById(R.id.pubdate);
        pubdateView.setText(item.getPubdate());

        //content
        TextView contentView = (TextView) findViewById(R.id.content);
        contentView.setText(item.getContent());

        //Tags
        TextView tagsView = (TextView) findViewById(R.id.tags);
        tagsView.setText("Tags: "+item.getTags());
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

            Log.i(LOG_TAG, "Request params: " + params.toString());
            JSONObject result = adapter.request(getString(R.string.get_message_detail_url), params);
            if(result.getInt("errno") == 0){
                Log.i(LOG_TAG, "success to get message detail: "+result.getString("result"));

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
            }else{
                Log.i(LOG_TAG, "failure: " + result.getString("errmsg"));
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
            Log.i(LOG_TAG, "AsyncTask result: " + result.toString());
            try {
                renderDetail(result);
                mDbAdapter.openWriteDB();
                mDbAdapter.updateMessage(result);
            }catch (SQLException e){
                mDbAdapter.close();
            }finally {
                mDbAdapter.close();
            }
        }
    }
}
