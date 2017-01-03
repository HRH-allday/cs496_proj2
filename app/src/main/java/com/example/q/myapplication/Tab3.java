package com.example.q.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class Tab3 extends Fragment {

    static final int REQUEST_FOR_REGISTRATION = 110;
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



        return view;


    }

    @Override
    public void onActivityResult(int requestCode, int resultcode, Intent Data){
        super.onActivityResult(requestCode,resultcode,Data);
        if(requestCode == REQUEST_FOR_REGISTRATION){

            if(resultcode ==1){

                Toast.makeText(getContext(),"Activity registration worked",Toast.LENGTH_LONG);
                try {

                    Log.d("result_joject",Data.getExtras().getString("result"));
                    JSONObject resultObject  = new JSONObject(Data.getExtras().getString("result"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }

    }



}
