package org.onelibrary.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * DB Operations
 * DB: onelibrary Table: location
 * Created by niko on 2/9/15.
 */
public class LocationDbAdapter {

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    private static final String TAG = "LocationDbAdapter";

    public static final String TABLE_NAME = "location";
    public static final String COLUMN_ID = LocationEntry.ID;
    public static final String COLUMN_NAME = LocationEntry.NAME;
    public static final String COLUMN_LONGITUDE = LocationEntry.LONGITUDE;
    public static final String COLUMN_LATITUDE = LocationEntry.LATITUDE;
    public static final String COLUMN_TIME = LocationEntry.CTIME;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + TEXT_TYPE + COMMA_SEP
                    + COLUMN_LONGITUDE + REAL_TYPE + COMMA_SEP
                    + COLUMN_LATITUDE + REAL_TYPE + COMMA_SEP
                    + COLUMN_TIME + INTEGER_TYPE
                    + " )";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "onelibrary.db";

    public LocationDbAdapter(Context ctx){
        this.mCtx = ctx;
    }

    /**
     * close() method that used to close database
     */
    public void close(){
        mDbHelper.close();
    }

    /**
     * Open() method
     * @return
     * @throws SQLException
     */
    public LocationDbAdapter openWriteDB() throws SQLException{
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Open() method
     * @return
     * @throws SQLException
     */
    public LocationDbAdapter openReadDB() throws SQLException{
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getReadableDatabase();
        return this;
    }


    /**
     * Inserts a {@link org.onelibrary.data.LocationEntry} item to the
     * database.
     */
    public final long insert(LocationEntry entry) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, entry.getName());
        values.put(COLUMN_LONGITUDE, entry.getLongitude());
        values.put(COLUMN_LATITUDE, entry.getLatitude());
        values.put(COLUMN_TIME, entry.getCtime().getTimeInMillis());

        // Insert the new row, returning the primary key value of the new row
        return mDb.insert(TABLE_NAME, "null", values);
    }


    /**
     * Returns a list of {@link org.onelibrary.data.LocationEntry}
     * objects from the database for a given day. The list can be empty (but not {@code null}) if
     * there are no such items. This method looks at the day that the calendar argument points at.
     */
    public final List<LocationEntry> getLocationList() {
        String[] projection = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_LONGITUDE,
                COLUMN_LATITUDE,
                COLUMN_TIME
        };

        // sort ASC based on the time of the entry
        String sortOrder = COLUMN_TIME + " ASC";
        String selection = null; //COLUMN_NAME + " LIKE ?";

        Cursor cursor = mDb.query(
                TABLE_NAME,                 // The table to query
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
        return result;
    }

    /**
     * Deletes all the entries in the database for the given day. The argument {@code day} should
     * match the format provided
     */
    public final int delete(int id) {
        // Define 'where' part of the query.
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return mDb.delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Deletes all the entries in the database for the given day. The argument {@code day} should
     * match the format provided
     */
    public final int deleteByName(String name) {
        // Define 'where' part of the query.
        String selection = COLUMN_NAME + " = ?";
        String[] selectionArgs = {name};
        return mDb.delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Deletes all the entries in the database for the day that the {@link Calendar}
     * argument points at.
     */
    public final int deleteAll() {
        return mDb.delete(TABLE_NAME, null, null);
    }

    /**
     * update method that used to update a location in the database
     * @param entry LocationEntry
     * @return
     */
    public long update(LocationEntry entry){

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, entry.getName());
        values.put(COLUMN_LONGITUDE, entry.getLongitude());
        values.put(COLUMN_LATITUDE, entry.getLatitude());
        values.put(COLUMN_TIME, entry.getCtime().getTimeInMillis());

        return mDb.update(TABLE_NAME, values, COLUMN_ID + "=" + entry.getId(), null);
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "---------------SQLiteDatabase onCreate--------------");
            Log.i(TAG, SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }


}
