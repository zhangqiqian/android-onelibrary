package org.onelibrary.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;


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
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "message_id INTEGER not null, " +
                    "title TEXT not null, " +
                    "author TEXT not null, " +
                    "content TEXT not null, " +
                    "category TEXT not null, " +
                    "link TEXT, " +
                    "tags TEXT, " +
                    "pubdate TEXT);";

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
     * @param item MessageItem
     * @return
     */
    public long createMessage(MessageItem item){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MESSAGE_ID, item.getMessageId());
        initialValues.put(KEY_TITLE, item.getTitle());
        initialValues.put(KEY_AUTHOR, item.getAuthor());
        initialValues.put(KEY_CONTENT, item.getContent());
        initialValues.put(KEY_CATEGORY, item.getCategory());
        initialValues.put(KEY_LINK, item.getLink());
        initialValues.put(KEY_TAGS, item.getTags());
        initialValues.put(KEY_PUBDATE, item.getPubdate());

        return mDb.insert(MESSAGE_TABLE_NAME, "tags", initialValues);
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
        String[] fields = new String[]{KEY_ID, KEY_MESSAGE_ID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT, KEY_CATEGORY, KEY_LINK, KEY_TAGS, KEY_PUBDATE};
        return mDb.query(MESSAGE_TABLE_NAME, fields, null, null, null, null, "id desc");
    }

    /**
     * getMessage() method that used to get a Message by Id from database
     * @param id
     * @return
     */
    public Cursor getMessage(long id){
        Cursor cursor = mDb.query(MESSAGE_TABLE_NAME, new String[]{KEY_ID, KEY_MESSAGE_ID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT, KEY_CATEGORY, KEY_LINK, KEY_TAGS, KEY_PUBDATE}, KEY_ID+"="+id, null, null, null, null);
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
