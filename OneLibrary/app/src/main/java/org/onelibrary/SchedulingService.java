package org.onelibrary;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.onelibrary.data.DbAdapter;
import org.onelibrary.data.MessageDataManager;
import org.onelibrary.data.MessageItem;

import java.util.List;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code AlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SchedulingService extends IntentService {

    public SchedulingService() {
        super("SchedulingService");
    }

    public static final String TAG = "SchedulingService";
    public final static String APP_STATUS = "app_status";
    public final static String STATUS_DATA_UPDATE = "data_update";

    NotificationManager mNotificationManager;
    SharedPreferences settings;
    SharedPreferences preferences = null;
    private MessageDataManager manager = null;

    @Override
    public void onCreate(){
        super.onCreate();
        preferences = getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String domain = settings.getString("server_address", "http://115.28.223.203:8080");
        manager = new MessageDataManager(getBaseContext(), domain);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "------onHandleIntent------");

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        ConnectivityManager cm = (ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){

            LocationService locationService = new LocationService(getBaseContext());
            locationService.getLastLocation();
            double longitude = locationService.getLongitude();
            double latitude  = locationService.getLatitude();

            DbAdapter mDbAdapter = DbAdapter.getInstance(getBaseContext());
            List<MessageItem> messageItems = manager.getRemoteMessages(getBaseContext(), longitude, latitude, 1, 1, 1);

            int size = messageItems.size();
            String title = getBaseContext().getString(R.string.notification_title);
            for (MessageItem item : messageItems){
                if(mDbAdapter.messageIsExist(item)){
                    size--;
                }else{
                    manager.addMessage(item);
                    Log.d(TAG, "------ send notification ------");
                    int notification_id = (int)(Math.random() * 10 + 1);
                    sendNotification(notification_id, title, item.getTitle());
                    Log.d(TAG, "------ notification: " + item.getTitle());
                }
            }

            if (size > 0) {
                if (preferences == null){
                    preferences = getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);
                }
                preferences.edit().putBoolean(STATUS_DATA_UPDATE, true).apply();
            }
        }

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }


    // Post a notification indicating whether a doodle was found.
    private void sendNotification(int notification_id, String title, String msg) {

        //两个参数,一个是key，就是在PreferenceActivity的xml中设置的,另一个是取不到值时的默认值
        Boolean new_notification = settings.getBoolean("notifications_new_message", true);
        Log.d(TAG, "---- new notification settings: " + new_notification + " ----");
        if (new_notification){
            Boolean notification_ringtone = settings.getBoolean("notifications_new_message_ringtone", true);
            Log.d(TAG, "---- notification ringtone settings: " + notification_ringtone + "------");
            Boolean notifications_vibrate = settings.getBoolean("notifications_new_message_vibrate",true);
            Log.d(TAG, "---- notification vibrate settings: " + notifications_vibrate + "------");

            int default_tip = Notification.DEFAULT_ALL;
            if(!notification_ringtone && !notifications_vibrate){
                default_tip = Notification.DEFAULT_LIGHTS;
            }else if(!notification_ringtone){
                default_tip = Notification.DEFAULT_VIBRATE;
            }else if(!notifications_vibrate){
                default_tip = Notification.DEFAULT_SOUND;
            }

            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            Log.d(TAG, "---- sendNotification: " + msg + " ----");

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(title)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setContentText(msg).setAutoCancel(true).setDefaults(default_tip);

            mBuilder.setContentIntent(contentIntent);
            Notification notification = mBuilder.build();
            mNotificationManager.notify(notification_id, notification);
        }
    }
 
    @Override
    public void onDestroy() {
        Intent intent = new Intent("org.onelibrary.scheduling.restart");
        sendBroadcast(intent);
        super.onDestroy();
    }
}
