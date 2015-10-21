/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onelibrary.data;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.R;
import org.onelibrary.util.LocationConverter;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A class that wraps database access and provides a cache for various GPS data.
 */
public class LocationDataManager {

    private DbAdapter mDbAdapter;

    private final List<LocationEntry> mPointsList = new ArrayList<LocationEntry>();

    public LocationDataManager(Context context) {
        mDbAdapter = DbAdapter.getInstance(context);
    }

    public LocationDataManager(DbAdapter dbAdapter) {
        mDbAdapter = dbAdapter;
    }

    /**
     * Returns a list of Location
     * objects for the day that the {@link Calendar} object points at. Internally it uses
     * a cache to speed up subsequent calls. If there is no cached value, it gets the result from
     * the database.
     */
    public final List<LocationEntry> getPoints() {
        synchronized (mPointsList) {
            List<LocationEntry> points = mDbAdapter.getLocationList();
            mPointsList.clear();
            mPointsList.addAll(points);
        }
        return mPointsList;
    }

    /**
     * Clears the data for the day that the {@link Calendar} object falls on. This method
     * removes the entries from the database and updates the cache accordingly.
     */
    public final int clearPoints() {
        synchronized (mPointsList) {
            mPointsList.clear();
            return mDbAdapter.deleteAllLocations();
        }
    }

    /**
     * Adds a Location point to the
     * database and cache if it is a new point.
     */
    public final void addPoint(LocationEntry entry) {
        synchronized (mPointsList) {
            mDbAdapter.insertLocation(entry);
            mPointsList.add(entry);
        }
    }

    public Bundle getBDLocation(Context ctx, double longitude, double latitude){
        Bundle bdLocation = new Bundle();
        try{
            NetworkAdapter adapter = new NetworkAdapter(ctx);
            Bundle params = new Bundle();
            params.putString("from", "0");
            params.putString("to", "4");
            params.putString("x", String.valueOf(longitude));
            params.putString("y", String.valueOf(latitude));

            JSONObject result = adapter.get(ctx.getString(R.string.baidu_map_convert_url), params);
            Log.i("BD LOCATION", result.toString());
            try {
                if(result.has("error") && result.getInt("error") == 0){
                    Log.i("BD LOCATION", "From Baidu Converter");
                    String x = new String(Base64.decode(result.getString("x").getBytes(), Base64.DEFAULT));
                    String y = new String(Base64.decode(result.getString("y").getBytes(), Base64.DEFAULT));

                    bdLocation.putDouble("longitude", Double.valueOf(x));
                    bdLocation.putDouble("latitude", Double.valueOf(y));
                }else{
                    //convert lon and lat to baidu lon and lat.
                    Log.i("BD LOCATION", "From Local Converter");
                    bdLocation = LocationConverter.convertWgs2Bd(latitude, longitude);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return bdLocation;
        }catch (IOException e){
            e.printStackTrace();
        }
        return bdLocation;
    }

    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2)
    {
        double EARTH_RADIUS = 6378.137;
        double radLat1 = rad(latitude1);
        double radLat2 = rad(latitude2);
        double a = radLat1 - radLat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000.0;
        return s * 1000;
    }

    public static double rad(double d)
    {
        return d * 3.1415926535898 / 180.0;
    }

}
