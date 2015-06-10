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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A helper class to set up the database
 */
public class DbAdapter extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "onelibrary.db";

    public static final String TABLE_NAME_MESSAGE = "message";
    public static final String TABLE_NAME_LOCATION = "location";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_LOCATION =
            "CREATE TABLE " + TABLE_NAME_LOCATION + " ("
                    + LocationEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LocationEntry.NAME + TEXT_TYPE + COMMA_SEP
                    + LocationEntry.LONGITUDE + REAL_TYPE + COMMA_SEP
                    + LocationEntry.LATITUDE + REAL_TYPE + COMMA_SEP
                    + LocationEntry.CTIME + INTEGER_TYPE
                    + " )";

    private static final String SQL_CREATE_ENTRIES_MESSAGE =
            "CREATE TABLE " + TABLE_NAME_MESSAGE + " ("
                    + MessageItem.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + MessageItem.PUBLISHID + INTEGER_TYPE + " UNIQUE NOT NULL" + COMMA_SEP
                    + MessageItem.MESSAGEID + INTEGER_TYPE + " NOT NULL" + COMMA_SEP
                    + MessageItem.TITLE + TEXT_TYPE + " NOT NULL" + COMMA_SEP
                    + MessageItem.AUTHOR + TEXT_TYPE + COMMA_SEP
                    + MessageItem.CONTENT + TEXT_TYPE + COMMA_SEP
                    + MessageItem.CATEGORY + TEXT_TYPE + COMMA_SEP
                    + MessageItem.LINK + TEXT_TYPE + COMMA_SEP
                    + MessageItem.TAGS + TEXT_TYPE + COMMA_SEP
                    + MessageItem.PUBDATE + TEXT_TYPE + COMMA_SEP
                    + MessageItem.STATUS + INTEGER_TYPE + COMMA_SEP
                    + MessageItem.CTIME + INTEGER_TYPE
                    + " )";

    private static final String SQL_DELETE_ENTRIES_MESSAGE = "DROP TABLE IF EXISTS " + TABLE_NAME_MESSAGE;
    private static final String SQL_DELETE_ENTRIES_LOCATION = "DROP TABLE IF EXISTS " + TABLE_NAME_LOCATION;


    public DbAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_LOCATION);
        db.execSQL(SQL_CREATE_ENTRIES_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_LOCATION);
        db.execSQL(SQL_DELETE_ENTRIES_MESSAGE);
        onCreate(db);
    }

    /**
     * getMessage() method that used to get a Message by Id from database
     * @param id
     * @return
     */
    public MessageItem getMessage(int id){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                MessageItem.ID,
                MessageItem.PUBLISHID,
                MessageItem.MESSAGEID,
                MessageItem.TITLE,
                MessageItem.AUTHOR,
                MessageItem.CONTENT,
                MessageItem.CATEGORY,
                MessageItem.LINK,
                MessageItem.TAGS,
                MessageItem.PUBDATE,
                MessageItem.STATUS
        };

        String selection = MessageItem.ID + " = ?";

        Cursor cursor = db.query(
                TABLE_NAME_MESSAGE,         // The table to query
                projection,                 // The columns to return
                selection,                  // The columns for the WHERE clause
                new String[]{String.valueOf(id)},                       // The values for the WHERE clause, new String[]{""},
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                null                       // The sort order
        );

        MessageItem item = new MessageItem();
        int count = cursor.getCount();
        if (count > 0) {
            cursor.moveToFirst();
            item.setId(cursor.getInt(0));
            item.setPublishId(cursor.getInt(1));
            item.setMessageId(cursor.getInt(2));
            item.setTitle(cursor.getString(3));
            item.setAuthor(cursor.getString(4));
            item.setContent(cursor.getString(5));
            item.setCategory(cursor.getString(6));
            item.setLink(cursor.getString(7));
            item.setTags(cursor.getString(8));
            item.setPubdate(cursor.getString(9));
            item.setStatus(cursor.getInt(10));
        }
        cursor.close();
        if (db.isOpen()){
            db.close();
        }
        return item;
    }

    /**
     * messageIsExist() method that used to assert a Message if exists.
     * @param item
     * @return
     */
    public boolean messageIsExist(MessageItem item){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                MessageItem.ID,
        };

        String selection = MessageItem.PUBLISHID + " = ?";

        Cursor cursor = db.query(
                TABLE_NAME_MESSAGE,         // The table to query
                projection,                 // The columns to return
                selection,                  // The columns for the WHERE clause
                new String[]{String.valueOf(item.getPublishId())},                       // The values for the WHERE clause, new String[]{""},
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                null                       // The sort order
        );

        int count = cursor.getCount();
        cursor.close();
        if (db.isOpen()){
            db.close();
        }
        return count > 0;
    }

    /**
     * Inserts a {@link org.onelibrary.data.MessageItem} item to the
     * database.
     */
    public final long insertMessage(MessageItem item) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(MessageItem.PUBLISHID, item.getPublishId());
        values.put(MessageItem.MESSAGEID, item.getMessageId());
        values.put(MessageItem.TITLE, item.getTitle());
        values.put(MessageItem.AUTHOR, item.getAuthor());
        values.put(MessageItem.CONTENT, item.getContent());
        values.put(MessageItem.CATEGORY, item.getCategory());
        values.put(MessageItem.LINK, item.getLink());
        values.put(MessageItem.TAGS, item.getTags());
        values.put(MessageItem.PUBDATE, item.getPubdate());
        values.put(MessageItem.STATUS, item.getStatus());
        values.put(MessageItem.CTIME, item.getCtime().getTimeInMillis());

        // Insert the new row, returning the primary key value of the new row
        long ret = db.insert(TABLE_NAME_MESSAGE, "null", values);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Returns a list of {@link org.onelibrary.data.MessageItem}
     */
    public final List<MessageItem> getMessageList() {

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                MessageItem.ID,
                MessageItem.PUBLISHID,
                MessageItem.MESSAGEID,
                MessageItem.TITLE,
                MessageItem.AUTHOR,
                MessageItem.CONTENT,
                MessageItem.CATEGORY,
                MessageItem.LINK,
                MessageItem.TAGS,
                MessageItem.PUBDATE,
                MessageItem.STATUS
        };

        // sort ASC based on the time of the entry
        String sortOrder = MessageItem.CTIME + " DESC";
        String selection = null; //LocationEntry.NAME + " LIKE ?";

        Cursor cursor = db.query(
                TABLE_NAME_MESSAGE,         // The table to query
                projection,                 // The columns to return
                selection,                  // The columns for the WHERE clause
                null,                       // The values for the WHERE clause, new String[]{""},
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                sortOrder                   // The sort order
        );

        List<MessageItem> result = new ArrayList<MessageItem>();
        int count = cursor.getCount();
        if (count > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                MessageItem item = new MessageItem(
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getInt(10)
                );
                item.setId(cursor.getInt(0));
                result.add(item);
                cursor.moveToNext();
            }
        }
        cursor.close();
        if (db.isOpen()){
            db.close();
        }
        return result;
    }

    /**
     * updateMessage method that used to update a message in the database
     * @param item MessageItem
     * @return
     */
    public long updateMessage(MessageItem item){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageItem.AUTHOR, item.getAuthor());
        values.put(MessageItem.CONTENT, item.getContent());
        values.put(MessageItem.CATEGORY, item.getCategory());
        values.put(MessageItem.LINK, item.getLink());
        values.put(MessageItem.TAGS, item.getTags());
        values.put(MessageItem.PUBDATE, item.getPubdate());
        values.put(MessageItem.STATUS, item.getStatus());
        values.put(MessageItem.CTIME, item.getCtime().getTimeInMillis());

        int ret = db.update(TABLE_NAME_MESSAGE, values, MessageItem.ID + "=" + item.getId(), null);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Deletes the entry in the database
     */
    public final int deleteMessage(MessageItem item) {
        SQLiteDatabase db = getWritableDatabase();
        // Define 'where' part of the query.
        String selection = MessageItem.ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};
        int ret = db.delete(TABLE_NAME_MESSAGE, selection, selectionArgs);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Deletes all the entries in the database
     */
    public final int deleteMessageByMessageId(int message_id) {
        SQLiteDatabase db = getWritableDatabase();
        // Define 'where' part of the query.
        String selection = MessageItem.MESSAGEID + " = ?";
        String[] selectionArgs = {String.valueOf(message_id)};
        int ret = db.delete(TABLE_NAME_MESSAGE, selection, selectionArgs);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Deletes all the entries in the database
     */
    public final int deleteAllMessages() {
        SQLiteDatabase db = getWritableDatabase();
        int ret = db.delete(TABLE_NAME_MESSAGE, null, null);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Inserts a {@link org.onelibrary.data.LocationEntry} item to the
     * database.
     */
    public final long insertLocation(LocationEntry entry) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.NAME, entry.getName());
        values.put(LocationEntry.LONGITUDE, entry.getLongitude());
        values.put(LocationEntry.LATITUDE, entry.getLatitude());
        values.put(LocationEntry.CTIME, entry.getCtime().getTimeInMillis());

        // Insert the new row, returning the primary key value of the new row

        long ret = db.insert(TABLE_NAME_LOCATION, null, values);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Returns a list of {@link org.onelibrary.data.LocationEntry}
     * objects from the database for a given day. The list can be empty (but not {@code null}) if
     * there are no such items. This method looks at the day that the calendar argument points at.
     */
    public final List<LocationEntry> getLocationList() {

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                LocationEntry.ID,
                LocationEntry.NAME,
                LocationEntry.LONGITUDE,
                LocationEntry.LATITUDE,
                LocationEntry.CTIME
        };

        // sort ASC based on the time of the entry
        String sortOrder = LocationEntry.CTIME + " ASC";
        String selection = null; //LocationEntry.NAME + " LIKE ?";

        Cursor cursor = db.query(
                TABLE_NAME_LOCATION,                 // The table to query
                projection,                 // The columns to return
                selection,                  // The columns for the WHERE clause
                null,                       // The values for the WHERE clause, new String[]{""},
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                sortOrder                   // The sort order
        );

        List<LocationEntry> result = new ArrayList<LocationEntry>();
        int count = cursor.getCount();
        if (count > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(cursor.getLong(4));
                LocationEntry entry = new LocationEntry(
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getDouble(3),
                        cal
                );
                entry.setId(cursor.getInt(0));
                result.add(entry);
                cursor.moveToNext();
            }
        }
        cursor.close();
        if (db.isOpen()){
            db.close();
        }
        return result;
    }

    /**
     * Deletes all the entries in the database for the given day. The argument {@code day} should
     * match the format provided
     */
    public final int deleteLocation(int id) {
        SQLiteDatabase db = getWritableDatabase();
        // Define 'where' part of the query.
        String selection = LocationEntry.ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        int ret =  db.delete(TABLE_NAME_LOCATION, selection, selectionArgs);
        if (db.isOpen()){
            db.close();
        }
        return ret;

    }

    /**
     * Deletes all the entries in the database for the given day. The argument {@code day} should
     * match the format provided
     */
    public final int deleteLocationByName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        // Define 'where' part of the query.
        String selection = LocationEntry.NAME + " = ?";
        String[] selectionArgs = {name};
        int ret = db.delete(TABLE_NAME_LOCATION, selection, selectionArgs);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }

    /**
     * Deletes all the entries in the database for the day that the {@link Calendar}
     * argument points at.
     */
    public final int deleteAllLocations() {
        SQLiteDatabase db = getWritableDatabase();
        int ret = db.delete(TABLE_NAME_LOCATION, null, null);
        if (db.isOpen()){
            db.close();
        }
        return ret;
    }
}
