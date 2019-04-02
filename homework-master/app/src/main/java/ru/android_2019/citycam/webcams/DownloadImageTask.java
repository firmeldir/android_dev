package ru.android_2019.citycam.webcams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.JsonReader;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;




public class DownloadImageTask extends AsyncTask<URL, Void, Bitmap> {

    private WeakReference<Context> applicationContext;

    public DownloadImageTask(WeakReference<Context> context){
        this.applicationContext = context;
    }

    private Camera camera;
    private Bitmap bitmap;

    protected Bitmap doInBackground(URL... urls) {

        URL urldisplay = urls[0];
        Bitmap mIcon11 = null;

        try {
            HttpsURLConnection c =(HttpsURLConnection)urldisplay.openConnection();
            c.setRequestProperty("X-RapidAPI-Key", "9897bac797msh44ca722fdb17575p1bedeajsned15390077b3");
            try{ camera = readJsonStream(c.getInputStream()); }catch (Exception e){ Log.e("ERROR", e.toString()); }


            try {
                if(camera == null){Log.i("hui", "penis");}
                InputStream in = new java.net.URL(camera.getUrl_image()).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        bitmap = mIcon11;
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        Intent intent = new Intent("json");

        String name_h;
        String time_h;
        String city_h;

        if(camera != null)
        {
            name_h = camera.name;
            time_h = camera.time;
            city_h = camera.location;
        }
        else
            {
                name_h = "\"nothing here\"";
                time_h = "";
                city_h = "";
            }


        intent.putExtra("name_v", name_h );
        intent.putExtra("time_v", time_h);
        intent.putExtra("city_v", city_h);
        LocalBroadcastManager.getInstance(applicationContext.get()).sendBroadcast(intent);

        Intent intent2 = new Intent("image");
        intent2.putExtra("image",bitmap);
        LocalBroadcastManager.getInstance(applicationContext.get()).sendBroadcast(intent2);
    }

    private Camera readJsonStream(InputStream in) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readCamera(reader);
        }catch (Exception e)
        {
            return null;
        }finally {
            reader.close();
        }
    }


    private Camera readCamera(JsonReader reader) throws IOException {
        Camera camera = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("result")) {
                camera = readResult(reader);
            }else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return camera;
    }

    private Camera readResult(JsonReader reader) throws IOException
    {
        Camera new_cam = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams"))
            {
                reader.beginArray();
                while(reader.hasNext()) {
                    new_cam = readWebcamsArray(reader);
                }
                reader.endArray();
            }else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new_cam;
    }

    private Camera readWebcamsArray(JsonReader reader) throws IOException {

        String name_c = null;
        String url_image = null;
        String time = null;
        String location = null;

        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();

            switch (name) {
                case "title":
                    {
                        name_c = reader.nextString();
                        break;
                    }
                case "image":
                    {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name_i = reader.nextName();

                            switch (name_i) {
                                case "current":
                                    url_image = previewFinder(reader);
                                    break;
                                case "update":
                                    long unixTime = reader.nextLong();
                                    @SuppressLint("SimpleDateFormat")
                                    String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(unixTime * 1000));
                                    time = date;
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                        break;
                    }

                case "location":
                    location = cityFinder(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        Log.i("uuu", name_c);
        Log.i("uuu", time);
        Log.i("uuu", location);

        return new Camera(name_c,url_image, time, location);
    }


    private String previewFinder(JsonReader reader) throws IOException
    {
        String buffer = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("preview"))
            {
                buffer = reader.nextString();
            }else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return buffer;
    }

    private String cityFinder(JsonReader reader) throws IOException
    {
        String buffer = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            if (name.equals("city"))
            {
                buffer = reader.nextString();
            }else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return buffer;
    }
}