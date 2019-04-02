package ru.android_2019.citycam.webcams;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Константы для работы с Webcams API
 */
public final class Webcams {

    // Зарегистрируйтесь на http://ru.webcams.travel/developers/
    // и вставьте сюда ваш devid
    private static final String DEV_ID = "9897bac797msh44ca722fdb17575p1bedeajsned15390077b3";
    private static final String BASE_URL = "https://webcamstravel.p.rapidapi.com/webcams/list/nearby";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_SHOW = "show";
    private static final String LANG = "en";
    private static final String SHOW = "webcams:title,image,location";


    /**
     * Возвращает URL для выполнения запроса Webcams API для получения
     * информации о веб-камерах рядом с указанными координатами в формате JSON.
     */
    public static URL createNearbyUrl(double latitude, double longitude)
            throws MalformedURLException {
        Uri uri = Uri.parse(BASE_URL + "=" + Double.toString(latitude) + "," + Double.toString(longitude) + "," + "250")
                .buildUpon()
                .appendQueryParameter(PARAM_SHOW, SHOW)
                .appendQueryParameter(PARAM_LANG, LANG)
                //.appendQueryParameter(PARAM_METHOD, METHOD_NEARBY)
                //.appendQueryParameter(PARAM_LAT, Double.toString(latitude))
                //.appendQueryParameter(PARAM_LON, Double.toString(longitude))
                //.appendQueryParameter(PARAM_DEVID, DEV_ID)
                //.appendQueryParameter(PARAM_FORMAT, FORMAT_JSON)

                .build();
        //uri.toString()
        return new URL(uri.toString());
    }

    private Webcams() {}
}
