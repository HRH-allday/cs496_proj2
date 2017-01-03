package com.example.q.myapplication;


import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;


/**
 * Created by Q on 2017-01-03.
 */

public class auctionAdapter extends RecyclerView.Adapter <auctionAdapter.MyViewHolder>  {

    private JSONArray dataSet;
    private Bitmap received_image;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewPrice;
        TextView textViewDate;
        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.textViewDate = (TextView) itemView.findViewById(R.id.textViewDate);
            this.textViewPrice= (TextView) itemView.findViewById(R.id.textViewPrice);
            this.imageView = (ImageView) itemView.findViewById(R.id.ProductImage);

        }
    }
    public auctionAdapter(JSONArray data, Bitmap bt) {
        this.dataSet = data;
        this.received_image= bt;

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

        try {
            textViewName.setText(dataSet.getJSONObject(listPosition).getString("postname"));
            textViewDate.setText(dataSet.getJSONObject(listPosition).getString("date"));
            textViewPrice.setText(dataSet.getJSONObject(listPosition).getString("price"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(this.received_image);
    }

    @Override
    public int getItemCount() {
        return dataSet.length();
    }

}
