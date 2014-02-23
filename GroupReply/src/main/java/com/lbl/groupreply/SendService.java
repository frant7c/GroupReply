package com.lbl.groupreply;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

public class SendService extends Service {
    public static boolean service_started = false;
    private int send_count = 0;
    public SendService() {
    }

    @Override
    public void onCreate() {
        service_started = true;
        Log.i("LBL", "onCreate.....");
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Log.i("LBL", "Start.....");
        int i;
        ArrayList<String> send_list = (ArrayList<String>) intent.getSerializableExtra("sendlist");
        String sms = intent.getStringExtra("sms");
        String address = send_list.get(send_count++);
        SmsManager mSmsManager = SmsManager.getDefault();
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        for (i = 0; i < 1; i++) {
            //mSmsManager.sendTextMessage(address, null, sms, mPendingIntent, null);
            Log.i("LBL", "Sending " + address);
            //Add sent message into database
            /*ContentValues values = new ContentValues();
            values.put("date", System.currentTimeMillis());
            values.put("read", 0);
            values.put("type", 2);
            values.put("address", address);
            values.put("body", sms);
            getContentResolver().insert(Uri.parse("content://sms/sent"), values);*/
        }

        if (send_count == send_list.size()) {
            PendingIntent pendingIntent = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            MainActivity.mAlarmManager.cancel(pendingIntent);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service_started = false;
        Log.i("LBL", "onDestroy.....");
    }
}