package org.onelibrary.data;

/**
 * Created by niko on 12/23/14.
 */
public class MessageItem {

    public static final String ID = "id";
    public static final String MESSAGEID = "message_id";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";
    public static final String CATEGORY = "category";
    public static final String LINK = "link";
    public static final String TAGS = "tags";
    public static final String PUBDATE = "pubdate";
    private int id = 0;
    private int messageId = 0;
    private String title = null;
    private String author = null;
    private String content = null;
    private String category = null;
    private String link = null;
    private String tags = null;
    private String pubdate = null;

    public MessageItem(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String toString(){
        if (title.length() > 20){
            return title.substring(0, 20)+"...";
        }
        return title;
    }
}
