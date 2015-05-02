package com.zhengfood.pointtoanything;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity implements SensorEventListener{

    Boolean isInternetPresent = false;

    ConnectionDetector connectionDetector;

    AlertDialogFragment alert = new AlertDialogFragment();

    GooglePlaces googlePlaces;

    PlacesList nearPlaces;

    GPSTracker gps;

    ProgressDialog pDialog;

    ImageView imageView;

    ListView lv;

    private SensorManager mSensorManager;
    static final float ALPHA = 0.25f;
    private static final int ANGLE_RANGE = 10;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private TextView directionText;
    private float currentDegree=0f;

    private Set<HashMap<String, String>> angleHashSet = new HashSet<>();
    private ArrayList<HashMap<String, String>> angleListItems;



    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<>();

    public static String KEY_REFERENCE = "reference";
    public static String KEY_NAME = "name";
    public static String KEY_PHOTO_REFERENCE= "photo_reference";
    public static String KEY_LATITUDE = "latitude";
    public static String KEY_LONGITUDE = "longitude";
    public static String KEY_VICINITY = "vicinity";
    public static String KEY_ICON = "icon";
    public static String KEY_TYPEs = "types";
    public static String KEY_DISTANCE = "distance";
    public static String KEY_ANGlE =  "angle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionDetector = new ConnectionDetector(getApplicationContext());
        gps = new GPSTracker(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(gps.canGetLocation()){
            Log.d("Your Location", "Latitude: " +gps.getLatitude() +
                    ". longitude: " +gps.getLongitude());


        } else {
            alert.setAttribute(getFragmentManager(),
                    "GPS Status", "Couldn't get location information. Please enable GPS", false);

            return;
        }


        isInternetPresent = connectionDetector.isConnectingToInternet();
        if(!isInternetPresent){
            alert.setAttribute(getFragmentManager(), "Internet Connection Error",
                    "Please connect to working Internet", false);
            return;
        }

        lv = (ListView)findViewById(R.id.list);
        imageView = (ImageView)findViewById(R.id.image);
        directionText = (TextView) findViewById(R.id.direction);

        imageView.setImageResource(R.drawable.compass);


        //new ImageHandler(MainActivity.this, imageView).execute("http://www.djvale.info/images/background.png");


        new LoadPlaces().execute();



        /**
         * ListItem click event
         * On selecting a listitem SinglePlaceActivity is launched
         * */
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);

                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                startActivity(in);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer){
            mLastAccelerometer = lowpass(event.values.clone(), mLastAccelerometer);
        }
        else if(event.sensor == mMagnetometer){
            mLastMagnetometer = lowpass(event.values.clone(), mLastMagnetometer);
        }

        if(mLastMagnetometer != null && mLastAccelerometer != null){
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
                    mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegrees = Math.round((float)
                    (Math.toDegrees(azimuthInRadians) + 360)%360);
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -azimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
            );
            ra.setDuration(400);
            ra.setFillAfter(true);
            imageView.startAnimation(ra);
            currentDegree = -azimuthInDegrees;
            directionText.setText("Direction: " + azimuthInDegrees);

            if (placesListItems != null) {
                int count = 0;
                for(HashMap<String, String> hashMap : placesListItems) {
                    if (Math.abs(Float.valueOf(hashMap.get(KEY_ANGlE).substring(0,5)) - azimuthInDegrees) <= ANGLE_RANGE) {
                        angleHashSet.add(hashMap);
                    }
                }

                for (HashMap<String, String> angleHashMap : angleHashSet) {
                    if (Math.abs(Float.valueOf(angleHashMap.get(KEY_ANGlE).substring(0,5)) - azimuthInDegrees) > ANGLE_RANGE) {
                        angleHashSet.remove(angleHashMap);
                        break;
                    }
                }

                angleListItems = new ArrayList<>(angleHashSet);
                Collections.sort(angleListItems, hashMapComparator);

                // list adapter
                ListAdapter adapter = new SimpleAdapter(MainActivity.this,  angleListItems,
                        R.layout.list_item,
                        new String[] { KEY_REFERENCE, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE, KEY_DISTANCE, KEY_ANGlE}, new int[] {
                        R.id.reference, R.id.name, R.id.latitude, R.id.longitude, R.id.distance, R.id.angle});

                // Adding data into listview
                lv.setAdapter(adapter);

            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            try {
                // Separeate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                //
                String types = ""; // Listing places only cafes, restaurants

                // Radius in meters - increase this value if you don't find any places
                double radius = 300; // 1000 meters

                // get nearest places
                nearPlaces = googlePlaces.search(gps.getLatitude(),
                        gps.getLongitude(), radius, types);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;

                    // Check for all possible status
                    if(status.equals("OK")){
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            for (Place p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                // Place reference won't display in listview - it will be hidden
                                // Place reference is used to get "place full details"
                                map.put(KEY_REFERENCE, p.reference);

                                // Place location
                                map.put(KEY_LATITUDE, String.valueOf(p.geometry.location.lat));
                                map.put(KEY_LONGITUDE, String.valueOf(p.geometry.location.lng));

                                // Place icon
                                map.put(KEY_ICON, p.icon);
                                // Place name
                                map.put(KEY_NAME, p.name);
                                // Place distance and angle
                                Location locationA = new Location("pointA");
                                locationA.setLatitude(p.geometry.location.lat);
                                locationA.setLongitude(p.geometry.location.lng);
                                String distance = String.valueOf(gps.getLocation().distanceTo(locationA)).substring(0,5)+ " m";
                                String angle = String.valueOf((gps.getLocation().bearingTo(locationA)+360)%360).substring(0,5)+"  \u030a";

                                map.put(KEY_DISTANCE, distance);

                                map.put(KEY_ANGlE, angle);

                                // adding HashMap to ArrayList
                                placesListItems.add(map);
                            }

                        }
                    }
                    else if(status.equals("ZERO_RESULTS")){
                        // Zero results found
                        alert.setAttribute(getFragmentManager(), "Near Places",
                                "Sorry no places found. Try to change the types of places",
                                false);
                    }
                    else if(status.equals("UNKNOWN_ERROR"))
                    {
                        alert.setAttribute(getFragmentManager(), "Places Error",
                                "Sorry unknown error occured.",
                                false);
                    }
                    else if(status.equals("OVER_QUERY_LIMIT"))
                    {
                        alert.setAttribute(getFragmentManager(), "Places Error",
                                "Sorry query limit to google places is reached",
                                false);
                    }
                    else if(status.equals("REQUEST_DENIED"))
                    {
                        alert.setAttribute(getFragmentManager(), "Places Error",
                                "Sorry error occured. Request is denied",
                                false);
                    }
                    else if(status.equals("INVALID_REQUEST"))
                    {
                        alert.setAttribute(getFragmentManager(), "Places Error",
                                "Sorry error occured. Invalid Request",
                                false);
                    }
                    else
                    {
                        alert.setAttribute(getFragmentManager(), "Places Error",
                                "Sorry error occured.",
                                false);
                    }
                }
            });

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected float[] lowpass(float[] input, float[] output){
        if (output == null) return input;

        for (int i=0; i<input.length;i++){
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public Comparator<HashMap<String, String>> hashMapComparator = new Comparator<HashMap<String, String>>() {
        @Override
        public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
            return lhs.get(KEY_DISTANCE).compareTo(rhs.get(KEY_DISTANCE));
        }
    };






}
