package com.lbl.groupreply;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

public class DeliveryService extends Service {
    public static boolean service_started = false;
    private BroadcastReceiver mDeliveryBroadcastReceiver;

    public DeliveryService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service_started = true;
        Log.i("LBL", "DeliveryService onCreate");
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Log.i("LBL", "DeliveryService onStartCommand");
        final int sendListSize = intent.getIntExtra("send_list_size", 0);
        mDeliveryBroadcastReceiver = new BroadcastReceiver() {
            int receiveCount = 0;
            @Override
            public void onReceive(Context context, Intent intent) {
                String address = intent.getStringExtra("address");
                String sms = intent.getStringExtra("body");
                long date = intent.getLongExtra("date", 0);
                String str_uri = intent.getStringExtra("uri");
                Uri uri = Uri.parse(str_uri);
                getContentResolver().delete(uri, "*", null);
                ContentValues values = new ContentValues();
                values.put("date", date);
                values.put("read", 0);
                values.put("type", 2);
                values.put("status", 0);
                values.put("address", address);
                values.put("body", sms);
                getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                receiveCount++;
                Log.i("LBL", "onReceive intent = " + intent.hashCode() + " context = " + context + " address = " + address
                    + "uri = " + uri.toString());

                if (receiveCount >= sendListSize) {
                    stopSelf();
                }
            }
        };
        this.registerReceiver(mDeliveryBroadcastReceiver, new IntentFilter("DELIVERED_SMS_ACTION"));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mDeliveryBroadcastReceiver);
        service_started = false;
        Log.i("LBL", "DeliveryService onDestroy");
    }
}
