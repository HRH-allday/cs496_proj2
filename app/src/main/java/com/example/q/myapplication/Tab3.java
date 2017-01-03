package com.example.q.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class Tab3 extends Fragment {

    static final int REQUEST_FOR_REGISTRATION = 110;
    static final int REQUEST_FOR_AUCTION = 120;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.tab3, container, false);

        Button register_button = (Button) view.findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),Registration.class);
                startActivityForResult(intent, REQUEST_FOR_REGISTRATION);
            }
        });

        Button test_button = (Button) view.findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),AuctionActivity.class);
                startActivityForResult(intent, REQUEST_FOR_AUCTION);
            }
        });



        return view;


    }

    @Override
    public void onActivityResult(int requestCode, int resultcode, Intent Data){
        super.onActivityResult(requestCode,resultcode,Data);
        if(requestCode == REQUEST_FOR_REGISTRATION){

            Toast.makeText(getContext(),"Activity registration worked",Toast.LENGTH_LONG);
        }
        else if(requestCode == REQUEST_FOR_AUCTION){
            Toast.makeText(getContext(),"Auction worked",Toast.LENGTH_LONG);
        }

    }



}
