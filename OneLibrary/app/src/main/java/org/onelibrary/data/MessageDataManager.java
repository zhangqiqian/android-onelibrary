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
import java.util.Iterator;
import java.util.List;

/**
 * A class that wraps database access and provides a cache for message.
 */
public class MessageDataManager {

    private DbAdapter mDbAdapter;

    private final List<MessageItem> mMessagesList = new ArrayList<MessageItem>();

    public MessageDataManager(DbAdapter dbAdapter) {
        mDbAdapter = dbAdapter;
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
}
