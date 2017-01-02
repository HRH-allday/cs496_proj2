package com.example.q.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.M;
import static com.facebook.FacebookSdk.getApplicationContext;


public class Tab2 extends Fragment {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    private GridView mGridView = null;
    private MyAdapter mGridAdapter = null;
    private List<Item> items=new ArrayList<Item>();

    private Uri mImageCaptureUri;
    String path;
    String filepath;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2, fab3;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;



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

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab1 = (FloatingActionButton) view.findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) view.findViewById(R.id.fab3);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);








        mGridView.setAdapter(mGridAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= M && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFAB();
                }
            });
            fab1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoadAsyncTask la = new LoadAsyncTask();
                    la.execute();
                }
            });
            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doTakeAlbumAction();
                }
            });
            fab3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doTakePhotoAction();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(getActivity(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendingPhoto(){
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
                sendingPhoto();
                break;
            }

            case PICK_FROM_CAMERA:
            {
                mImageCaptureUri = data.getData();
                path = getPathFromURI(mImageCaptureUri);
                items.add(new Item(mImageCaptureUri));
                mGridAdapter.notifyDataSetChanged();
                sendingPhoto();
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
    public class LoadAsyncTask extends AsyncTask<Void,Void,String[]> {





        @Override
        protected String[] doInBackground(Void... params) {

            try {
                URL u ;
                u = new URL("http://52.79.155.110:3000/download/check");
                Log.d("connected", "sasasasas");
                HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                Log.d("open", "");
                huc.setRequestMethod("POST");
                huc.setDoInput(true);
                huc.setDoOutput(true);
                huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                huc.connect();

                int status = huc.getResponseCode();

                InputStream is = null;

                if(status > 400){

                    is = huc.getErrorStream();
                }

                else {
                    is = huc.getInputStream();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();

                String response = new String(byteData);

                Log.d("reererrer",response);

                JSONArray jArray= new JSONArray(response);
                JSONObject jtemp;
                ArrayList<String> name_list = new ArrayList<String>();
                for(int i=0;i<jArray.length();i++){
                    jtemp = jArray.getJSONObject(i);
                    name_list.add(jtemp.getString("name"));

                }

                String[] stockArr = new String[name_list.size()];
                stockArr = name_list.toArray(stockArr);

                is.close();
                return stockArr;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;

        }

        protected void onPostExecute(final String[] result){


            final String[] item = result;

            final int[] selector = {-1};
            String select;
            AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
            ab.setTitle("Title");
            ab.setSingleChoiceItems(item, 0,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 각 리스트를 선택했을때
                            selector[0] = whichButton;

                        }
                    }).setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Log.d("whichbutton", String.valueOf(selector[0]));
                            DownloadAsyncTask da= new DownloadAsyncTask();
                            if(selector[0] == -1) selector[0] = 0;
                            da.execute(item[selector[0]]);
                            // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Cancel 버튼 클릭시
                        }
                    });
            ab.show();


        }
    }


    public class DownloadAsyncTask extends AsyncTask<String,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            URL ud;
            String choosing = params[0];
            try {
                JSONObject file_to_choose = new JSONObject();
                file_to_choose.accumulate("name", choosing);
                String json = file_to_choose.toString();
                ud = new URL("http://52.79.155.110:3000/upload/:file");
                Log.d("connected", "sasasasas");
                HttpURLConnection huc = (HttpURLConnection) ud.openConnection();
                Log.d("open", "");
                huc.setRequestMethod("POST");
                huc.setDoInput(true);
                huc.setDoOutput(true);
                huc.setRequestProperty("Accept", "application/json");
                huc.setRequestProperty("Content-type", "application/json");
                OutputStream os = huc.getOutputStream();
                os.write(json.getBytes("euc-kr"));
                os.flush();
                os.close();

                InputStream is = null;


                is = huc.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);

                is.close();
                String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/proj2";
                File dir = new File(dirPath);

                if(!dir.exists())
                    dir.mkdir();
                filepath = dirPath + "/" + choosing;
                return bmp;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute (final Bitmap result)
        {
            String file_path = filepath;
            Log.i("split", file_path);
            String[] parts = file_path.split("[.]");
            String filename = parts[0];
            String ext = parts[1];
            try {
                File f = new File(file_path);

                int i = 1;
                 while(f.exists() == true) {
                    file_path = filename + "("+ i + ")." + ext;
                    f = new File(file_path);
                     i+=1;
                }

                f.createNewFile();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                result.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Item nitem = new Item(Uri.fromFile(f));
                items.add(nitem);
                mGridAdapter.notifyDataSetChanged();
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
                out.flush();
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }




        }

    }

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;
            Log.d("Raj", "open");

        }
    }
}
