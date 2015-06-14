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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.R;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A class that wraps database access and provides a cache for message.
 */
public class MessageDataManager {

    private static final String LOG_TAG = "MessageDataManager";

    private DbAdapter mDbAdapter;

    public final static String MAIN_INFO = "main_info";
    public final static String REQUEST_LAST_TIME = "last_time";
    public final static String REQUEST_LAST_PUBLISH_ID = "last_publish_id";
    public final static String REQUEST_LAST_LONGITUDE = "longitude";
    public final static String REQUEST_LAST_LATITUDE = "latitude";
    public final static String REQUEST_NEXT_START = "next_start";


    private final List<MessageItem> mMessagesList = new ArrayList<MessageItem>();
    private String domain;

    public MessageDataManager(Context context) {
        mDbAdapter = DbAdapter.getInstance(context);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        domain = settings.getString("server_address", "http://192.168.1.105");
        Log.d(LOG_TAG, "---- server domain settings: " + domain + " ----");
    }

    /**
     * Returns a list of Message
     */
    public final List<MessageItem> getMessageList() {
        synchronized (mMessagesList) {
            List<MessageItem> messages = mDbAdapter.getMessageList();
            mMessagesList.addAll(messages);
        }
        return mMessagesList;
    }

    /**
     * Returns a list of Message
     */
    public final MessageItem getMessage(int id) {
        synchronized (mMessagesList) {
            List<MessageItem> messages = mDbAdapter.getMessageList();
            for (MessageItem message : messages){
                if(message.getId() == id){
                    return message;
                }
            }
        }
        return null;
    }


    /**
     * Clears the data.
     */
    public final int clearMessages() {
        synchronized (mMessagesList) {
            mMessagesList.clear();
            return mDbAdapter.deleteAllMessages();
        }
    }

    /**
     * Adds a message to database and cache if it is a new message.
     */
    public final void addMessage(MessageItem item) {
        synchronized (mMessagesList) {
            mDbAdapter.insertMessage(item);
            mMessagesList.add(item);
        }
    }

    /**
     * Adds many messages to database and cache if it is a new message.
     */
    public final void addAllMessages(List<MessageItem> items) {
        synchronized (mMessagesList) {
            for (MessageItem item : items){
                mDbAdapter.insertMessage(item);
            }
            mMessagesList.addAll(items);
        }
    }

    /**
     * update a message to database and cache if it is a new message.
     */
    public final void updateMessage(MessageItem item) {
        synchronized (mMessagesList) {
            mDbAdapter.updateMessage(item);
            for(Iterator<MessageItem> it = mMessagesList.iterator();it.hasNext();){
                MessageItem message = it.next();
                if(message.getId() == item.getId()){
                    it.remove();
                }
            }
            mMessagesList.add(item);
        }
    }

    /**
     * delete a message from database and cache.
     */
    public final void removeMessage(MessageItem item) {
        synchronized (mMessagesList) {
            mDbAdapter.deleteMessage(item);
            for(Iterator<MessageItem> it = mMessagesList.iterator();it.hasNext();){
                MessageItem message = it.next();
                if(message.getId() == item.getId()){
                    it.remove();
                }
            }
        }
    }

    public List<MessageItem> getRemoteMessages(Context ctx, double longitude, double latitude){
        List<MessageItem> messageItems = new ArrayList<MessageItem>();
        try {
            NetworkAdapter adapter = new NetworkAdapter(ctx);
            SharedPreferences session = ctx.getSharedPreferences(MAIN_INFO, 0);
            long last_time = session.getLong(REQUEST_LAST_TIME, 0);
            long last_publish_id = session.getLong(REQUEST_LAST_PUBLISH_ID, 0);
            int next_start = session.getInt(REQUEST_NEXT_START, 0);

            long new_last_time = System.currentTimeMillis()/1000;
            if(new_last_time - last_time > 60){
                next_start = 0;
            }

            Bundle params = new Bundle();
            params.putString(REQUEST_LAST_TIME, String.valueOf(last_time));
            params.putString(REQUEST_LAST_PUBLISH_ID, String.valueOf(last_publish_id));
            params.putString(REQUEST_LAST_LONGITUDE, String.valueOf(longitude));
            params.putString(REQUEST_LAST_LATITUDE, String.valueOf(latitude));
            params.putString(REQUEST_NEXT_START, String.valueOf(next_start));

            Log.d(LOG_TAG, "Request params: " + params.toString());
            try{
                JSONObject result = adapter.request(domain + ctx.getString(R.string.get_messages_url), params);

                if(result != null && result.getInt("errno") == 0){
                    Log.d(LOG_TAG, "success to get messages");

                    int start = result.getInt("start");

                    JSONArray messagesArray = result.getJSONArray("result");
                    if(messagesArray.length() > 0){
                        for (int i = 0;i< messagesArray.length();i++){
                            JSONObject message = messagesArray.getJSONObject(i);
                            MessageItem item = new MessageItem();
                            item.setPublishId(message.getInt("publish_id"));
                            item.setMessageId(message.getInt("message_id"));
                            item.setTitle(message.getString("title"));
                            item.setAuthor("");
                            item.setContent("");
                            item.setCategory("");
                            item.setTags("");
                            item.setLink("");
                            item.setPubdate("");
                            item.setStatus(0);
                            item.setCtime(Calendar.getInstance());
                            messageItems.add(item);
                        }
                        session.edit().putInt(REQUEST_NEXT_START, start).putLong(REQUEST_LAST_TIME, new_last_time).apply();
                        Log.d(LOG_TAG, "new_last_time=" + new_last_time + " start=" + start);
                    }
                }else{
                    Log.d(LOG_TAG, "failure");
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return messageItems;
    }

    public MessageItem getMessageDetail(Context ctx, int id, int message_id){
        MessageItem item = null;
        try {
            NetworkAdapter adapter = new NetworkAdapter(ctx);

            Bundle params = new Bundle();
            params.putString("message_id", String.valueOf(message_id));

            Log.d(LOG_TAG, "Request params: " + params.toString());
            JSONObject result = adapter.request(domain + ctx.getString(R.string.get_message_detail_url), params);
            if(result != null && result.getInt("errno") == 0){
                Log.d(LOG_TAG, "success to get message detail: " + result.getString("result"));

                item = new MessageItem();
                JSONObject messageResult = result.getJSONObject("result");
                item.setId(id);
                item.setTitle(messageResult.getString("title"));
                item.setAuthor(messageResult.getString("author"));
                item.setContent(messageResult.getString("content"));
                item.setCategory(messageResult.getString("category"));
                item.setTags(messageResult.getString("tags"));
                item.setLink(messageResult.getString("link"));

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(messageResult.getLong("pubdate") *1000);
                String pub_date = format.format(date);
                item.setPubdate(pub_date);
                item.setStatus(1);
                item.setCtime(Calendar.getInstance());
            }else{
                Log.d(LOG_TAG, "failure");
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return item;
    }
}
