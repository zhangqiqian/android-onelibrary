package org.onelibrary;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.onelibrary.data.DatabaseAdapter;
import org.onelibrary.data.MessageCollection;
import org.onelibrary.data.MessageItem;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    public final static String SESSION_INFO = "session_info";
    public final static String IS_LOGIN = "is_login";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";

    private MessageCollection messages = null;
    private DatabaseAdapter mDbAdapter;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences session = getSharedPreferences(SESSION_INFO, 0);
        Boolean isLogin = session.getBoolean(IS_LOGIN, false);
        if (!isLogin){
            //auto login
            try {
                NetworkAdapter adapter = new NetworkAdapter();
                String username = session.getString(USERNAME, "");
                String password = session.getString(PASSWORD, "");

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if(username.equals("") || password.equals("")){
                    //logout
                    startActivity(intent);
                }else{
                    Bundle params = new Bundle();
                    params.putString("username", session.getString(USERNAME, ""));
                    params.putString("password", session.getString(PASSWORD, ""));

                    Map<String, Object> result = adapter.request(getString(R.string.login_url), params);
                    if(result.get("errno") == 0){
                        Log.i("MainActivity", "auto login success.");
                        session.edit().putBoolean(IS_LOGIN, true).commit();
                    }else{
                        //logout
                        Log.i("MainActivity", "auto login failure.");
                        startActivity(intent);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        handleIntent(getIntent());

        //list
        messages = getMessages();
        showListView();

        //assert if network is ok
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(MainActivity.this, "Unconnected to network.", Toast.LENGTH_SHORT).show();
        }

    }

    private MessageCollection getMessages(){
        MessageCollection messages = new MessageCollection();
        mDbAdapter = new DatabaseAdapter(this);
        try {
            mDbAdapter.open();
        }catch (SQLException e){
            mDbAdapter.close();
        }

        cursor = mDbAdapter.getAllMessages();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++){
            MessageItem item = new MessageItem();
            item.setId(cursor.getInt(0));
            item.setTitle(cursor.getString(1));
            item.setContent(cursor.getString(2));
            item.setCategory(cursor.getString(3));
            item.setLink(cursor.getString(4));
            item.setPubdate(cursor.getString(5));
            messages.addMessageItem(item);
            cursor.moveToNext();
        }
        mDbAdapter.close();
        return messages;
    }

    private void showListView(){
        ListView itemList = (ListView) findViewById(R.id.message_list);
        if (messages == null) {
            return;
        }
        SimpleAdapter adapter = new SimpleAdapter(this, messages.getAllItemsForListView(), android.R.layout.simple_list_item_2, new String[]{MessageItem.TITLE,MessageItem.PUBDATE}, new int[]{android.R.id.text1,android.R.id.text2});
        itemList.setAdapter(adapter);
        itemList.setOnItemClickListener(this);
        itemList.setSelection(0);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle bundle = new Bundle();
        MessageItem item = messages.getMessageItem(position);
        bundle.putInt("id", item.getId());
        bundle.putString("title", item.getTitle());
        bundle.putString("content", item.getContent());
        bundle.putString("category", item.getCategory());
        bundle.putString("link", item.getLink());
        bundle.putString("pubdate", item.getPubdate());

        intent.putExtra("message_item", bundle);
        startActivityForResult(intent, 0);

    }
    @Override

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
}
