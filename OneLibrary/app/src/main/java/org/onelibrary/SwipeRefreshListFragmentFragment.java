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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A sample which shows how to use {@link SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 *
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class SwipeRefreshListFragmentFragment extends SwipeRefreshListFragment implements AdapterView.OnItemLongClickListener{

    private static final String LOG_TAG = SwipeRefreshListFragmentFragment.class.getSimpleName();

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
         * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light,
                        R.color.holo_purple_light, R.color.holo_orange_light);
                initiateRefresh();
            }
        });

        getListView().setOnItemLongClickListener(this);
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
                Log.d(LOG_TAG, "Refresh menu item selected");

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
        Log.d(LOG_TAG, "------- initiateRefresh -------");
        LocationService locationService = new LocationService(getActivity());
        double longitude = locationService.getLongitude();
        double latitude  = locationService.getLatitude();

        // check if location enabled
        new LoadMessagesTask().execute(longitude, latitude);
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)
    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(int itemSize ) {
        Log.d(LOG_TAG, "onRefreshComplete");

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
        MessageDataManager messageDataManager = new MessageDataManager(getActivity());

        return messageDataManager.getMessageList();
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoadMessagesTask extends AsyncTask<Double, Void, List<MessageItem>> {

        @Override
        protected List<MessageItem> doInBackground(Double...params) {
            //get remote message, and save to db.
            MessageDataManager manager = new MessageDataManager(getActivity());
            return manager.getRemoteMessages(getActivity(), params[0], params[1]);
        }

        @Override
        protected void onPostExecute(List<MessageItem> result) {
            mDbAdapter = DbAdapter.getInstance(getActivity());
            MessageDataManager manager = new MessageDataManager(getActivity());
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete the message?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
        return true; // let the system show the context menu
    }

}
