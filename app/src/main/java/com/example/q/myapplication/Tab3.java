package com.example.q.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class Tab3 extends Fragment {

    static final int REQUEST_FOR_REGISTRATION = 110;
    static final int REQUEST_FOR_AUCTION = 120;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView auctionView;
    //    private static ArrayList<DataModel> data;
    static View.OnClickListener auctionOnClickListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.tab3, container, false);

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


            adapter = new auctionAdapter(result, null);
            auctionView.setAdapter(adapter);

//


        }
    }
}




