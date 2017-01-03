package com.example.q.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.example.q.myapplication.R.anim.auction_start;
import static com.example.q.myapplication.R.anim.auction_start_reverse;
import static com.example.q.myapplication.R.id.price;

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
    private String startdate;
    private String productName;
    private String minPrice;
    private String userName;
    private Boolean isNewbie = true;
    LinearLayout mRevealView;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab;
    private Animation fab_start, fab_end;
    boolean hidden=true;
    private Date startD;
    private EditText priceEdit;
    private Button sendbtn;
    private String currentPrice;



    {
        try {
            mSocket = IO.socket(ec2url);
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auction_main);

        Intent intent = getIntent();
        String roomName = intent.getStringExtra("roomName");
        startdate = intent.getStringExtra("startDate");
        minPrice = intent.getStringExtra("minPrice");
        currentPrice = minPrice;
        /* for testing
        String roomName = "testroom";
        minPrice = "10000원";
        //SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

*/

        auctionInfo = (TextView) findViewById(R.id.auctionInfo);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date now = Calendar.getInstance().getTime();
        try {
            startD = format.parse( startdate );
            Log.i("date",format.format(startD));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int compare = now.compareTo(startD);
        if(compare < 0){
            auctionInfo.setText("경매시작 전 입니다");
        }
        else{
            auctionInfo.setText("Price : "+minPrice);
        }
        UserAccount ua = ((UserAccount) getApplication());
        userName = ua.getGlobalVarValue();



        new AlarmHATT(getApplicationContext()).Alarm();
        priceEdit = (EditText) findViewById(R.id.price_set);
        sendbtn = (Button) findViewById(R.id.price_send);
        mMessageView = (ListView) findViewById(R.id.mChatList);
        mMessageAdapter = new MessageViewAdapter(this);
        mMessageView.setAdapter(mMessageAdapter);

        mUserView = (ListView) findViewById(R.id.mUserList);
        mUserAdapter = new UserViewAdapter(this);
        mUserView.setAdapter(mUserAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);

        fab_start = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_start);
        fab_end = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_end);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // finding X and Y co-ordinates
                int cx = (mRevealView.getLeft() + mRevealView.getRight());
                int cy = (mRevealView.getTop());

                // to find  radius when icon is tapped for showing layout
                int startradius=0;
                int endradius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

                // performing circular reveal when icon will be tapped
                Animator animator = ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, startradius, endradius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(200);

                //reverse animation
                // to find radius when icon is tapped again for hiding layout
                //  starting radius will be the radius or the extent to which circular reveal animation is to be shown

                int reverse_startradius = Math.max(mRevealView.getWidth(),mRevealView.getHeight());

                //endradius will be zero
                int reverse_endradius=0;

                // performing circular reveal for reverse animation
                Animator animate = ViewAnimationUtils.createCircularReveal(mRevealView,cx,cy,reverse_startradius,reverse_endradius);
                if(hidden){

                    // to show the layout when icon is tapped
                    mRevealView.setVisibility(View.VISIBLE);
                    animator.start();
                    hidden = false;
                }
                else {
                    mRevealView.setVisibility(View.VISIBLE);

                    // to hide layout on animation end
                    animate.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRevealView.setVisibility(View.INVISIBLE);
                            hidden = true;
                        }
                    });
                    animate.start();
                }
            }

        });

        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("auction start", onAuctionStart);
        mSocket.on("to newbie", onToNewbie);
        mSocket.on("new price", onNewPrice);
        mSocket.on("user left", onUserLeft);
        mSocket.connect();
        mSocket.emit("room", roomName);
        mSocket.emit("add user", userName);
        mInputMessageView = (EditText) findViewById(R.id.edit);
        Button b = (Button) findViewById(R.id.send);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceEdit.getText().toString().trim();

                if(Integer.parseInt(currentPrice) > Integer.parseInt(price) | TextUtils.isEmpty(price)){
                    Toast.makeText(getApplicationContext(),"현재 입찰 가격보다 낮습니다!", Toast.LENGTH_LONG).show();
                }
                else mSocket.emit("deal", price);

                priceEdit.setText("");

                Log.i("how much", price);
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

    private void addUser(String username){
        User u = new User();
        u.mName = username;
        mUserAdapter.addItem(u);
        mUserAdapter.notifyDataSetChanged();
    }
    private void removeUser(String username){
        mUserAdapter.removeUser(username);
        mUserAdapter.notifyDataSetChanged();
    }

    private void checkUser(String username){
        if(!mUserAdapter.checkName(username)) addUser(username);
    }

    private void setPrice(String price){
        currentPrice = price;
        auctionInfo.setText("Price : "+price);
    }

    private void startAuction(String startdate){
        TextView tv = (TextView) findViewById(R.id.timer);
        Animation start= AnimationUtils.loadAnimation(getApplicationContext(), auction_start);
        Animation start_reverse= AnimationUtils.loadAnimation(getApplicationContext(), auction_start_reverse);

        auctionInfo.setText("경매가 시작됩니다!");

        tv.startAnimation(start);
        tv.startAnimation(start_reverse);
        tv.setText("2");
        tv.startAnimation(start);
        tv.startAnimation(start_reverse);
        tv.setText("1");
        tv.startAnimation(start);
        tv.startAnimation(start_reverse);
        tv.setText("START!");
        tv.startAnimation(start);
        tv.startAnimation(start_reverse);

        auctionInfo.setText("Price : "+minPrice);

    }

    public class AlarmHATT {
        private Context context;
        public AlarmHATT(Context context) {
            this.context=context;
        }
        public void Alarm() {
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(AuctionActivity.this, BroadcastD.class);

            PendingIntent sender = PendingIntent.getBroadcast(AuctionActivity.this, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            //알람시간 calendar에 set해주기

            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), startD.getHours(), startD.getMinutes(), startD.getSeconds());

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

                    mSocket.emit("to newbie", userName);

                    // add the message to view
                    if(!isNewbie) addUser(username);
                    else isNewbie = false;
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
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
                    removeUser(username);
                }
            });
        }
    };

    private Emitter.Listener onToNewbie = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                     checkUser(username);
                }
            });
        }
    };

    private Emitter.Listener onNewPrice = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String price;
                    try {
                        price = data.getString("price");
                        Log.i("price", price);
                    } catch (JSONException e) {
                        return;
                    }

                    setPrice(price);
                }
            });
        }
    };

    private Emitter.Listener onAuctionStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String startDate;
                    try {
                        startDate = data.getString("startDate");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    startAuction(startDate);
                }
            });
        }
    };



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
        public boolean checkName(String nameC){
            for(User u : mListData){
                if(u.mName == nameC) return true;
            }
            return false;
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

        public void removeUser(String usernaem){
            for(int i = 0; i < mListData.size() ; i++){
                if(mListData.get(i).mName == usernaem) {
                    mListData.remove(i);
                    return;
                }

            }

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

