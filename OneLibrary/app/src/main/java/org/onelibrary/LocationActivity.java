package org.onelibrary;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.LocationDataManager;
import org.onelibrary.data.LocationEntry;

import java.util.ArrayList;
import java.util.List;


public class LocationActivity extends ListActivity {

    //LocationService gps;
    private List<String> listItems;
    LocationDataManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        manager = new LocationDataManager(getBaseContext());

        initListView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            manager.clearPoints();
            initListView();
        }

        if (id == R.id.action_refresh) {
            initListView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initListView()   {
        listItems = new ArrayList<String>();
        List<LocationEntry> points = manager.getPoints();

        for (LocationEntry entry:points){
            listItems.add(entry.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listItems
        );
        setListAdapter(adapter);
    }

}
