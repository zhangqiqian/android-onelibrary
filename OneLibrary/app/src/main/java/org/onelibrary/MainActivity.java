package org.onelibrary;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.lang.annotation.Target;
import java.sql.SQLException;

public class MainActivity extends FragmentActivity {//implements AdapterView.OnItemClickListener {

    public static final String TAG = "MainActivity";

    private static long back_pressed;

    public final static String SESSION_INFO = "session_info";
    public final static String IS_LOGIN = "is_login";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";

    private MessageCollection messages = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
        Boolean isLogin = session.getBoolean(IS_LOGIN, false);
        if (!isLogin){
            //auto login
            String username = session.getString(USERNAME, "");
            String password = session.getString(PASSWORD, "");

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            if(username.equals("") || password.equals("")){
                //logout
                startActivity(intent);
            }else{
                Bundle params = new Bundle();
                params.putString("username", username);
                params.putString("password", password);
                new LoginTask().execute(params);
            }
        }


        //assert if network is ok
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(MainActivity.this, "Network disconnect.", Toast.LENGTH_SHORT).show();
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SwipeRefreshListFragmentFragment fragment = new SwipeRefreshListFragmentFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

    }



    private void showListView(){
        /*ListView itemList = (ListView) findViewById(R.id.message_list);
        if (messages == null) {
            return;
        }
        SimpleAdapter adapter = new SimpleAdapter(this, messages.getAllItemsForListView(), android.R.layout.simple_list_item_2, new String[]{MessageItem.TITLE,MessageItem.PUBDATE}, new int[]{android.R.id.text1,android.R.id.text2});
        itemList.setAdapter(adapter);
        itemList.setOnItemClickListener(this);
        itemList.setSelection(0);*/
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle bundle = new Bundle();
        MessageItem item = messages.getMessageItem(position);
        bundle.putInt("id", item.getId());
        bundle.putInt("message_id", item.getMessageId());
        bundle.putString("title", item.getTitle());
        bundle.putString("author", item.getAuthor());
        bundle.putString("content", item.getContent());
        bundle.putString("category", item.getCategory());
        bundle.putString("link", item.getLink());
        bundle.putString("tags", item.getTags());
        bundle.putString("pubdate", item.getPubdate());

        intent.putExtra("message_item", bundle);
        startActivityForResult(intent, 0);

    }*/

    /*@Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        Log.i("MainActivity", "query: " + intent.getAction());
        Log.i("MainActivity", "query: " + Intent.ACTION_SEARCH);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            Log.i("MainActivity", "query: " + query);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Associate searchable configuration with the SearchView
        /*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_logout) {
            SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
            Boolean isLogin = session.getBoolean(IS_LOGIN, false);
            if (isLogin){
                session.edit().putBoolean(IS_LOGIN, false).remove(PASSWORD).commit();
            }
            Intent intent = new Intent(MainActivity.this, IndexActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class LoginTask extends AsyncTask<Bundle, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Bundle...params) {
            boolean is_ok = false;
            try {
                NetworkAdapter adapter = new NetworkAdapter(getBaseContext());
                JSONObject result = adapter.request(getString(R.string.login_url), params[0]);
                SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);

                if(result.getInt("errno") == 0){
                    is_ok = true;
                    session.edit().putString(USERNAME, params[0].getString(USERNAME)).putString(PASSWORD, params[0].getString(PASSWORD)).putBoolean(IS_LOGIN, true).apply();
                }else{
                    session.edit().putString(USERNAME, params[0].getString(USERNAME)).putBoolean(IS_LOGIN, false).apply();
                }
                return is_ok;
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }
            return is_ok;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i(TAG, "Auto login result: " + result);
            if(!result){
                Looper.prepare();
                Toast.makeText(getBaseContext(), "Failure to login.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
        }else{
            Toast.makeText(getBaseContext(), "Press once again to exit.", Toast.LENGTH_LONG).show();
        }
        back_pressed = System.currentTimeMillis();
    }
}
