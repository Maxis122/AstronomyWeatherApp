package com.example.max.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.AsyncTask;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    //Doubles for longitude and latitude.
    double lat = 0;
    double longi = 0;

    //String to store current location as a postcode.
    String currentLocation = "";

    //Local Storage filename
    String locationFileName = "locationData";

    //Array list to store Json values
    ArrayList<String> items = new ArrayList<String>();

    //Json String
    String json_data = "";

    //JSON TAGS for Postcode.io
    private static final String TAG_LONGITUDE = "longitude";
    private static final String TAG_LATITUDE  = "latitude";
    private static final String TAG_POSTCODE = "postcode";
    private static final String TAG_RESULT = "result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

                //Set the current location textView widget to now show the current location.
                TextView currentLoc = (TextView)findViewById(R.id.text_CurrentLocation);
                currentLoc.setText("Current Location:" + currentLocation);

                //Set the current longitude textView widget to now show the current location.
                TextView currentLongi = (TextView)findViewById(R.id.text_CurrentLongi);
                currentLongi.setText("Current Longi:" + longi);

                //Set the current latitude textView widget to now show the current location.
                TextView currentLat = (TextView)findViewById(R.id.text_CurrentLat);
                currentLat.setText("Current Lat:" + lat);


            } catch (IOException ioe) {

            }

        }

    }

    public void goto_Back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void get_Location(View view) {
        //If we haven't gotten permission, attempt to get permission.
        if( ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        //We now check if the permission has been granted, if not, we don't try to get location.
        if( ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //If the permission was granted, then we can get the location.

            try {
                //Get a reference to the locationManager
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                //Get the last know location from the locationManager
                String locationProv = LocationManager.GPS_PROVIDER;
                Location lastLoc = locationManager.getLastKnownLocation(locationProv);

                //Get the latitude and the longitude
                lat = lastLoc.getLatitude();
                longi = lastLoc.getLongitude();

                //Set the current longitude textView widget to now show the current location.
                TextView currentLongi = (TextView)findViewById(R.id.text_CurrentLongi);
                currentLongi.setText("Current Longi:" + longi);

                //Set the current latitude textView widget to now show the current location.
                TextView currentLat = (TextView)findViewById(R.id.text_CurrentLat);
                currentLat.setText("Current Lat:" + lat);

                //Get the postcode from the lat and longi
                new AsyncTask_GetPostCode().execute();
            } catch (Exception ex) {
                // Exception in saving.
                Context context = getApplicationContext();
                CharSequence text = "Error: " + ex.toString();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }



        }
    }

    public void get_LocationPostcode(View view) {
        //Get the postcode from the EditText widget
        EditText postEdit = (EditText)findViewById(R.id.text_Postcode);
        currentLocation = postEdit.getText().toString();

        //Set the current location textView widget to now show the current location.
        TextView currentLoc = (TextView)findViewById(R.id.text_CurrentLocation);
        currentLoc.setText("Current Location:" + currentLocation);

        //Get the lat and longi from the api
        new AsyncTask_GetLongiLat().execute();



    }

    public void ApplyChanges(View view) {
        try {
            //Open a filestream and load the file.
            FileOutputStream outputStream;

            //Create/Open the file in private mode.
            outputStream = openFileOutput(locationFileName, MODE_PRIVATE);

            //Write last location, lat and long
            outputStream.write((currentLocation + "\n").getBytes());
            outputStream.write((longi + "\n").getBytes());
            outputStream.write((lat + "\n").getBytes());

            //Close the file
            outputStream.close();

            // show toast message for successful save
            Context context = getApplicationContext();
            CharSequence text = "Changes saved!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        catch(Exception ex) {
            // Exception in saving.
            Context context = getApplicationContext();
            CharSequence text = "Error in saving..." + ex.toString();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                //If the request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission was granted.
                } else {
                    //Permission was denied.
                    Toast.makeText(SettingsActivity.this,
                            "Location permission denied. Cannot access location!",
                            Toast.LENGTH_SHORT).show();
                }
        }
        return;
    }

    public class AsyncTask_GetPostCode extends AsyncTask<String, String, String> {
        @Override
        // this method is used for......................
        protected void onPreExecute() {}

        @Override
        // this method is used for...................
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();

                // get json string from service url
                json_data = jParser.getPostFromLatLongi(lat, longi);

                //Create JSON Object
                JSONObject PostcodeObject = new JSONObject(json_data);
                JSONArray postcodes = PostcodeObject.getJSONArray(TAG_RESULT);
                JSONObject firstPostcode = postcodes.getJSONObject(0);

                //Get the postcode from the object
                currentLocation = firstPostcode.getString(TAG_POSTCODE);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            //Set the current location textView widget to now show the current location.
            TextView currentLoc = (TextView)findViewById(R.id.text_CurrentLocation);
            currentLoc.setText("Current Location:" + currentLocation);
        }
    }

    public class AsyncTask_GetLongiLat extends AsyncTask<String, String, String> {
        @Override
        // this method is used for......................
        protected void onPreExecute() {}

        @Override
        // this method is used for...................
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();

                // get json string from service url
                json_data = jParser.getLongLatFromPost(currentLocation);

                //Create JSON Object
                JSONObject LocationObject = new JSONObject(json_data);
                JSONObject location = LocationObject.getJSONObject(TAG_RESULT);

                //Get the postcode from the object
                lat = location.getDouble(TAG_LATITUDE);
                longi = location.getDouble(TAG_LONGITUDE);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            //Set the current longitude textView widget to now show the current location.
            TextView currentLongi = (TextView)findViewById(R.id.text_CurrentLongi);
            currentLongi.setText("Current Longi:" + longi);

            //Set the current latitude textView widget to now show the current location.
            TextView currentLat = (TextView)findViewById(R.id.text_CurrentLat);
            currentLat.setText("Current Lat:" + lat);

        }
    }

}
