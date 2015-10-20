package org.onelibrary;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.LocationDataManager;
import org.onelibrary.data.LocationEntry;

import java.lang.reflect.Method;
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
        /*getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;*/
        setIconEnable(menu, true);

        MenuItem item1 = menu.add(0, 1, 0, R.string.menu_refresh);
        item1.setIcon(android.R.drawable.ic_menu_rotate);
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                initListView();
                return false;
            }
        });

        MenuItem item2 = menu.add(0, 1, 0, R.string.menu_clear);
        item2.setIcon(android.R.drawable.ic_menu_delete);
        item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                manager.clearPoints();
                initListView();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return super.onPrepareOptionsMenu(menu);
    }

    //enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效
    private void setIconEnable(Menu menu, boolean enable)
    {
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);

            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private void initListView()   {
        listItems = new ArrayList<String>();
        List<LocationEntry> points = manager.getPoints();

        LocationEntry lastPoint = null;
        for (LocationEntry entry:points){
            double distance = 0;
            if(lastPoint == null){
                distance = 0;
                lastPoint = entry;
            }else{
                distance = LocationDataManager.getDistance(lastPoint.getLongitude(), lastPoint.getLatitude(), entry.getLongitude(), entry.getLatitude());
                lastPoint = entry;
            }
            listItems.add(entry.toString()+" "+distance+"m");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listItems
        );
        setListAdapter(adapter);
    }

}
