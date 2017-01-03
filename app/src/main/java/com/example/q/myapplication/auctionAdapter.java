package com.example.q.myapplication;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Q on 2017-01-03.
 */

public class auctionAdapter extends RecyclerView.Adapter <auctionAdapter.MyViewHolder>  {

    private JSONArray dataSet;
    private Bitmap received_image;
    private Tab3 currentFragment;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewPrice;
        TextView textViewDate;
        ImageView imageView;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.textViewDate = (TextView) itemView.findViewById(R.id.textViewDate);
            this.textViewPrice= (TextView) itemView.findViewById(R.id.textViewPrice);
            this.imageView = (ImageView) itemView.findViewById(R.id.ProductImage);
            this.cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }

    public auctionAdapter(JSONArray data, Bitmap bt, Tab3 ct) {
        this.dataSet = data;
        this.received_image = bt;
        this.currentFragment = ct;
    }

    public void addObject(JSONObject jobj){
        dataSet.put(jobj);
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.auction_item, parent, false);

        view.setOnClickListener(Tab3.auctionOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textViewName;
        TextView textViewDate = holder.textViewDate;
        TextView textViewPrice = holder.textViewPrice;
        ImageView imageView = holder.imageView;




        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    currentFragment.itemHandler((JSONObject)dataSet.get(listPosition));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
        try {
            JSONObject jobj= dataSet.getJSONObject(listPosition);
            Log.d ("flag1",jobj.getString("postname"));

            byte[] decodedString = Base64.decode(jobj.getString("img"), Base64.DEFAULT);

            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            int height = decodedByte.getHeight();
            int width = decodedByte.getWidth();
// Toast.makeText(this, width + " , " + height, Toast.LENGTH_SHORT).show();
            Bitmap resized = null;
            while (height > 80) {
                resized = Bitmap.createScaledBitmap(decodedByte, 80, 80, true);
                height = resized.getHeight();
                width = resized.getWidth();
            }

            textViewName.setText(jobj.getString("postname"));
            textViewDate.setText(jobj.getString("date"));
            textViewPrice.setText(jobj.getString("price")+"Won");


            imageView.setImageBitmap(decodedByte);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.length();
    }

}
