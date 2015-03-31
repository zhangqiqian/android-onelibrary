/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onelibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.data.DatabaseAdapter;
import org.onelibrary.data.MessageCollection;
import org.onelibrary.data.MessageItem;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A sample which shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link android.widget.ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 *
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link android.support.v4.widget.SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class SwipeRefreshListFragmentFragment extends SwipeRefreshListFragment {

    private static final String LOG_TAG = SwipeRefreshListFragmentFragment.class.getSimpleName();

    public final static String MAIN_INFO = "main_info";
    public final static String LAST_TIME = "last_time";
    public final static String LAST_MESSAGE_ID = "last_message_id";
    public final static String LAST_LONGITUDE = "longitude";
    public final static String LAST_LATITUDE = "latitude";
    public final static String NEXT_START = "next_start";

    private DatabaseAdapter mDbAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
         * uses the system-defined simple_list_item_1 layout that contains one TextView.
         */

        List<MessageItem> messages = getLocalMessages().getAllMessageItems();

        ArrayList<String> titles = new ArrayList<String>();
        for (MessageItem item : messages){
            titles.add(item.getTitle());
        }
        ListAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                titles);

        // Set the adapter between the ListView and its backing data.
        setListAdapter(adapter);

        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light,
                        R.color.holo_purple_light, R.color.holo_orange_light);
                initiateRefresh();
            }
        });
        // END_INCLUDE (setup_refreshlistener)
    }
    // END_INCLUDE (setup_views)

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }*/

    // BEGIN_INCLUDE (setup_refresh_menu_listener)
    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     *
     * <p>A color scheme menu item used for demonstrating the use of SwipeRefreshLayout's color
     * scheme functionality. This kind of menu item should not be incorporated into your app,
     * it just to demonstrate the use of color. Instead you should choose a color scheme based
     * off of your application's branding.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Log.i(LOG_TAG, "Refresh menu item selected");

                setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light,
                        R.color.holo_purple_light, R.color.holo_orange_light);

                // We make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
                if (!isRefreshing()) {
                    setRefreshing(true);
                }

                // Start our refresh background task
                initiateRefresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE (setup_refresh_menu_listener)

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        Bundle bundle = new Bundle();
        ListAdapter adapter = getListAdapter();
        //MessageItem item = messages.getMessageItem(position);
        bundle.putLong("id", id);
        bundle.putInt("message_id", 12);
        bundle.putString("title", "test title" +position);
        bundle.putString("author", "niko" + position);
        bundle.putString("content", "test content: " + adapter.getItem(position));
        bundle.putString("category", "life");
        bundle.putString("link", "http://www.qq.com/" + id);
        bundle.putString("tags", "test, life, "+id);
        bundle.putString("pubdate", "2015-03-07 00:23:49");

        intent.putExtra("message_item", bundle);
        startActivityForResult(intent, 0);
    }

    // BEGIN_INCLUDE (initiate_refresh)
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        new LoadMessageTask().execute();
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)
    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<MessageItem> result) {
        Log.i(LOG_TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        adapter.clear();

        List<MessageItem> messages = getLocalMessages().getAllMessageItems();
        for (MessageItem item : messages){
            adapter.add(item.getTitle());
        }

        // Stop the refreshing indicator
        setRefreshing(false);

        Toast.makeText(getActivity(), "Updated "+ result.size() +" message(s).", Toast.LENGTH_LONG).show();
    }
    // END_INCLUDE (refresh_complete)


    private MessageCollection getLocalMessages(){
        MessageCollection messages = new MessageCollection();
        mDbAdapter = new DatabaseAdapter(getActivity());
        try {
            mDbAdapter.open();
        }catch (SQLException e){
            mDbAdapter.close();
        }


        Cursor cursor = mDbAdapter.getAllMessages();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++){
            MessageItem item = new MessageItem();
            Log.i(LOG_TAG, "messages:"+ cursor.toString());
            item.setId(cursor.getInt(0));
            item.setMessageId(cursor.getInt(1));
            item.setTitle(cursor.getString(2));
            item.setAuthor(cursor.getString(3));
            item.setContent(cursor.getString(4));
            item.setCategory(cursor.getString(5));
            item.setLink(cursor.getString(6));
            item.setTags(cursor.getString(7));
            item.setPubdate(cursor.getString(8));
            messages.addMessageItem(item);
            cursor.moveToNext();
        }
        mDbAdapter.close();
        return messages;
    }

    private List<MessageItem> getRemoteMessages(){
        List<MessageItem> messageItems = new ArrayList<MessageItem>();
        try {
            NetworkAdapter adapter = new NetworkAdapter(getActivity());
            SharedPreferences session = getActivity().getSharedPreferences(MAIN_INFO, 0);
            long last_time = session.getLong(LAST_TIME, 0);
            long last_message_id = session.getLong(LAST_MESSAGE_ID, 0);
            double longitude = session.getFloat(LAST_LONGITUDE, 0);
            double latitude = session.getFloat(LAST_LATITUDE, 0);
            int next_start = session.getInt(NEXT_START, 0);

            Bundle params = new Bundle();
            params.putString(LAST_TIME, String.valueOf(last_time));
            params.putString(LAST_MESSAGE_ID, String.valueOf(last_message_id));
            params.putString(LAST_LONGITUDE, String.valueOf(longitude));
            params.putString(LAST_LATITUDE, String.valueOf(latitude));
            params.putString(NEXT_START, String.valueOf(next_start));

            JSONObject result = adapter.request(getString(R.string.get_messages_url), params);
            if(result.getInt("errno") == 0){
                Log.i("MainActivity", "success to get messages: " + result.get("result").toString());

                int start = result.getInt("start");
                long new_last_time = System.currentTimeMillis()/1000;
                long new_last_message_id = last_message_id;

                JSONArray messages = result.getJSONArray("result");
                if(messages.length() > 0){
                    for (int i = 0;i< messages.length();i++){
                        JSONObject message = messages.getJSONObject(i);
                        MessageItem item = new MessageItem();
                        int message_id = message.getInt("message_id");
                        item.setMessageId(message_id);
                        item.setTitle(message.getString("title"));
                        item.setAuthor(message.getString("author"));
                        item.setContent(message.getString("content"));
                        item.setCategory(message.getString("category"));
                        item.setTags(message.getString("tags"));
                        item.setLink(message.getString("link"));

                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = new Date(message.getLong("pubdate") *1000);
                        String pub_date = format.format(date);
                        item.setPubdate(pub_date);

                        messageItems.add(item);
                        if(message_id > new_last_message_id){
                            new_last_message_id = message_id;
                        }
                    }
                    session.edit().putInt(NEXT_START, start).putLong(LAST_TIME, new_last_time).putLong(LAST_MESSAGE_ID, new_last_message_id).apply();
                    Log.i(LOG_TAG, "new_last_time=" + new_last_time + " start=" + start + " new_last_message_id="+new_last_message_id);
                }
            }else{
                Log.i(LOG_TAG, "failure: " + result.getString("errmsg"));
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return messageItems;
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoadMessageTask extends AsyncTask<Void, Void, List<MessageItem>> {

        @Override
        protected List<MessageItem> doInBackground(Void...params) {
            //get remote message, and save to db.
            return getRemoteMessages();
        }

        @Override
        protected void onPostExecute(List<MessageItem> result) {
            Log.i(LOG_TAG, "result: " + result);
            mDbAdapter = new DatabaseAdapter(getActivity());
            try {
                mDbAdapter.open();
                for (MessageItem item : result){
                    mDbAdapter.createMessage(item);
                }
            }catch (SQLException e){
                mDbAdapter.close();
            }finally {
                mDbAdapter.close();
            }

            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result);
        }
    }

}
