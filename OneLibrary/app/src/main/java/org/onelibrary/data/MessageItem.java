package org.onelibrary.data;

import java.util.Calendar;

/**
 * Created by niko on 12/23/14.
 */
public class MessageItem {

    public static final String ID = "id";
    public static final String PUBLISHID = "publish_id";
    public static final String MESSAGEID = "message_id";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";
    public static final String CATEGORY = "category";
    public static final String LINK = "link";
    public static final String TAGS = "tags";
    public static final String PUBDATE = "pubdate";
    public static final String STATUS = "status";
    public static final String CTIME = "ctime";

    private int id;
    private int publishId;
    private int messageId;
    private String title;
    private String author;
    private String content;
    private String category;
    private String link;
    private String tags;
    private String pubdate;
    private int status;
    private Calendar ctime;

    public MessageItem(){}

    public MessageItem(int publishId,
                       int messageId,
                       String title,
                       String author,
                       String content,
                       String category,
                       String link,
                       String tags,
                       String pubdate,
                       int status) {
        this.publishId = publishId;
        this.messageId = messageId;
        this.title = title;
        this.author = author;
        this.content = content;
        this.category = category;
        this.link = link;
        this.tags = tags;
        this.pubdate = pubdate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPublishId() {
        return publishId;
    }

    public void setPublishId(int publishId) {
        this.publishId = publishId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public int getStatus() {
        return status;
    }

    public Calendar getCtime() {
        return ctime;
    }

    public void setCtime(Calendar ctime) {
        this.ctime = ctime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String toString(){
        if (title.length() > 20){
            return title.substring(0, 20)+"...";
        }
        return title;
    }
}
