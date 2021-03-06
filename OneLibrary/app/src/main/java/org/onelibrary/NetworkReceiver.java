package org.onelibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by niko on 5/29/15.
 */
public class NetworkReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                Log.d("NetworkReceiver", "onReceiver: Network Connected");
                alarm.setAlarm(context);
            }else{
                Log.d("NetworkReceiver", "onReceiver: Lost Connection");
                alarm.cancelAlarm(context);
            }
        }
    }

}
