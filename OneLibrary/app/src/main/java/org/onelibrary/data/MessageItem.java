package org.onelibrary.data;

/**
 * Created by niko on 12/23/14.
 */
public class MessageItem {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String CATEGORY = "category";
    public static final String LINK = "link";
    public static final String PUBDATE = "pubdate";
    private int id = 0;
    private String title = null;
    private String content = null;
    private String link = null;
    private String category = null;
    private String pubdate = null;

    public MessageItem(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public String toString(){
        if (title.length() > 20){
            return title.substring(0, 42)+"...";
        }
        return title;
    }
}
