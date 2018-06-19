package com.example.max.myapplication;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;

import java.io.FileOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Local Storage filename
    String NASAAstroidData = "astroidData";

    //Json String
    String json_data = "";

    //JSON TAGS for NASA API
    private static final String TAG_ECOUNT = "element_count";
    private static final String TAG_NEO = "near_earth_objects";
    private static final String TAG_NAME = "name";
    private static final String TAG_URL = "nasa_jpl_url";


    // Array list to store the name data from the astroids
    ArrayList<String> astroid_names = new ArrayList<String>();
    ArrayList<String> astroid_url = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get data
        //Get the data from NASA API
        new AsyncTask_GetNASAAstroidData().execute();





    }

    public void goto_Settings(View view) {
        //Goto the settings activity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void getNASAPicture(View view) {
        //Goto the NASA Image activity
        Intent intent = new Intent(this, NASAImageActivity.class);
        startActivity(intent);
    }

    public void get_Weather(View view) {
        //Goto the NASA Image activity
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
    }

    public class AsyncTask_GetNASAAstroidData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0)  {
            // create new instance of the httpConnect class
            httpConnect jParser = new httpConnect();

            //Get the current date
            String current_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            // get json string from service url
            json_data = jParser.getAstroid(current_date);

            //Save the json data to file
            try {
                //Open a filestream and load the file.
                FileOutputStream outputStream;

                //Create/Open the file in private mode.
                outputStream = openFileOutput(NASAAstroidData, MODE_PRIVATE);

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


            try {
                //Create JSON Object
                JSONObject NASAObject = new JSONObject(json_data);
                JSONObject NEOS = NASAObject.getJSONObject(TAG_NEO);
                JSONArray JNeos = NEOS.getJSONArray(current_date);
                int element_count = NASAObject.getInt(TAG_ECOUNT);

                //Loop through JSON Array and get data.
                for (int i = 0; i < element_count; i++) {
                    JSONObject json_grab = JNeos.getJSONObject(i);

                    if (json_grab != null) {
                        astroid_names.add(json_grab.getString(TAG_NAME));
                        astroid_url.add(json_grab.getString(TAG_URL));
                    }
                }



            } catch (JSONException je) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            //Bind the arrays to the listView
            ListView list = findViewById(R.id.Astroid_List);

            ArrayAdapter<String> astroidArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, astroid_names);
            list.setAdapter(astroidArrayAdapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(astroid_url.get(position)));
                    startActivity(intent);
                }
            });

        }

    }

}
