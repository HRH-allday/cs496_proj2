package com.example.q.myapplication;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;

import android.app.Dialog;
import android.app.TimePickerDialog;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.Menu;

import android.view.View;

import android.widget.DatePicker;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import android.widget.Toast;

import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.ViewGroup;
import android.widget.Button;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.path;
import static android.R.id.edit;

/**
 * Created by Q on 2017-01-02.
 */

public class Registration extends Activity{

    int year, month, day, hour, minute;
    static final int DATE_DIALOG_ID = 100;
    static final int TIME_DIALOG_ID = 200;
    static final int PICK_FROM_ALBUM = 110;
    TextView dtv ;
    TextView ttv ;
    TextView itv;

    String path="";
    @Override
    public void onCreate(Bundle saveInstancestate){

        super.onCreate(saveInstancestate);
        setContentView(R.layout.account);

        Log.d("flag1","sdfsdfsdfsdfsdfsdfsdfsdfsdfsdf");

        dtv = (TextView) findViewById(R.id.date_text);
        ttv = (TextView) findViewById(R.id.time_text);
        itv = (TextView) findViewById(R.id.image_path);
        Button time_button = (Button) findViewById(R.id.time_select);
        Button date_button = (Button) findViewById(R.id.date_select);
        Button upload_button = (Button)findViewById(R.id.image_select);
        Button register_button = (Button)findViewById(R.id.register_auction);

        final EditText title = (EditText)findViewById(R.id.name);
        final EditText description = (EditText)findViewById(R.id.desc_text);
        final EditText minimum_price = (EditText)findViewById(R.id.price);
        time_button.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               showDialog(TIME_DIALOG_ID);

                                           }
                                       }

        );

        date_button.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               showDialog(DATE_DIALOG_ID);


                                           }
                                       });

        upload_button.setOnClickListener(new View.OnClickListener(){

                                             @Override
                                             public void onClick(View v) {
                                                 doTakeAlbumAction();
                                             }
                                         }

        );

        register_button.setOnClickListener(new View.OnClickListener(){

                                               @Override
                                               public void onClick(View v) {

                                                   String input_title = title.getText().toString();
                                                   String input_desc = description.getText().toString();
                                                   String price = minimum_price.getText().toString();


                                                   String date = dtv.getText().toString();
                                                   String time = ttv.getText().toString();

                                                   File uploading_file = new File(path);
                                                   Future uploading = Ion.with(getApplicationContext())
                                                           .load("http://52.79.155.110:3000/register_auction")
                                                           .setMultipartParameter("postname",input_title)
                                                           .setMultipartParameter("price",price)
                                                           .setMultipartParameter("date",date)
                                                           .setMultipartParameter("time",time)
                                                           .setMultipartParameter("description",input_desc)
                                                           .setMultipartFile("image", uploading_file)
                                                           .asString()
                                                           .withResponse()
                                                           .setCallback(new FutureCallback<Response<String>>() {
                                                               @Override

                                                               public void onCompleted(Exception e, Response<String> result) {
                                                                   try {



                                                                       Log.d("son",result.getResult());

                                                                       JSONObject jobj = new JSONObject(result.getResult());
                                                                       Intent resultIntent = new Intent();
                                                                       resultIntent.putExtra("result",result.getResult());
                                                                       setResult(1,resultIntent);
                                                                        finish();


                                                                   } catch (JSONException e1) {
                                                                       e1.printStackTrace();
                                                                   }

                                                               }
                                                           });


                                               }
                                           }

        );






        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        updateDisplay();

    }

    public void doTakeAlbumAction() // 앨범에서 이미지 가져오기
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }




    private void updateDisplay()
    {
        dtv.setText(new StringBuilder()
                .append(month+1).append("-")
                .append(day).append("-")
                .append(year).append(" "));
        ttv.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));
    }

    private static String pad(int c) {
        // TODO Auto-generated method stub
        if(c >= 10){
            return String.valueOf(c);
        }else
            return "0" + String.valueOf(c);
    }


    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int selecting_year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    year = selecting_year;
                    month = monthOfYear;
                    day = dayOfMonth;
                    updateDisplay();
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                    // TODO Auto-generated method stub
                    hour = hourOfDay;
                    minute = minuteOfHour;
                    updateDisplay();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch(id)
        {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, year, month, day);
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener, hour, minute, false);
        }

        return null;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_FROM_ALBUM) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri image_URI = data.getData();
                path = getPathFromURI(image_URI);
                itv.setText(path);

            }
        }
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }







}


