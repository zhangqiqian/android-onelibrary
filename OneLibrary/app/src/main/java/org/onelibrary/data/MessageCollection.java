package org.onelibrary.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by niko on 12/23/14.
 */
public class MessageCollection {

    private String pubdate = null;
    private List<MessageItem> itemList;

    public MessageCollection() {
        itemList = new ArrayList<MessageItem>(0);
    }

    public int addMessageItem(MessageItem item){
        itemList.add(item);
        return itemList.size();
    }

    public MessageItem getMessageItem(int location){
        return itemList.get(location);
    }

    public List<MessageItem> getAllMessageItems(){
        return itemList;
    }

    public List<Map<String, Object>> getAllItemsForListView(){
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put(MessageItem.ID, itemList.get(i).getId());
            item.put(MessageItem.MESSAGEID, itemList.get(i).getMessageId());
            item.put(MessageItem.TITLE, itemList.get(i).getTitle());
            item.put(MessageItem.AUTHOR, itemList.get(i).getAuthor());
            item.put(MessageItem.CONTENT, itemList.get(i).getContent());
            item.put(MessageItem.LINK, itemList.get(i).getLink());
            item.put(MessageItem.CATEGORY, itemList.get(i).getCategory());
            item.put(MessageItem.TAGS, itemList.get(i).getTags());
            item.put(MessageItem.PUBDATE, itemList.get(i).getPubdate());
            data.add(item);
        }
        return data;
    }

}
