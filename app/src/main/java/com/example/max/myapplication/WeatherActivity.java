package com.example.max.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.util.Date;
import android.icu.text.SimpleDateFormat;

public class WeatherActivity extends AppCompatActivity {

    //Json String
    String json_data = "";

    //Doubles for longitude and latitude.
    double lat = 0;
    double longi = 0;

    //Local Storage filename
    String locationFileName = "locationData";
    String weatherFileName = "weatherData";

    //String to store current location as a postcode.
    String currentLocation = "";

    //Array list to store Json values
    ArrayList<String> max_temp = new ArrayList<String>();
    ArrayList<String> min_temp = new ArrayList<String>();
    ArrayList<String> icon = new ArrayList<String>();
    ArrayList<String> description = new ArrayList<String>();
    ArrayList<String> datetime = new ArrayList<String>();

    //JSON Tags
    private static final String TAG_DATA = "data";
    private static final String TAG_WEATHER = "weather";
    private static final String TAG_MINTEMP = "min_temp";
    private static final String TAG_MAXTEMP = "max_temp";
    private static final String TAG_ICON = "code";
    private static final String TAG_DESC = "description";
    private static final String TAG_DATETIME = "datetime";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //Get the weather
        get_Weather(null);
    }

    public void goto_Settings(View view) {
        //Goto the settings activity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goto_Back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void get_Weather(View view) {
        //Check for local storage
        File file = getFileStreamPath(locationFileName);
        if(file.exists())
        {
            try {
                //If we have data in local storage, load it.
                FileInputStream fIn = openFileInput(locationFileName);
                InputStreamReader isr = new InputStreamReader(fIn);
                BufferedReader buffreader = new BufferedReader( isr ) ;

                // Loop through the file, finding the last location, longitude and latitude
                currentLocation = buffreader.readLine();

                //Possible number fuckery here
                try {
                    longi = Double.parseDouble(buffreader.readLine());
                    lat= Double.parseDouble(buffreader.readLine());
                } catch (NumberFormatException NFe) {

                }

                isr.close();


            } catch (IOException ioe) {

            }

        } else {
            //There is no location data, so we must go back to the settings and toast a message
            CharSequence text = "No location found, please set location.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();

            Intent intent =  new Intent(this, SettingsActivity.class);
            startActivity(intent);

        }

        //Use location data to get AsyncTask
        //Get the data from NASA API
        new AsyncTask_GetWeather().execute();
    }

    public class AsyncTask_GetWeather extends AsyncTask<String, String, String> {
        @Override
        // this method is used for......................
        protected void onPreExecute() {}

        @Override
        // this method is used for...................
        protected String doInBackground(String... arg0)  {

            //Check if there is an internet connection
            Context context = getApplicationContext();
            ConnectivityManager connectivityManager =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if(isConnected) {
                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();

                // get json string from service url
                json_data = jParser.getWeather(lat, longi);

                // save json data
                try {
                    //Open a filestream and load the file.
                    FileOutputStream outputStream;

                    //Create/Open the file in private mode.
                    outputStream = openFileOutput(weatherFileName, MODE_PRIVATE);

                    //Write the json data.
                    outputStream.write((json_data).getBytes());

                    //Close the file
                    outputStream.close();
                } catch (Exception ex) {
                    // Exception in saving.
                    CharSequence text = "Error in saving..." + ex.toString();
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            } else {
                //Try to load the file.
                File file = getFileStreamPath(weatherFileName);
                if(file.exists()) {
                    try {
                        //If we have data in local storage, load it.
                        FileInputStream fIn = openFileInput(weatherFileName);
                        InputStreamReader isr = new InputStreamReader(fIn);
                        BufferedReader buffreader = new BufferedReader(isr);

                        // Loop through the file, finding the last location, longitude and latitude
                        json_data = buffreader.readLine();

                        isr.close();
                    } catch (Exception e) {
                        return "Failure";
                    }
                }
            }

            try {
                //Create JSON Object
                JSONObject WeatherObject = new JSONObject(json_data);
                JSONArray WeatherDataArray = WeatherObject.getJSONArray(TAG_DATA);

                for (int i = 0; i < 16; i++) {
                    JSONObject o = WeatherDataArray.getJSONObject(i);
                    max_temp.add(i, o.getString(TAG_MAXTEMP));
                    min_temp.add(i, o.getString(TAG_MINTEMP));
                    datetime.add(i, o.getString(TAG_DATETIME));
                    JSONObject weather = o.getJSONObject(TAG_WEATHER);
                    icon.add(i, weather.getString(TAG_ICON));
                    description.add(i, weather.getString(TAG_DESC));
                }

            } catch (JSONException je) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            //If we have a failure.
            if (strFromDoInBg == "Failure") {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                return;
            }


            //Assign data to widgets
            TextView text;

            text = findViewById(R.id.text_weatherUpdated);
            text.setText("Weather Last Updated: " + datetime.get(0));

            //Get the offset - only relevent if data is grabbed from storage.
            int offset = getDateOffset(datetime.get(0));

            //The highest offset we can have is 11 since the array is only 16 values large.
            if (offset >= 12) {
                offset = 11;
            }

            //----------Today-------------------//
            //Description
            text = findViewById(R.id.weather_description);
            text.setText(description.get(0 + offset));

            //Min Temp
            text = findViewById(R.id.min_temp);
            text.setText("Minimum Temperature: " + min_temp.get(0 + offset) + "\u00b0C");

            //Max Temp
            text = findViewById(R.id.max_temp);
            text.setText("Maximum Temperature: " + max_temp.get(0 + offset) + "\u00b0C");

            //Image
            getImageFromCode(icon.get(0 + offset), (ImageView)findViewById(R.id.image_Today));

            //------------------Tomorrow----------------//
            //Temp
            text = findViewById(R.id.temp_tomorrow);
            text.setText(min_temp.get(1 + offset) + "\u00b0C - " + max_temp.get(1 + offset) + "\u00b0C");

            //Image
            getImageFromCode(icon.get(1 + offset), (ImageView)findViewById(R.id.image_Tomorrow));

            //--------------Two Days------------------//
            //Temp
            text = findViewById(R.id.temp_TwoDays);
            text.setText(min_temp.get(2 + offset) + "\u00b0C - " + max_temp.get(2 + offset) + "\u00b0C");

            //Date
            text = findViewById(R.id.text_TwoDays);
            text.setText(datetime.get(2 + offset));

            //Image
            getImageFromCode(icon.get(2 + offset), (ImageView)findViewById(R.id.image_TwoDays));


            //--------------Three Days------------------//
            //Temp
            text = findViewById(R.id.temp_ThreeDays);
            text.setText(min_temp.get(3 + offset) + "\u00b0C - " + max_temp.get(3 + offset) + "\u00b0C");

            //Date
            text = findViewById(R.id.text_ThreeDays);
            text.setText(datetime.get(3 + offset));

            //Image
            getImageFromCode(icon.get(3 + offset), (ImageView)findViewById(R.id.image_ThreeDays));
        }
    }

    public void getImageFromCode(String code, ImageView imageView) {
        AsyncTask_GetWeatherIcon downloadImage;

        switch (code) {
            case "200":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/t01d.png");
                downloadImage.execute();
                break;

            case "201":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/t02d.png");
                downloadImage.execute();
                break;

            case "202":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/t03d.png");
                downloadImage.execute();
                break;

            case "230":
            case "231":
            case "232":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/t04d.png");
                downloadImage.execute();
                break;

            case "233":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/t05d.png");
                downloadImage.execute();
                break;

            case "300":
            case "301":
            case "302":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/d01d.png");
                downloadImage.execute();
                break;

            case "500":
            case "501":
            case "502":
            case "511":
            case "520":
            case "521":
            case "522":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/r06d.png");
                downloadImage.execute();
                break;

            case "600":
            case "601":
            case "602":
            case "621":
            case "622":
            case "623":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/s02d.png");
                downloadImage.execute();
                break;

            case "610":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/s04d.png");
                downloadImage.execute();
                break;

            case "611":
            case "612":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/s05d.png");
                downloadImage.execute();
                break;

            case "700":
            case "711":
            case "721":
            case "731":
            case "741":
            case "751":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/a03d.png");
                downloadImage.execute();
                break;

            case "800":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/c01d.png");
                downloadImage.execute();
                break;

            case "801":
            case "802":
            case "803":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/c02d.png");
                downloadImage.execute();
                break;

            case "804":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/c04d.png");
                downloadImage.execute();
                break;

            case "900":
                //Get the image from the URL
                downloadImage = new AsyncTask_GetWeatherIcon(imageView, "https://www.weatherbit.io/static/img/icons/u00d.png");
                downloadImage.execute();
                break;

        }

    }

    public int getDateOffset(String datetime) {
        String current_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int max_days = 16;

        if (current_date == datetime)
            return 0;

        int time_distance = 0;
        int current_year;
        int current_month;
        int current_day;
        int stored_year;
        int stored_month;
        int stored_day;

        try {
            current_year = Integer.parseInt(current_date.substring(0, 3));
            current_month = Integer.parseInt(current_date.substring(4, 5));
            current_day = Integer.parseInt(current_date.substring(6, 7));

            stored_year = Integer.parseInt(datetime.substring(0, 3));
            stored_month = Integer.parseInt(datetime.substring(4, 5));
            stored_day = Integer.parseInt(datetime.substring(6, 7));
        } catch (NumberFormatException nfe) {
            return 0;
        }

        //Find year difference
        if (current_year - stored_year == 0) {
            if (current_month - stored_month == 0) {
                time_distance = current_day - stored_day;
            } else {

                if (current_month - stored_month == 1 && current_day <= max_days) {
                    time_distance = current_day + (31 - stored_day);
                }
            }
        } if (current_year - stored_year == 1 && stored_month == 12) {
            time_distance = current_day + (31 - stored_day);
        }

        return time_distance;
    }

    //Class adapted from modified code from https://inducesmile.com/android-tutorials-for-nigerian-developer/android-load-image-from-url/
    public class AsyncTask_GetWeatherIcon extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;
        String ImageURL;

        AsyncTask_GetWeatherIcon(ImageView imageView, String ImageURL) {
            this.imageView = imageView;
            this.ImageURL = ImageURL;

        }

        @Override
        protected void onPreExecute() {}

        protected Bitmap doInBackground(String... arg0) {
            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(ImageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception ex) {
                // Exception in loading image.
                Context context = getApplicationContext();
                CharSequence ToastText = "Error in loading image..." + ex.toString();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, ToastText, duration);
                toast.show();
            }

            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }


}
