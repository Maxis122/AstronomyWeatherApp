package com.example.max.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;
import java.io.InputStream;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NASAImageActivity extends AppCompatActivity {

    //Strings to hold data from NASA API
    String Title = "";
    String Date = "";
    String Copyright = "";
    String Description = "";
    String ImageURL = "";

    //Json String
    String json_data = "";

    //Local Storage filename
    String NASAFileName = "NASAJSONData";

    //JSON TAGS for NASA API
    private static final String TAG_COPYRIGHT = "copyright";
    private static final String TAG_DATE = "date";
    private static final String TAG_EXPLANATION = "explanation";
    private static final String TAG_TITLE = "title";
    private static final String TAG_IMAGEURL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nasaimage);

        //Set the description to have a scrolling text layout in case of large description.
        TextView explainView = (TextView)findViewById(R.id.text_Explain);
        explainView.setMovementMethod(new ScrollingMovementMethod());

        //Get reference to imageView
        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        //Check if there is an internet connection
        Context context = getApplicationContext();
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            //We are connected to a network, run async tasks
            //Get the data from NASA API
            new AsyncTask_GetNASAData().execute();

            //Get the image from the URL
            AsyncTask_GetNASAPicture downloadImage = new AsyncTask_GetNASAPicture(imageView);
            downloadImage.execute();
        }
        else {
            //We are not connected, check for file and load data. Elsewise we show a toast and fall back.
            //Check for local storage
            File file = getFileStreamPath(NASAFileName);
            if(file.exists()) {
                try {
                    //If we have data in local storage, load it.
                    FileInputStream fIn = openFileInput(NASAFileName);
                    InputStreamReader isr = new InputStreamReader(fIn);
                    BufferedReader buffreader = new BufferedReader(isr);

                    // Loop through the file, finding the last location, longitude and latitude
                    json_data = buffreader.readLine();

                    isr.close();

                    //Create JSON Object
                    JSONObject NASAImageObject = new JSONObject(json_data);

                    //Get the URL of the picture from the object
                    Title = NASAImageObject.getString(TAG_TITLE);
                    Date = NASAImageObject.getString(TAG_DATE);
                    Copyright = NASAImageObject.getString(TAG_COPYRIGHT);
                    Description = NASAImageObject.getString(TAG_EXPLANATION);
                    ImageURL = NASAImageObject.getString(TAG_IMAGEURL);

                    //Set the title text
                    TextView text = (TextView) findViewById(R.id.text_Title);
                    text.setText(Title);

                    //Set the date text
                    text = (TextView) findViewById(R.id.text_Date);
                    text.setText(Date);

                    //Set the date text
                    text = (TextView) findViewById(R.id.text_Copyright);
                    text.setText(Copyright);

                    //Set the date text
                    text = (TextView) findViewById(R.id.text_Explain);
                    text.setText(Description);


                } catch (IOException ioe) {

                } catch (JSONException je) {

                }

            }
        }


    }

    public void goto_Back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Class adapted from modified code from https://inducesmile.com/android-tutorials-for-nigerian-developer/android-load-image-from-url/
    public class AsyncTask_GetNASAPicture extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;

        AsyncTask_GetNASAPicture(ImageView imageView) {
            this.imageView = imageView;
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

    public class AsyncTask_GetNASAData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();

                // get json string from service url
                json_data = jParser.getNASAPicture();

                //Save the json data to file
                try {
                    //Open a filestream and load the file.
                    FileOutputStream outputStream;

                    //Create/Open the file in private mode.
                    outputStream = openFileOutput(NASAFileName, MODE_PRIVATE);

                    //Write last location, lat and long
                    outputStream.write((json_data).getBytes());

                    //Close the file
                    outputStream.close();
                }
                catch(Exception ex) {
                    // Exception in saving.
                    Context context = getApplicationContext();
                    CharSequence text = "Error in saving..." + ex.toString();
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                //Create JSON Object
                JSONObject NASAImageObject = new JSONObject(json_data);


                //Get the URL of the picture from the object
                Title = NASAImageObject.getString(TAG_TITLE);
                Date = NASAImageObject.getString(TAG_DATE);
                //Copyright = NASAImageObject.getString(TAG_COPYRIGHT);
                Description = NASAImageObject.getString(TAG_EXPLANATION);
                ImageURL = NASAImageObject.getString(TAG_IMAGEURL);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            //Set the title text
            TextView text = (TextView)findViewById(R.id.text_Title);
            text.setText(Title);

            //Set the date text
            text = (TextView)findViewById(R.id.text_Date);
            text.setText(Date);

            //Set the date text
            text = (TextView)findViewById(R.id.text_Copyright);
            text.setText(Copyright);

            //Set the date text
            text = (TextView)findViewById(R.id.text_Explain);
            text.setText(Description);




        }
    }
}
