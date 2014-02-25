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
        super.onCreate();
        service_started = true;
        Log.i("LBL", "SendService onCreate");
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Log.i("LBL", "SendService Start");
        int i;
        String address;

        ArrayList<String> send_list = (ArrayList<String>) intent.getSerializableExtra("send_list");
        String sms = intent.getStringExtra("sms");

        SmsManager mSmsManager = SmsManager.getDefault();
        //PendingIntent mSendPI = PendingIntent.getActivity(this, 0, new Intent(), 0);
        for (i = 0; i < 1; i++) {
            long date = System.currentTimeMillis();
            address = send_list.get(send_count++);

            //Add sent message into database
            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("read", 0);
            values.put("type", 2);
            values.put("address", address);
            values.put("body", sms);
            Uri uri = getContentResolver().insert(Uri.parse("content://sms/sent"), values);

            ArrayList<String> messageArray = mSmsManager.divideMessage(sms);
            ArrayList<PendingIntent> mDeliverIntents = new ArrayList<PendingIntent>();
            for (int j = 0; j < messageArray.size(); j++) {
                Intent mDeliverIntent = new Intent("DELIVERED_SMS_ACTION");
                //Log.i("LBL", "" + mDeliverIntent.hashCode());
                mDeliverIntent.putExtra("address", address);
                mDeliverIntent.putExtra("date", date);
                mDeliverIntent.putExtra("body", sms);
                mDeliverIntent.putExtra("uri", uri.toString());
                PendingIntent mDeliverPI = PendingIntent.getBroadcast(this, 0,
                        mDeliverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mDeliverIntents.add(mDeliverPI);
            }

            mSmsManager.sendMultipartTextMessage(address, null, messageArray, null, mDeliverIntents);
            Log.i("LBL", "Sending " + address);

            Log.i("LBL", uri.toString());
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
        Log.i("LBL", "SendService onDestroy");
    }
}
