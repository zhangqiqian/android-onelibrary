package org.onelibrary.util;

import android.os.Bundle;

/**
 * Created by niko on 10/15/15.
 */
public class LocationConverter {

    public static final double pi = 3.14159265358979324;
    public static final double a = 6378245.0;
    public static final double ee = 0.00669342162296594323;

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法，其中 convertGcj2Bd 将 GCJ-02 坐标转换成 BD-09 坐标， convertBd2Gcj 反之。
     *
     * @param gcjLat 火星坐标系（纬度）
     * @param gcjLon 火星坐标系（经度）
     * @return Bundle
     */
    public static Bundle convertGcj2Bd(double gcjLat, double gcjLon) {
        double z = Math.sqrt(gcjLon * gcjLon + gcjLat * gcjLat) + 0.00002 * Math.sin(gcjLat * pi);
        double theta = Math.atan2(gcjLat, gcjLon) + 0.000003 * Math.cos(gcjLon * pi);

        double baiduLon = z * Math.cos(theta) + 0.0065;
        double baiduLat = z * Math.sin(theta) + 0.006;
        Bundle location = new Bundle();
        location.putDouble("longitude", baiduLon);
        location.putDouble("latitude", baiduLat);
        return location;
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法，其中 convertGcj2Bd 将 GCJ-02 坐标转换成 BD-09 坐标， convertBd2Gcj 反之。
     *
     * @param bdLat 百度坐标系（纬度）
     * @param bdLon 百度坐标系（经度）
     * @return Bundle
     */
    public static Bundle convertBd2Gcj(double bdLat, double bdLon) {
        double x = bdLon - 0.0065;
        double y = bdLat - 0.006;

        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);

        double gcjLon = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);

        Bundle location = new Bundle();
        location.putDouble("longitude", gcjLon);
        location.putDouble("latitude", gcjLat);
        return location;
    }

    /**
     * 地球坐标系 (WGS-84) 到火星坐标系 (GCJ-02) 的转换算法，WGS-84 到 GCJ-02 的转换（即 GPS 加偏）算法
     * @param wgsLat 地球坐标系（纬度）
     * @param wgsLon 地球坐标系（经度）
     * @return Bundle
     */
    public static Bundle convertWgs2Gcj(double wgsLat, double wgsLon){
        Bundle location = new Bundle();
        if (outOfChina(wgsLat, wgsLon))
        {
            location.putDouble("longitude", wgsLon);
            location.putDouble("latitude", wgsLat);
            return location;
        }

        double dLat = transformLat(wgsLon - 105.0, wgsLat - 35.0);
        double dLon = transformLon(wgsLon - 105.0, wgsLat - 35.0);
        double radLat = wgsLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double lat = wgsLat + dLat;
        double lon = wgsLon + dLon;

        location.putDouble("longitude", lon);
        location.putDouble("latitude", lat);
        return location;
    }

    private static boolean outOfChina(double lat, double lon)
    {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    private static double transformLat(double x, double y)
    {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 地球坐标系 (WGS-84) 到百度坐标系 (BD-09) 的转换算法，WGS-84 到 BD-09 的转换算法
     * @param wgsLat 地球坐标系（纬度）
     * @param wgsLon 地球坐标系（经度）
     * @return Bundle
     */
    public static Bundle convertWgs2Bd(double wgsLat, double wgsLon){
        Bundle gcjLocation = convertWgs2Gcj(wgsLat, wgsLon);
        return convertGcj2Bd(gcjLocation.getDouble("latitude"), gcjLocation.getDouble("longitude"));
    }
}
