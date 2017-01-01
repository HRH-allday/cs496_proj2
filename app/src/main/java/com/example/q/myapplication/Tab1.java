package com.example.q.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static android.os.Build.VERSION_CODES.M;
import static com.example.q.myapplication.R.id.fbcontact;
import static com.example.q.myapplication.R.id.mName;


public class Tab1 extends Fragment {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;

    private View view;
    private Button b1;
    private Button b2;
    ContentResolver contentResolver;
    private String ec2url = "http://ec2-52-79-155-110.ap-northeast-2.compute.amazonaws.com:3000";
    private String localurl = "http://143.248.49.213:8080";
    private String body = "posttest";
    private URL u;
    private String ind = "";
    private String cont = "";

    private JSONArray jArray = new JSONArray();

    private HttpURLConnection huc;

    private class ViewHolder {
        public TextView mName;

        public TextView mNumber;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context mContext) {
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

        public void addItem(ListData addInfo){
            mListData.add(addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
            dataChange();
        }

        public void sort(){
            Collections.sort(mListData, ListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item, null);

                holder.mName = (TextView) convertView.findViewById(mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);


            holder.mName.setText(mData.mName);
            holder.mNumber.setText(mData.mNumber);

            return convertView;
        }
    }
    public void addcontact(){
        contentResolver = getActivity().getContentResolver();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cursor1 = contentResolver.query(uri, projection, null, null, null);

        int indexName = cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        mAdapter.resetList();
        jArray = new JSONArray();
        ind = "c";
        cont="/contact";
        if(cursor1.moveToFirst()) {
            do {
                ListData contact = new ListData();
                String name   = cursor1.getString(indexName);
                String number = cursor1.getString(indexNumber);

                contact.mName = name;
                contact.mNumber = number;
                try {
                    JSONObject cont = new JSONObject();
                    cont.put("name", name);
                    cont.put("number", number);
                    jArray.put(cont);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                int br = 0;

                for(int i=0 ; i<mAdapter.getCount() ; i++ ){
                    ListData l = (ListData) mAdapter.getItem(i);
                    if(l.mName.equals(name)){
                        br = 1;
                        break;
                    }

                }
                if(br == 0)
                    mAdapter.addItem(contact);

            } while (cursor1.moveToNext());
        }
        cursor1.close();
        mAdapter.notifyDataSetChanged();
        PostThread p = new PostThread();
        p.start();
    }
    public void addFacebookContact(){
        mAdapter.resetList();
        ind = "b";
        cont = "/fbcontact";
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/taggable_friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        JSONObject obj = response.getJSONObject();
                        try {
                            JSONArray data = obj.getJSONArray("data");
                            jArray = new JSONArray();
                            for(int i = 0; i < data.length(); i++){
                                String name = data.getJSONObject(i).getString("name");
                                JSONObject cont = new JSONObject();
                                cont.put("name", name);
                                jArray.put(cont);
                                ListData fbcontact = new ListData();
                                fbcontact.mName = name;
                                fbcontact.mNumber = "";
                                mAdapter.addItem(fbcontact);
                            }
                            mAdapter.notifyDataSetChanged();
                            PostThread p = new PostThread();
                            p.start();
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }
    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.tab1, container, false);


        mListView = (ListView) view.findViewById(R.id.mList);
        mAdapter = new ListViewAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        b1 = (Button) view.findViewById(R.id.contact);
        b2 = (Button) view.findViewById(fbcontact);



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListData who = (ListData) parent.getAdapter().getItem(position);
                final String wname = who.mName;
                final String wnum = who.mNumber;

                AlertDialog.Builder dlg1 = new AlertDialog.Builder(getActivity());
                dlg1.setMessage("Call / Text").setCancelable(true).setPositiveButton("Call",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + wnum));
                                startActivity(intent);
                            }
                        }).setNegativeButton("Text",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("smsto:" + wnum));
                                startActivity(intent);
                            }
                        });


                dlg1.setTitle(wname);
                dlg1.show();
            }
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }else{
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addcontact();
                }
            });
            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFacebookContact();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addcontact();
            } else {
                Toast.makeText(getActivity(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class PostThread extends Thread {
        public PostThread() {

        }

        @Override
        public void run() {
            super.run();
            try {
                u = new URL(ec2url+cont);
                Log.i("connected", "");
                huc = (HttpURLConnection) u.openConnection();
                Log.i("open", "");
                huc.setRequestMethod("POST");
                huc.setDoInput(true);
                huc.setDoOutput(true);
                huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                OutputStream os = huc.getOutputStream();
                os.write((ind+jArray.toString()).getBytes("utf-8"));
                os.flush();
                os.close();
                InputStream is = huc.getInputStream();
                byte[] arr = new byte[is.available()];
                is.read(arr);
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
