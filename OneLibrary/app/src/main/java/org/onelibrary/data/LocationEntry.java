package org.onelibrary.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by niko on 05/26/15.
 */
public class LocationEntry {

    private long id;
    private String name;
    private double latitude;
    private double longitude;
    private Calendar ctime;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CTIME = "ctime";

    public LocationEntry(String name, double longitude, double latitude, Calendar calendar) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.ctime = calendar;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Calendar getCtime() {
        return ctime;
    }

    public void setCtime(Calendar ctime) {
        this.ctime = ctime;
    }

    public String toString(){
        if(name.isEmpty()){
            return "latitude: "+latitude+", longitude: "+longitude;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        return fmt.format(ctime.getTime())+": "+longitude+","+latitude+" "+name;
    }
}
