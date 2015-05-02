package com.zhengfood.pointtoanything;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SinglePlaceActivity extends Activity {
    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogFragment alert = new AlertDialogFragment();

    // Google Places
    GooglePlaces googlePlaces;

    // Place Details
    PlaceDetails placeDetails;

    // Progress dialog
    ProgressDialog pDialog;

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_place);

        Intent i = getIntent();

        // Place referece id
        String reference = i.getStringExtra(KEY_REFERENCE);

        Log.d("Reference", reference);

        // Calling a Async Background thread
        new LoadSinglePlaceDetails().execute(reference);
    }


    /**
     * Background Async Task to Load Google places
     * */
    class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SinglePlaceActivity.this);
            pDialog.setMessage("Loading profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Profile JSON
         * */
        protected String doInBackground(String... args) {
            String reference = args[0];

            // creating Places class object
            googlePlaces = new GooglePlaces();

            // Check if used is connected to Internet
            try {
                placeDetails = googlePlaces.getPlaceDetails(reference);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
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
                    if(placeDetails != null){
                        String status = placeDetails.status;

                        // check place deatils status
                        // Check for all possible status
                        if(status.equals("OK")){
                            if (placeDetails.result != null) {
                                // Displaying all the details in the view
                                // single_place.xml
                                TextView lbl_name = (TextView) findViewById(R.id.name);
                                TextView lbl_address = (TextView) findViewById(R.id.address);
                                TextView lbl_phone = (TextView) findViewById(R.id.phone);
                                TextView lbl_types = (TextView) findViewById(R.id.types);
                                TextView lbl_openNow = (TextView) findViewById(R.id.openNow);
                                TextView lbl_weekdayText = (TextView) findViewById(R.id.weekday);
                                TextView lbl_rating = (TextView) findViewById(R.id.rating);
                                TextView lbl_location = (TextView) findViewById(R.id.location);

                                String name = placeDetails.result.name;
                                String[] types= placeDetails.result.types;
                                String address = placeDetails.result.formatted_address;
                                String phone = placeDetails.result.formatted_phone_number;
                                String openNow;
                                String[] weekdayText;
                                String weekdayOpen = "\n";
                                if (placeDetails.result.opening_hours != null) {
                                    openNow = String.valueOf(placeDetails.result.opening_hours.open_now);
                                    weekdayText = placeDetails.result.opening_hours.weekday_text;
                                    for (String i : weekdayText){
                                        weekdayOpen = weekdayOpen + i + "\n";
                                    }

                                } else {
                                    openNow = "Not present";
                                    weekdayOpen = "Not present";
                                    lbl_openNow.setVisibility(View.GONE);
                                    lbl_weekdayText.setVisibility(View.GONE);

                                }

                                String rating = String.valueOf(placeDetails.result.rating);
                                String latitude = Double.toString(placeDetails.result.geometry.location.lat);
                                String longitude = Double.toString(placeDetails.result.geometry.location.lng);



                                Log.d("Place ", name + address + phone + latitude + longitude);





                                // Check for null data from google
                                // Sometimes place details might missing
                                name = name == null ? "Not present" : name; // if name is null display as "Not present"
                                address = address == null ? "Not present" : address;
                                phone = phone == null ? "Not present" : phone;
                                latitude = latitude == null ? "Not present" : latitude;
                                longitude = longitude == null ? "Not present" : longitude;
                                types[0] = types[0] == null ? "Not present": types[0];
                                rating = rating == "0.0" ? "Not present": rating;

                                String strTypes = "";
                                for (String i : types){
                                    strTypes = strTypes + i  + "; ";
                                }


                                lbl_name.setText(name);
                                lbl_address.setText(address);
                                lbl_phone.setText(Html.fromHtml("<b>Phone:</b> " + phone));
                                lbl_types.setText(Html.fromHtml("<b>Types:</b> " + strTypes));
                                lbl_openNow.setText(Html.fromHtml("<b>Opening Now:</b> " + openNow));
                                lbl_weekdayText.setText("Weekday opening: " + weekdayOpen);
                                lbl_rating.setText(Html.fromHtml("<b>Rating:</b> " + rating));

                                lbl_location.setText(Html.fromHtml("<b>Latitude:</b> " + latitude + ", <b>Longitude:</b> " + longitude));
                            }
                        }
                        else if(status.equals("ZERO_RESULTS")){
                            alert.setAttribute(getFragmentManager(), "Near Places",
                                    "Sorry no place found.",
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
                    }else{
                        alert.setAttribute(getFragmentManager(), "Places Error",
                                "Sorry error occured.",
                                false);
                    }


                }
            });

        }

    }

}
