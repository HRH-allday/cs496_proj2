package com.example.q.myapplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by q on 2017-01-03.
 */

public class AuctionSet extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Intent intent = getIntent();
        String auctionName = intent.getStringExtra("name");
        */
        Log.i("entered alarm","hi");

        NotificationManager notificationManager= (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.on).setTicker("HETT").setWhen(System.currentTimeMillis())
                .setNumber(1).setContentTitle("푸쉬 제목").setContentText("푸쉬내용")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setAutoCancel(true).setOngoing(true);

        notificationManager.notify(1, builder.build());


        setResult(0);


    }
}
