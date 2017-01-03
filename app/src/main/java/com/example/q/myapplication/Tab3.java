package com.example.q.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class Tab3 extends Fragment {

    private Socket mSocket;
    static final int REQUEST_FOR_REGISTRATION = 110;
    static final int REQUEST_FOR_AUCTION = 120;
    private static auctionAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView auctionView;
    private String roomDate;
    //    private static ArrayList<DataModel> data;
    private String ec2url = "http://ec2-52-79-155-110.ap-northeast-2.compute.amazonaws.com:3000";
    static View.OnClickListener auctionOnClickListener;

    {
        try {
            mSocket = IO.socket(ec2url);
        } catch (URISyntaxException e) {
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.tab3, container, false);

        mSocket.on("test", onTest);
        mSocket.connect();
        auctionOnClickListener = new MyOnClickListener(getContext());

        auctionView = (RecyclerView) view.findViewById(R.id.auction_view);

        layoutManager = new LinearLayoutManager(getActivity());
        auctionView.setLayoutManager(layoutManager);
        auctionView.setItemAnimator(new DefaultItemAnimator());


        createAsyncTask da = new createAsyncTask();
        da.execute();


        JSONObject testing = new JSONObject();
        try {
            testing.put("name", "sdfsdf");
            testing.put("date", "2016-07-12");
            testing.put("price", "2000");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray jar = new JSONArray();
        jar.put(testing);


        Button register_button = (Button) view.findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Registration.class);
                startActivityForResult(intent, REQUEST_FOR_REGISTRATION);
            }
        });

        Button test_button = (Button) view.findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AuctionActivity.class);
                startActivityForResult(intent, REQUEST_FOR_AUCTION);
            }
        });


        return view;


    }
    private Emitter.Listener onTest = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String name;
                    String date;
                    String price;
                    JSONObject testing = new JSONObject();
                    try {
                        testing.put("date", data.getString("price"));
                        testing.put("name", data.getString("name"));
                        testing.put("price", data.getString("date"));
                    } catch (JSONException e) {
                        return;
                    }

                    createAsyncTask da = new createAsyncTask();
                    da.execute();
                }
            });
        }
    };

    private Emitter.Listener onNewPrice2 = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String name;
                    String price;
                    JSONObject testing = new JSONObject();
                    try {
                        name = data.getString("name");
                        price = data.getString("date");
                    } catch (JSONException e) {
                        return;
                    }

                    adapter.addObject(testing);
                }
            });
        }
    };

    private Emitter.Listener onGetTime = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String date;
                    JSONObject testing = new JSONObject();
                    try {
                        date = data.getString("date");
                    } catch (JSONException e) {
                        return;
                    }

                    roomDate = date;
                }
            });
        }
    };

    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultcode, Intent Data) {
        super.onActivityResult(requestCode, resultcode, Data);
        if (requestCode == REQUEST_FOR_REGISTRATION) {

            if (resultcode == 1) {

                Toast.makeText(getContext(), "Activity registration worked", Toast.LENGTH_LONG);
                try {

                    Log.d("result_joject", Data.getExtras().getString("result"));
                    JSONObject resultObject = new JSONObject(Data.getExtras().getString("result"));
                    mSocket.emit("create room", resultObject.getString("_id"), resultObject.getString("price"), resultObject.getString("date"), resultObject.getString("postname"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getContext(), "Activity registration worked", Toast.LENGTH_LONG);
            } else if (requestCode == REQUEST_FOR_AUCTION) {
                Toast.makeText(getContext(), "Auction worked", Toast.LENGTH_LONG);
            }


        }


    }


    public class createAsyncTask extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... params) {

            try {
                URL u;
                u = new URL("http://52.79.155.110:3000/call_auction_list");
                Log.d("connected2", "sasasasas");
                HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                Log.d("open", "");
                huc.setRequestMethod("POST");
                huc.setDoInput(true);
                huc.setDoOutput(true);
                huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                huc.connect();

                int status = huc.getResponseCode();

                InputStream is = null;

                if (status > 400) {

                    is = huc.getErrorStream();
                } else {
                    is = huc.getInputStream();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();

                String response = new String(byteData);


                JSONArray receiving = new JSONArray(response);
                Log.d("sdfsdfsdf", response);


                for (int i = 0; i < receiving.length(); i++) {

                    Log.d("JSONname", receiving.getJSONObject(i).getString("postname"));
                }


                is.close();
                return receiving;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;

        }

        protected void onPostExecute(final JSONArray result) {


            adapter = new auctionAdapter(result, null, Tab3.this);
            auctionView.setAdapter(adapter);

//


        }
    }

    public void itemHandler(JSONObject jobj){
        try {
            String startDate = jobj.getString("date");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date now = Calendar.getInstance().getTime();
            Date startD = Calendar.getInstance().getTime();;
            try {
                startD = format.parse( startDate );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int compare = now.compareTo(startD);
            if(compare < 0){
                Intent intent = new Intent(this.getActivity(), AuctionActivity.class);
                String idname = jobj.getString("_id");
                //mSocket.emit("get time", idname);

                intent.putExtra("startDate", jobj.getString("date"));
                intent.putExtra("roomName", jobj.getString("_id"));
                intent.putExtra("startDate", jobj.getString("date"));
                intent.putExtra("minPrice", jobj.getString("price"));
                startActivity(intent);
            }



        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}




