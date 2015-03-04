package org.onelibrary.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DB Operations
 * DB: onelibrary Table: message
 * Created by niko on 2/9/15.
 */
public class DatabaseAdapter {

    public static final String KEY_ID           = "id";
    public static final String KEY_MESSAGE_ID   = "message_id";
    public static final String KEY_TITLE        = "title";
    public static final String KEY_AUTHOR       = "author";
    public static final String KEY_CONTENT      = "content";
    public static final String KEY_CATEGORY     = "category";
    public static final String KEY_LINK         = "link";
    public static final String KEY_PUBDATE      = "pubdate";
    public static final String KEY_TAGS         = "tags";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "onelibrary";
    private static final String MESSAGE_TABLE_NAME = "message";
    private static final String MESSAGE_TABLE_CREATE =
            "CREATE TABLE " + MESSAGE_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "message_id INTEGER not null, " +
                    "title TEXT not null, " +
                    "author TEXT not null, " +
                    "content TEXT not null, " +
                    "category TEXT not null, " +
                    "link TEXT, " +
                    "tags TEXT, " +
                    "pubdate TEXT not null);";

    public DatabaseAdapter(Context ctx){
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
    public DatabaseAdapter open() throws SQLException{
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }


    /**
     * createMessage method that used to create a message in the database
     * @param title
     * @param content
     * @param link
     * @param category
     * @param pubdate
     * @return
     */
    public long createMessage(int message_id, String title, String author, String content, String category, String link, String tags, long pubdate){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MESSAGE_ID, message_id);
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_AUTHOR, author);
        initialValues.put(KEY_CONTENT, content);
        initialValues.put(KEY_LINK, link);
        initialValues.put(KEY_CATEGORY, category);
        initialValues.put(KEY_TAGS, tags);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(pubdate);
        String pub_date = format.format(date);
        initialValues.put(KEY_PUBDATE, pub_date);

        return mDb.insert(MESSAGE_TABLE_NAME, null, initialValues);
    }

    /**
     * deleteMessage() method that used to delete Message by id
     * @param id
     * @return
     */
    public boolean deleteMessage(long id){
        return mDb.delete(MESSAGE_TABLE_NAME, KEY_ID + "=" + id, null)>0;
    }

    /**
     * getAllMessages() method that used to get all messages from database
     * @return
     */
    public Cursor getAllMessages(){
        String[] fields = new String[]{KEY_ID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT, KEY_CATEGORY, KEY_LINK, KEY_TAGS, KEY_PUBDATE};
        return mDb.query(MESSAGE_TABLE_NAME, fields, null, null, null, null, "id desc");
    }

    /**
     * getMessage() method that used to get a Message by Id from database
     * @param id
     * @return
     */
    public Cursor getMessage(long id){
        Cursor cursor = mDb.query(MESSAGE_TABLE_NAME, new String[]{KEY_ID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT, KEY_CATEGORY, KEY_LINK, KEY_TAGS, KEY_PUBDATE}, KEY_ID+"="+id, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(MESSAGE_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS message");
            onCreate(db);
        }
    }


}
