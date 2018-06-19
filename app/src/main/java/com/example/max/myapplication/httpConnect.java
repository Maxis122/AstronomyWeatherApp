package com.example.max.myapplication;

/**
 * Created by Max on 05/12/2017.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

public class httpConnect {

    //Key to weather api.
    String key_Weather = "ba0202b121fd4ac4a2cfa8d322ab49c1";

    //Endpoint for weather api.
    String api_https_weather_daily = "http://api.weatherbit.io/v2.0/forecast/daily";

    //Key to NASA api.
    String key_NASA = "RO8v4hV2PjpHCnhp6bFbGGdmNxJeGl93niFXNB0T";

    //Endpoint for NASA api
    String api_https_NASA = "https://api.nasa.gov/";

    //Endpoint for poscodes.io api
    String api_https_postcodes = "http://api.postcodes.io/postcodes";

    //TAG
    final String TAG = "JsonParser.java";

    //String to store json data
    String json = "";

    //-------------------------Modified Code from Workshop 5 -------------------------------//
    //The code is adapted to use the same httpConnect method for all the httpConnect calls.
    //The change is that the specific calls are done using methods that construct the correct URL.
    //This saves code since we dont need to copy/paste the same httpConnect method for each different call,
    //while still keeping all httpConnect specific variables in this class.
    public String httpConnection(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection restConnection = (HttpURLConnection) u.openConnection();
            restConnection.setRequestMethod("GET");
            restConnection.setUseCaches(false);
            restConnection.setAllowUserInteraction(false);
            restConnection.setConnectTimeout(10000);
            restConnection.setReadTimeout(10000);


            restConnection.connect();
            int status = restConnection.getResponseCode();

            // switch statement to catch HTTP 200 and 201 errors
            switch (status) {
                case 200:
                    // live connection to your REST service is established here using getInputStream() method
                    BufferedReader br = new BufferedReader(new InputStreamReader(restConnection.getInputStream()));

                    // create a new string builder to store json data returned from the REST service
                    StringBuilder sb = new StringBuilder();
                    String line;

                    // loop through returned data line by line and append to stringbuilder 'sb' variable
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    // remember, you are storing the json as a stringy
                    try {
                        json = sb.toString();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing data " + e.toString());
                    }
                    // return JSON String containing data to Tweet activity (or whatever your activity is called!)
                    return json;

            }
            // HTTP 200 and 201 error handling from switch statement
        } catch (MalformedURLException ex) {
            Log.e(TAG, "Malformed URL ");
        } catch (IOException ex) {
            Log.e(TAG, "IO Exception ");
        }
        return null;
    }


    public String getPostFromLatLongi(double lat, double longi) {
        // Get set the URL for getting postcode from longi and lat.
        String url = api_https_postcodes + "?lon=" + longi + "&lat=" + lat;
        return httpConnection(url);

    }

    public String getLongLatFromPost(String postcode) {
        // Get the long and lat from a postcode.
        String url = api_https_postcodes + "/" + postcode;
        return httpConnection(url);
    }


    public String getNASAPicture() {
        //Get the nasa picture of the day data.
        String url = api_https_NASA + "planetary/apod?api_key=" + key_NASA;
        return httpConnection(url);
    }


    public String getWeather(double lat, double longi) {
        //Get the weather from the lat and longi.
        String url = api_https_weather_daily + "?lon=" + longi + "&lat=" + lat + "&key=" + key_Weather;
        return httpConnection(url);
    }

    public String getAstroid(String date) {
        String url = api_https_NASA + "neo/rest/v1/feed?start_date=" + date + "&end_date=" + date + "&api_key=" + key_NASA;
        return httpConnection(url);
    }
}
