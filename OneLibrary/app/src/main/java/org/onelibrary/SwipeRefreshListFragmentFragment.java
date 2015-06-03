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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    private DbAdapter mDbAdapter;
    private List<MessageItem> messages;

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

        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null){
            initiateRefresh();
            // Stop the refreshing indicator
            setRefreshing(true);
        }

        /**
         * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
         * uses the system-defined simple_list_item_1 layout that contains one TextView.
         */
        messages = getLocalMessages();

        ArrayList<String> titles = new ArrayList<String>();
        for (MessageItem item : messages){
            titles.add(item.getTitle());
        }

        ListAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item,
                R.id.item_text,
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

        MessageItem item = messages.get(position);
        bundle.putInt("id", item.getId());
        bundle.putInt("message_id", item.getMessageId());
        intent.putExtra("message", bundle);
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
        new LoadMessagesTask().execute();
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)
    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(int itemSize ) {
        Log.i(LOG_TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        adapter.clear();

        messages = getLocalMessages();
        for (MessageItem item : messages){
            adapter.add(item.getTitle());
        }

        // Stop the refreshing indicator
        setRefreshing(false);

        Toast.makeText(getActivity(), "Updated "+ itemSize +" message(s).", Toast.LENGTH_LONG).show();
    }
    // END_INCLUDE (refresh_complete)


    private List<MessageItem> getLocalMessages(){
        mDbAdapter = new DbAdapter(getActivity());
        MessageDataManager messageDataManager = new MessageDataManager(mDbAdapter);

        return messageDataManager.getMessageList();
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

            long new_last_time = System.currentTimeMillis()/1000;
            if(new_last_time - last_time > 60){
                next_start = 0;
            }

            Bundle params = new Bundle();
            params.putString(LAST_TIME, String.valueOf(last_time));
            params.putString(LAST_MESSAGE_ID, String.valueOf(last_message_id));
            params.putString(LAST_LONGITUDE, String.valueOf(longitude));
            params.putString(LAST_LATITUDE, String.valueOf(latitude));
            params.putString(NEXT_START, String.valueOf(next_start));

            Log.i(LOG_TAG, "Request params: " + params.toString());
            JSONObject result = adapter.request(getString(R.string.get_messages_url), params);
            if(result.getInt("errno") == 0){
                Log.i(LOG_TAG, "success to get messages");

                int start = result.getInt("start");

                JSONArray messagesArray = result.getJSONArray("result");
                if(messagesArray.length() > 0){
                    for (int i = 0;i< messagesArray.length();i++){
                        JSONObject message = messagesArray.getJSONObject(i);
                        MessageItem item = new MessageItem();
                        item.setPublishId(message.getInt("publish_id"));
                        item.setMessageId(message.getInt("message_id"));
                        item.setTitle(message.getString("title"));
                        item.setAuthor("");
                        item.setContent("");
                        item.setCategory("");
                        item.setTags("");
                        item.setLink("");
                        item.setPubdate("");
                        item.setStatus(0);
                        item.setCtime(Calendar.getInstance());
                        messageItems.add(item);
                    }
                    session.edit().putInt(NEXT_START, start).putLong(LAST_TIME, new_last_time).apply();
                    Log.i(LOG_TAG, "new_last_time=" + new_last_time + " start=" + start);
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
    private class LoadMessagesTask extends AsyncTask<Void, Void, List<MessageItem>> {

        @Override
        protected List<MessageItem> doInBackground(Void...params) {
            //get remote message, and save to db.
            return getRemoteMessages();
        }

        @Override
        protected void onPostExecute(List<MessageItem> result) {
            mDbAdapter = new DbAdapter(getActivity());
            MessageDataManager manager = new MessageDataManager(mDbAdapter);
            int size = result.size();
            for (MessageItem item : result){
                if(mDbAdapter.messageIsExist(item)){
                    size--;
                }else{
                    manager.addMessage(item);
                }
            }
            onRefreshComplete(size);
        }
    }

}
