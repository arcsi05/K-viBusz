package com.aron.kvibusz;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import static com.aron.kvibusz.App.CHANNEL_ID;

public class activeBusService extends Service {
    public activeBusService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = intent.getStringExtra("title");
        String label = intent.getStringExtra("label");
        long when = intent.getLongExtra("when", System.currentTimeMillis() + 60000);
//        Log.i("intentdebug", String.valueOf(when));
//        Log.i("timedebug",when+ " ms (" + String.valueOf(when).length()+")");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(label)
                .setSmallIcon(R.drawable.ic_departure_board_24px)
                .setContentIntent(pendingIntent)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(System.currentTimeMillis() + when * 60000)
                .setTimeoutAfter(0)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
