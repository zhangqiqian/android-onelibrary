package org.onelibrary.util;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Network Adapter
 * Created by niko on 2/9/15.
 */
public class NetworkAdapter {

    public static final String TAG = "Network Connect";

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
        if(params != null && !params.isEmpty()){
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
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null){
                buffer.append(line);
            }
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

    public JSONObject parseJSON(String jsonString) {
        //Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            JSONObject result = new JSONObject(jsonString);
            return result;
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
        StringBuffer buffer = new StringBuffer();
        while ((line = reader.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }
}
