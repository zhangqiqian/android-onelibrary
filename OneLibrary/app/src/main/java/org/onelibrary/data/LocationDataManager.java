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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A class that wraps database access and provides a cache for various GPS data.
 */
public class LocationDataManager {

    private DbAdapter mDbAdapter;

    private final List<LocationEntry> mPointsList = new ArrayList<LocationEntry>();

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
}
