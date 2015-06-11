package org.onelibrary;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    NotificationManager mNotificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "------onHandleIntent------");
        LocationService locationService = new LocationService(getBaseContext());
        double longitude = locationService.getLongitude();
        double latitude  = locationService.getLatitude();

        DbAdapter mDbAdapter = DbAdapter.getInstance(getBaseContext());
        MessageDataManager manager = new MessageDataManager(getBaseContext());
        List<MessageItem> messageItems = manager.getRemoteMessages(getBaseContext(), longitude, latitude);

        int size = messageItems.size();
        String content = getBaseContext().getString(R.string.notification_content);
        for (MessageItem item : messageItems){
            if(mDbAdapter.messageIsExist(item)){
                size--;
            }else{
                manager.addMessage(item);
                content = item.getTitle();
            }
        }

        String msg;
        String title = getBaseContext().getString(R.string.notification_title);
        if (size > 0) {
            Log.d(TAG, "------ send notification ------");
            if(size == 1){
                int notification_id = (int)(Math.random() * 10 + 1);
                msg = content;
                sendNotification(notification_id, title, msg);
            }else{
                msg = "You have " + size + "new messages.";
                sendNotification(NOTIFICATION_ID, title, msg);
            }
            Log.d(TAG, msg);
        }

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }


    // Post a notification indicating whether a doodle was found.
    private void sendNotification(int notification_id, String title, String msg) {
        mNotificationManager = (NotificationManager)
               this.getSystemService(Context.NOTIFICATION_SERVICE);
    
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Log.d(TAG, "------sendNotification: " + msg + "------");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        mNotificationManager.notify(notification_id, notification);
    }
 
    @Override
    public void onDestroy() {
        Intent intent = new Intent("org.onelibrary.scheduling.restart");
        sendBroadcast(intent);
        super.onDestroy();
    }
}
