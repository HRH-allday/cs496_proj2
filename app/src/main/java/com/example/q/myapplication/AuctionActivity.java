package com.example.q.myapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by q on 2017-01-03.
 */

public class AuctionActivity extends Activity {
    private Socket mSocket;
    private EditText mInputMessageView;
    private ListView mMessageView;
    private MessageViewAdapter mMessageAdapter;
    private ListView mUserView;
    private UserViewAdapter mUserAdapter;
    private String localurl = "http://143.248.49.213:3000";
    private String ec2url = "http://ec2-52-79-155-110.ap-northeast-2.compute.amazonaws.com:3000";
    private TextView auctionInfo;
    private Date startdate = new Date();
    private String productName;
    private String minPrice;
    {
        try {
            mSocket = IO.socket(ec2url);
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auction_main);
        /*
        Intent intent = getIntent();
        String roomName = intent.getStringExtra("roomName");
        */
        /* for testing */
        String roomName = "testroom";
        minPrice = "10000원";
        //SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        UserAccount ua = ((UserAccount) getApplication());

        auctionInfo = (TextView) findViewById(R.id.auctionInfo);

        new AlarmHATT(getApplicationContext()).Alarm();

        mMessageView = (ListView) findViewById(R.id.mChatList);
        mMessageAdapter = new MessageViewAdapter(this);
        mMessageView.setAdapter(mMessageAdapter);

        mUserView = (ListView) findViewById(R.id.mUserList);
        mUserAdapter = new UserViewAdapter(this);
        mUserView.setAdapter(mUserAdapter);

        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.connect();
        mSocket.emit("add user", ua.getGlobalVarValue());
        mSocket.emit("room", roomName);
        mInputMessageView = (EditText) findViewById(R.id.edit);
        Button b = (Button) findViewById(R.id.send);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }

    private void attemptSend() {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        mInputMessageView.setText("");
        mSocket.emit("new message", message);
    }
    private void addMessage(String username, String message){
        Message m = new Message();
        m.mMessage = message;
        m.mName = username;
        mMessageAdapter.addItem(m);
        mMessageAdapter.notifyDataSetChanged();
    }

    private void addUser(String username, String numUser){
        User u = new User();
        u.mName = username;
        mUserAdapter.addItem(u);
        mUserAdapter.notifyDataSetChanged();
    }

    public class AlarmHATT {
        private Context context;
        public AlarmHATT(Context context) {
            this.context=context;
        }
        public void Alarm() {
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(AuctionActivity.this, AuctionSet.class);

            PendingIntent sender = PendingIntent.getActivity(AuctionActivity.this, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            //알람시간 calendar에 set해주기

            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 16, 57, 0);

            //알람 예약
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }



    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String numUser;
                    try {
                        username = data.getString("username");
                        numUser = data.getString("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    addUser(username, numUser);
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0){
            Log.i("result","set");
            auctionInfo.setText("Price : "+minPrice);
        }
    }

    private class ViewHolder {
        public TextView mName;

        public TextView mMessage;
    }

    private class MessageViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<Message> mListData = new ArrayList<Message>();

        public MessageViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        public void resetList(){
            mListData.clear();
        }

        public void addItem(Message addInfo){
            mListData.add(addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.message, null);

                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mMessage = (TextView) convertView.findViewById(R.id.mMessage);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            Message mData = mListData.get(position);


            holder.mName.setText(mData.mName);
            holder.mMessage.setText(mData.mMessage);

            return convertView;
        }
    }

    private class ViewHolder2 {
        public TextView mName;

    }
    private class UserViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<User> mListData = new ArrayList<User>();

        public UserViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        public void resetList(){
            mListData.clear();
        }

        public void addItem(User addInfo){
            mListData.add(addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder2 holder;
            if (convertView == null) {
                holder = new ViewHolder2();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user, null);

                holder.mName = (TextView) convertView.findViewById(R.id.mName);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder2) convertView.getTag();
            }

            User mData = mListData.get(position);


            holder.mName.setText(mData.mName);

            return convertView;
        }
    }
}

