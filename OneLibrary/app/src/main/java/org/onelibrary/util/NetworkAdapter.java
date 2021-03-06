package org.onelibrary.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

/**
 * Network Adapter
 * Created by niko on 2/9/15.
 */
public class NetworkAdapter {

    public static final String TAG = "Network Connect";
    public final static String SESSION_INFO = "session_info";
    public static final String PHPSESSID = "phpsessid";

    private SharedPreferences preferences = null;

    public NetworkAdapter(Context mContext){
        preferences = mContext.getSharedPreferences(SESSION_INFO, 0);
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws java.io.IOException
     */
    public JSONObject request(String urlString, Bundle params) throws IOException {

        //new connection
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        String sessionId = preferences.getString(PHPSESSID, null);
        if(sessionId != null) {
            conn.setRequestProperty("Cookie", sessionId);
        }
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        conn.connect();
        //add post params
        OutputStream os = conn.getOutputStream();
        DataOutputStream dataos = new DataOutputStream(os);
        Set<String> keys = params.keySet();

        StringBuilder sb = new StringBuilder();
        if(!params.isEmpty()){
            for (String key : keys){
                String value = params.getString(key);
                sb.append(key).append('=')
                        .append(URLEncoder.encode(value, "UTF-8")).append('&');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        dataos.writeBytes(sb.toString());
        dataos.flush();
        dataos.close();

        if(conn.getResponseCode() == 200){
            // Start the query
            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            StringBuilder buffer = new StringBuilder();
            while ((line = reader.readLine()) != null){
                buffer.append(line);
            }
            String cookie = conn.getHeaderField("set-cookie");
            if(cookie != null) {
                sessionId = cookie.substring(0, cookie.indexOf(";"));
                preferences.edit().putString(PHPSESSID, sessionId).apply();
            }
            conn.disconnect();
            return parseJSON(buffer.toString());
        }else{
            JSONObject map = new JSONObject();
            try {
                map.put("errno", 1);
                map.put("errmsg", "Server Error. Return code: " + conn.getResponseCode());
            }catch (JSONException e){
                e.printStackTrace();
            }
            return map;
        }
    }

    public JSONObject get(String url, Bundle params) throws IOException {
        JSONObject result = new JSONObject();
        InputStream stream = null;
        try {
            Set<String> keys = params.keySet();
            StringBuilder sb = new StringBuilder();
            if(!params.isEmpty()){
                for (String key : keys){
                    String value = params.getString(key);
                    sb.append(key).append('=').append(URLEncoder.encode(value, "UTF-8")).append('&');
                }
                sb.deleteCharAt(sb.length()-1);
                if(url.contains("?")){
                    url = url+"&"+sb.toString();
                }else{
                    url = url+"?"+sb.toString();
                }
            }
            Log.i("Network", url);
            stream = requestUrl(url);
            String str = readIt(stream);
            result = parseJSON(str);
        }catch (Exception e){
            try {
                result.put("error", 1);
                result.put("errmsg", "Server Error");
            }catch (JSONException err){
                err.printStackTrace();
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return result;
    }

    public JSONObject parseJSON(String jsonString) {
        //Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /** Initiates the fetch operation. */
    public String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str = "";

        try {
            stream = requestUrl(urlString);
            str = readIt(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws java.io.IOException
     */
    private InputStream requestUrl(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;

    }

    /** Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from targeted site.
     * @return String concatenated according to len parameter.
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     */
    private String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }
}
