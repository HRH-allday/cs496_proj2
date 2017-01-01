package com.example.q.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class Tab2 extends Fragment {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    private GridView mGridView = null;
    private MyAdapter mGridAdapter = null;
    private List<Item> items=new ArrayList<Item>();

    private Uri mImageCaptureUri;
    String path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab2, container, false);
        mGridView = (GridView) view.findViewById(R.id.gridView1);
        mGridAdapter = new MyAdapter((getActivity()));
        final ImageView imgzoom = (ImageView) view.findViewById(R.id.imageZoom);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item dc = (Item) mGridAdapter.getItem(position);
                imgzoom.setImageURI(dc.uri);
                imgzoom.setVisibility(View.VISIBLE);
                imgzoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgzoom.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });



        final DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakePhotoAction();
            }
        };
        final DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakeAlbumAction();
            }
        };

        final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        Button b = (Button) view.findViewById(R.id.append);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영", cameraListener)
                        .setNeutralButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancelListener)
                        .show();
            }
        });

        Button connect_btn = (Button) view.findViewById(R.id.connect_to_DB);
        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(path);
                Future uploading = Ion.with(getActivity())
                        .load("http://52.79.155.110:3000/upload")
                        .setMultipartFile("image", f)
                        .asString()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override

                            public void onCompleted(Exception e, Response<String> result) {
                                try {


                                    Log.d("son",result.getResult());
                                    JSONObject jobj = new JSONObject(result.getResult());
                                    Toast.makeText(getContext(), jobj.getString("response"), Toast.LENGTH_SHORT).show();

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });
            }

        });
        mGridView.setAdapter(mGridAdapter);
        return view;
    }


    public void doTakePhotoAction() // 카메라 촬영 후 이미지 가져오기
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    public void doTakeAlbumAction() // 앨범에서 이미지 가져오기
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode != RESULT_OK)
            return;

        switch(requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
                path = getPathFromURI(mImageCaptureUri);
                items.add(new Item(mImageCaptureUri));
                mGridAdapter.notifyDataSetChanged();
                break;
            }

            case PICK_FROM_CAMERA:
            {
                mImageCaptureUri = data.getData();
                path = getPathFromURI(mImageCaptureUri);
                items.add(new Item(mImageCaptureUri));
                mGridAdapter.notifyDataSetChanged();
                break;
            }
            default:
                break;
        }


    }
    public class MyAdapter extends BaseAdapter
    {


        private LayoutInflater inflator;

        public MyAdapter(Context context) {
            // TODO Auto-generated constructor stub
            inflator=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // TODO Auto-generated method stub
            View v=convertView;
            ImageView img1;
            TextView txt1;

            if(v==null){
                v=inflator.inflate(R.layout.grid_item,parent,false);
                v.setTag(R.id.picture,v.findViewById(R.id.picture));
                v.setTag(R.id.text,v.findViewById(R.id.text));
            }

            img1=(ImageView)v.findViewById(R.id.picture);
            Item item=(Item)getItem(position);

            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(item.uri);
                Drawable yourChoice = Drawable.createFromStream(inputStream, item.uri.toString());
                img1.setImageDrawable(yourChoice);

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            return v;
        }

    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this.getContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private class Item
    {
        final Uri uri;
        int chosen;

        Item(Uri uri)
        {
            this.uri=uri;
        }
    }


}
