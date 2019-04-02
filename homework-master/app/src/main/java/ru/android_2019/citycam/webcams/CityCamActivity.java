package ru.android_2019.citycam.webcams;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import ru.android_2019.citycam.R;
import ru.android_2019.citycam.model.City;


public class CityCamActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "city";
    private City city;

    DownloadImageTask downloadImageTask = null;
    String imageDirectory;

    private ProgressBar progressView;

    private ImageView camImageView;
    private TextView name_v;
    private TextView time_v;
    private TextView city_v;

    SQHelper sqHelper;
    //--------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("json"));
        LocalBroadcastManager.getInstance(this).registerReceiver(imageBroadcastReceiver, new IntentFilter("image"));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if(camImageView.getDrawable() != null)
        {
            BitmapDrawable bitmapDrawable = (BitmapDrawable)camImageView.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            imageDirectory = Util.saveToInternalStorage("image", bitmap, getApplicationContext());

            outState.putString("image", imageDirectory);
            outState.putString("name", name_v.getText().toString());
            outState.putString("time", time_v.getText().toString());
            outState.putString("city", city_v.getText().toString());
        }

        super.onSaveInstanceState(outState);
    }

    //--------------------------------------------------------------------------
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null)
            {
                String result = bundle.getString("name_v");
                name_v.setText(result);

                result = bundle.getString("time_v");
                time_v.setText(result);

                result = bundle.getString("city_v");
                city_v.setText(result);
            }
        }
    };

    private BroadcastReceiver imageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Bitmap bitmap = bundle.getParcelable("image");
                if(bitmap == null){ bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hitler); camImageView.setImageBitmap(bitmap);}
                else{
                    camImageView.setImageBitmap(bitmap);
                    loadToSQL();
                }


            }
        }
    };
    //--------------------------------------------------------------------------


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(imageBroadcastReceiver);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadImageTask;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_cam);

        sqHelper = new SQHelper(this);

        camImageView = (ImageView) findViewById(R.id.cam_image);
        name_v = (TextView) findViewById(R.id.name_nn);
        time_v = (TextView) findViewById(R.id.time_nn);
        city_v = (TextView) findViewById(R.id.city_nn);

        if(savedInstanceState != null)
        {
            name_v.setText(savedInstanceState.getString("name"));
            time_v.setText(savedInstanceState.getString("time"));
            city_v.setText(savedInstanceState.getString("city"));

            String imageDir = savedInstanceState.getString("image");
            if(imageDir != null)
            {
                Bitmap bitmap;

                try
                {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(imageDir));

                    if(bitmap.equals(null)){ Log.i("EROOR", "2"); }

                    camImageView.setImageBitmap(bitmap);

                }catch (Exception e){e.printStackTrace();}
            }
            else{ Log.i("ERROR", "ERROR"); }
        }

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            finish();
        }

        progressView = (ProgressBar) findViewById(R.id.progress);
        getSupportActionBar().setTitle(city.name);
        progressView.setVisibility(View.VISIBLE);


        if(camImageView.getDrawable() ==  null){

            boolean flag = setBySQL(city.name);

            if(flag && hasConnection(this))
            {
                try
                {
                    downloadImageTask = new DownloadImageTask(new WeakReference<>(getApplicationContext()));
                    URL url_city = Webcams.createNearbyUrl(city.latitude, city.longitude);
                    downloadImageTask.execute(url_city);
                }catch (Exception e){}
            }

            if(!hasConnection(this)){ name_v.setText(R.string.no_connection); }
        }
    }

    //----------------------------------------------------------------------------
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
    //----------------------------------------------------------------------------


    //----------------------------------------------------------------------------
    private void loadToSQL()
    {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = sqHelper.getWritableDatabase();


        String name_cv = name_v.getText().toString();
        String time_cv = time_v.getText().toString();
        String city_cv = city_v.getText().toString();
        String name_of_cv = city.name;

        BitmapDrawable bitmapDrawable = (BitmapDrawable)camImageView.getDrawable();
        Bitmap bitmap_cv = bitmapDrawable.getBitmap();

        cv.put("name", name_cv);
        cv.put("city", city_cv);
        cv.put("time", time_cv);
        cv.put("image", getBytes(bitmap_cv));
        cv.put("name_of", name_of_cv);


        db.insert("city_table", null, cv);
    }

    private boolean setBySQL(String name_oof)
    {
        SQLiteDatabase db = sqHelper.getWritableDatabase();
        Cursor c = db.query("city_table", null, null, null, null, null, null);

        if(c.moveToFirst())
        {
            int  name_col = c.getColumnIndex("name");
            int time_col = c.getColumnIndex("time");
            int city_col = c.getColumnIndex("city");
            int image_col = c.getColumnIndex("image");
            int name_of_col = c.getColumnIndex("name_of");

            do {
                if(c.getString(name_of_col).equals(name_oof))
                {
                    camImageView.setImageBitmap(getImage(c.getBlob(image_col)));
                    name_v.setText(c.getString(name_col));
                    time_v.setText(c.getString(time_col));
                    city_v.setText(c.getString(city_col));
                    return false;
                }
            } while (c.moveToNext());
        }

        c.close();
        return true;
    }

    class SQHelper extends SQLiteOpenHelper
    {
        public SQHelper(@Nullable Context context) {
            super(context, "city_table", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table city_table ("
                    + "_id integer primary key autoincrement,"
                    + "name text,"
                    + "city text,"
                    + "time text,"
                    + "name_of text,"
                    + "image blob"
                    +  ");");

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
    //----------------------------------------------------------------------------

    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }

}
