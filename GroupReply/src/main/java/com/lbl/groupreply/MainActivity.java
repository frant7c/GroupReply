package com.lbl.groupreply;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

class SMS implements Serializable {
    int intType = -1;
    long time = 0;
    String strBody = null;
}

class Conversation{
    String strName = null;
    String strLatestSMS = null;
    String strAddress = null;
    ArrayList<SMS> lstSMSList;
}

class ConversationListAdapter extends BaseAdapter {
    private HashMap<String, Conversation> mapItems;
    private LayoutInflater mInflater;
    HashMap<Integer,View> hmListViewMap;
    String[] postion_array;
    static HashMap<String, Boolean> send_map = new HashMap<String, Boolean>();

    class ViewHolder{
        TextView tvName = null;
        TextView tvSMS = null;
        CheckBox ckbCheck = null;
    }

    public ConversationListAdapter(HashMap<String, Conversation> mapConversations, String[] position,
                                   Context context){
        mapItems = mapConversations;
        mInflater = LayoutInflater.from(context);
        hmListViewMap = new HashMap<Integer,View>();
        postion_array = position;
    }

    @Override
    public int getCount() {
        return mapItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mapItems.get(postion_array[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vView1;
        final ViewHolder vhHolder;

        //Log.i("LBL", "" + intCount++);
        if (hmListViewMap.get(position) == null) {
            vView1 = mInflater.inflate(R.layout.relative, null);
            vhHolder = new ViewHolder();
            assert vView1 != null;
            vhHolder.tvName = (TextView) vView1.findViewById(R.id.textView1);
            vhHolder.tvSMS = (TextView) vView1.findViewById(R.id.textView2);
            vhHolder.ckbCheck = (CheckBox) vView1.findViewById(R.id.checkBox1);
            hmListViewMap.put(position, vView1);
            final Conversation cvConversation = mapItems.get(postion_array[position]);
            vhHolder.tvName.setText(cvConversation.strName);
            vhHolder.tvSMS.setText(cvConversation.strLatestSMS);

            vhHolder.ckbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if(isChecked){
                        vhHolder.ckbCheck.setChecked(true);
                        send_map.put(cvConversation.strAddress, false);
                        Log.i("LBL", position + " has been checked!");
                    }else{
                        send_map.remove(cvConversation.strAddress);
                        vhHolder.ckbCheck.setChecked(false);
                        Log.i("LBL", position + " has been unchecked!");
                    }
                }

            });
            vView1.setTag(vhHolder);
        }else{
            vView1 = hmListViewMap.get(position);
        }

        return vView1;
    }

}

public class MainActivity extends Activity {
    public static AlarmManager mAlarmManager;
    String[] position;
    HashMap<String, Conversation> mapConversations;
    Conversation mConversation;
    SMS mSMS;
    HashMap<String, String> mapNum2Name;

    private HashMap<String, Conversation> getSmsInPhone() {
        final String SMS_URI_ALL = "content://sms/";
        //final String SMS_URI_INBOX = "content://sms/inbox";
        mapConversations = new HashMap<String, Conversation>();
        position = new String[500];
        Cursor cursor;
        int iCount = 0;

        Uri uri = Uri.parse(SMS_URI_ALL);
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        cursor = getContentResolver().query(uri, projection, null, null, "date desc");
        if (cursor != null) {
            dumpNumber();
            try {
                if(cursor.moveToFirst()) {
                    //int index_Address = cursor.getColumnIndex("address");
                    int index_Address = 1;
                    //int index_Person = cursor.getColumnIndex("person");
                    //int index_Body = cursor.getColumnIndex("body");
                    int index_Body = 3;
                    //int index_type = cursor.getColumnIndex("type");
                    int index_type = 5;
                    //int index_Date = cursor.getColumnIndex("date");
                    int index_Date = 4;
                    //Log.i("LBL", "" + index_Date);
                    do{
                        String strAddress = handleNumber(cursor.getString(index_Address));
                        if (strAddress == null) {
                            continue;
                        }
                        String strName;
                        String strBody = cursor.getString(index_Body);
                        int intType = cursor.getInt(index_type);
                        long longDate = cursor.getLong(index_Date);
                        strName = mapNum2Name.get(strAddress);
                        if(strName == null){
                            strName = strAddress;
                        }
                        if(!mapConversations.containsKey(strName)){
                            mConversation = new Conversation();
                            mConversation.strName = strName;
                            mConversation.strLatestSMS = strBody;
                            mConversation.strAddress = strAddress;
                            mConversation.lstSMSList = new ArrayList<SMS>();
                            mSMS = new SMS();
                            mSMS.strBody = strBody;
                            mSMS.intType = intType;
                            mSMS.time = longDate;
                            mConversation.lstSMSList.add(mSMS);
                            mapConversations.put(strName, mConversation);
                            position[iCount++] = strName;
                        } else {
                            mConversation = mapConversations.get(strName);
                            mSMS = new SMS();
                            mSMS.strBody = strBody;
                            mSMS.intType = intType;
                            mSMS.time = longDate;
                            mConversation.lstSMSList.add(mSMS);
                        }
                    }while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return mapConversations;
    }

    private String handleNumber(String strNum){
        String tmp;
        tmp = strNum.replace(" ", "");
        if(tmp.charAt(0) == '+') {
            if (tmp.charAt(1) != '8' || tmp.charAt(2) != '6') {
                return null;
            }
            return tmp.substring(3);
        }
        return tmp;
    }

    private void dumpNumber(){
        assert ContactsContract.CommonDataKinds.Phone.CONTENT_URI != null;
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String strName;
        String strNumber;
        mapNum2Name = new HashMap<String, String>();
        try {
            if(cursor != null && cursor.moveToFirst()){
                int intNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int intNumIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                do{
                    strName = cursor.getString(intNameIndex);
                    strNumber = handleNumber(cursor.getString(intNumIndex));
                    if (strNumber == null) {
                        continue;
                    }
                    mapNum2Name.put(strNumber, strName);
                }while(cursor.moveToNext());
            }
        } finally {
            assert cursor != null;
            cursor.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long startTime = System.nanoTime();
        final HashMap<String, Conversation> mapConversations = getSmsInPhone();
        long consumingTime = System.nanoTime() - startTime;
        Log.i("LBL", "Used " + consumingTime/1000 + "us");

        ConversationListAdapter myListAdapter = new ConversationListAdapter(mapConversations, position,
                this);
        ListView myList = (ListView)findViewById(R.id.mylist);
        myList.setAdapter(myListAdapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("LBL", i + " has been selected!");
                Conversation cvConversation = mapConversations.get(position[i]);
                /*for (SMS sms : cvConversation.lstSMSList) {
                    Log.i("LBL", sms.intType + " " + sms.strBody);
                }*/
                Intent mIntent = new Intent(MainActivity.this, ConversationActivity.class);
                Bundle mData = new Bundle();
                mData.putSerializable("smslist", cvConversation.lstSMSList);
                mIntent.putExtras(mData);
                startActivity(mIntent);
            }
        });

        Button btnButton1 = (Button)findViewById(R.id.button1);
        btnButton1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (SendService.service_started) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("确认重发")
                            .setMessage("eggbht")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (sendSMS() == 0) {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("正在发送")
                                                .setMessage("fdfdsf")
                                                .setPositiveButton("确定", null)
                                                .create()
                                                .show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                } else {

                    if (sendSMS() == 0) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("正在发送")
                                .setMessage("fdfdsf")
                                .setPositiveButton("确定", null)
                                .create()
                                .show();
                    }
                }
            }
        });
    }

    private int sendSMS() {
        if (ConversationListAdapter.send_map.size() == 0) {
            Toast.makeText(MainActivity.this,
                    "请选择收信人。",
                    Toast.LENGTH_SHORT).show();
            return -1;
        }

        EditText etEditText1 = (EditText)findViewById(R.id.editText1);
        String strSMS = etEditText1.getText().toString();

        if(strSMS.equals("")){
            Toast.makeText(MainActivity.this,
                    "请输入短信内容。",
                    Toast.LENGTH_SHORT).show();
            return -2;
        }

        if (SendService.service_started) {
            Intent mSendIntent = new Intent(MainActivity.this, SendService.class);
            PendingIntent mPendingIntent = PendingIntent.getService(MainActivity.this, 0,
                    mSendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.cancel(mPendingIntent);
            stopService(mSendIntent);
        }

        if (DeliveryService.service_started) {
            Intent mDeliveryIntent = new Intent(MainActivity.this, DeliveryService.class);
            stopService(mDeliveryIntent);
        }

        mAlarmManager = (AlarmManager) MainActivity.this
                .getSystemService(MainActivity.ALARM_SERVICE);

        //包装需要执行Service的Intent
        Intent mSendIntent = new Intent(MainActivity.this, SendService.class);
        ArrayList<String> send_list = new ArrayList<String>();
        for(String strAddress : ConversationListAdapter.send_map.keySet()){
            send_list.add(strAddress);
        }

        mSendIntent.putExtra("send_list", send_list);
        mSendIntent.putExtra("sms", strSMS);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0,
                mSendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //触发服务的起始时间
        long triggerAtTime = SystemClock.elapsedRealtime();

        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                20 * 1000, pendingIntent);

        Intent mDeliveryIntent = new Intent(MainActivity.this, DeliveryService.class);
        mDeliveryIntent.putExtra("send_list_size", ConversationListAdapter.send_map.size());
        startService(mDeliveryIntent);

        etEditText1.setText("");
        return 0;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i("LBL", "-------onResume------");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.i("LBL", "-------onPause------");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.i("LBL", "-------onStop------");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ConversationListAdapter.send_map.clear();
        Log.i("LBL", "-------onDestroy------");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
