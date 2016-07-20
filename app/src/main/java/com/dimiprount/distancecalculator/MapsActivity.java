package com.dimiprount.distancecalculator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*These commented out is to show my location when the app opens and when moving*/
public class MapsActivity extends AppCompatActivity /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener*/  {

    // Define a request code to send to Google Play services. This code is returned in Activity.onActivityResult
   //   private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker mLocA, mLocB;
    /* private GoogleApiClient gac;    // The latest client in Google Play Services that was designed to make this and other functions from Google Play Services easier to setup and use.
     public static final String TAG = MapsActivity.class.getSimpleName();
     private LocationRequest lr; */
    final Context context = this;
    List<Address> lAddressA, lAddressB;
    Address adrA, adrB;
    LatLng latLngA, latLngB;
    EditText etLocationA, etLocationB;
    String strLocationA, strLocationB;
    TextView tvDisDur;
    ProgressDialog prDialog;
    Button bGoA, bGoB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

         UiSettings mapSettings;
         mapSettings = mMap.getUiSettings();
         mapSettings.setZoomControlsEnabled(true);
         mapSettings.setCompassEnabled(true);
         mapSettings.isCompassEnabled();

        tvDisDur = (TextView) findViewById(R.id.tvDisDur);
        bGoA = (Button) findViewById(R.id.bGoA);
        bGoB = (Button) findViewById(R.id.bGoB);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            Toast.makeText(this, "You are connected to the Internet!", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setMessage("Internet is off. Do you want to turn on Wi-fi or Data?")
                    .setCancelable(false)
                    .setNegativeButton("Wi-fi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent iWf = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            context.startActivity(iWf);
                        }
                    })
                    .setPositiveButton("Data", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent iDt = new Intent(Settings.ACTION_SETTINGS);
                            context.startActivity(iDt);
                        }
                    });


            // create alert dialog
            AlertDialog ad = adb.create();

            // show it
            ad.show();
        }

        // Initialize gac
      /*  gac = new GoogleApiClient.Builder(this) // Create a new GoogleApiClient object using the Builder pattern that you may have seen in Android with things like AlertDialogs.
                .addConnectionCallbacks(this)   // Handle connection stuff. Registers a listener to receive connection events from this GoogleApiClient
                .addOnConnectionFailedListener(this)   // Handle connection stuff. Adds a listener to register to receive connection failed events from this GoogleApiClient.
                .addApi(LocationServices.API)    // Add the LocationServices API endpoint from GooglePlayServices
                .build();   // The client is built for us

        // Create the LocationRequest object
        lr = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)    // Request as accurate a location as possible. Requires more power and time
                .setInterval(10000)        // 10 seconds, in milliseconds. e.g. a navigation app. More power. Lower interval
                .setFastestInterval(1000); // 1 second, in milliseconds. The app can passively listen to any location updates, which doesnâ€™t cost any extra power. The fastest interval*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {
            if (tvDisDur.length() == 0) {
                Toast.makeText(this, "Nothing to save!", Toast.LENGTH_SHORT).show();
            } else {
                boolean didItWork = true;
                try {
                    String metLocA = etLocationA.getText().toString();
                    String metLocB = etLocationB.getText().toString();
                    String mCDD = tvDisDur.getText().toString();

                    DbDatabase entry = new DbDatabase(this);
                    entry.open();
                    entry.createentry(metLocA, metLocB, mCDD);
                    entry.close();

                } catch (Exception e) {
                    didItWork = false;
                    Toast.makeText(this, "Data hasn't been saved. Please try again!", Toast.LENGTH_SHORT).show();
                } finally {
                    if (didItWork = true)
                        Toast.makeText(this, "Data has been saved successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (id == R.id.seeSaved)
            startActivity(new Intent(this, DbView.class));

        if(id == R.id.mtNormal)
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(id == R.id.mtHybrid)
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if(id == R.id.mtTerrain)
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {  // Whenever we add code in onResume() we want to think about adding corresponding code in onPause(). We donâ€™t always need something, but in this case we want to disconnect from location services when our Activity is paused. Then we will be reconnected if the Activity is resumed again.
        super.onPause();
     /*   if (gac.isConnected()) {    // Call onConnected()
            LocationServices.FusedLocationApi.removeLocationUpdates(gac, this); // Just like I have to disconnect the client, I also need to explicitly remove location updates after requesting them.
            gac.disconnect();
        }*/
    }

    @Override
    protected void onResume() { // When the activity may be paused at any time, like when a call or text message comes in, for example. So we want to pause and then resume any activity, such as network connections or location updates like this.
        super.onResume();
        setUpMapIfNeeded();
        // gac.connect();
    }

    public void onGoLocA(View v) {
        // mMap.clear();   // Delete everything (markers and polyline) from map when go button is pressed
        // Hide keyboard when icon bGoA is pushed
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(bGoA.getWindowToken(), 0);
        etLocationA = (EditText) findViewById(R.id.etLocationA);
        strLocationA = etLocationA.getText().toString();
        lAddressA = null;
        boolean didItWork = true;

        Geocoder gcA = new Geocoder(this);
        try {
            lAddressA = gcA.getFromLocationName(strLocationA, 1);    // Generate longitude and latitude coordinates.Second parameter: maximum addresses
        } catch (IOException e) {
            didItWork = false;
            e.printStackTrace();
        }

        // If I put it 8 lines if code below, it doesn't show the marker
        if (mLocA != null)   // If there is already a marker from etLocationA, remove it.
            mLocA.remove();

        if (lAddressA == null || lAddressA.size() == 0) {  // If the user hasn't entered location or the location doesn't exist
            Toast.makeText(this, "This location doesn't exist!", Toast.LENGTH_SHORT).show();
        } else {
            adrA = lAddressA.get(0);        // Stores the latitude and longitude. 0: fetch the first value
            latLngA = new LatLng(adrA.getLatitude(), adrA.getLongitude());
            mLocA = mMap.addMarker(new MarkerOptions().position(latLngA).title(strLocationA)); // Marker that is setting the location
            CameraPosition cpA = new CameraPosition.Builder()
                    .target(latLngA) // Sets the center of the map to latLngA
                    .zoom(9)                   // Sets the zoom
                    .bearing(0) // Sets the orientation of the camera
                    .tilt(0)    // Sets the tilt of the camera to 0 degrees
                    .build();    // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpA));
        }
    }

    public void onGoLocB(View v) {
        //   mMap.clear();   // Delete everything (markers and polyline) from map when go button is pressed
        // Hide keyboard when icon bGoB is pushed
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(bGoB.getWindowToken(), 0);
        etLocationB = (EditText) findViewById(R.id.etLocationB);
        strLocationB = etLocationB.getText().toString();
        lAddressB = null;

        Geocoder gcB = new Geocoder(this);
        try {
            lAddressB = gcB.getFromLocationName(strLocationB, 1);    // Generate longitude and latitude coordinates.Second parameter: maximum addresses
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If I put it 8 lines if code below, it doesn't show the marker
        if (mLocB != null)   // If there is already a marker from etLocationB , remove it.
            mLocB.remove();

        if (lAddressB == null || lAddressB.size() == 0) {  // If the user hasn't entered location or the locaion doesn't exist
            Toast.makeText(this, "This location doesn't exist!", Toast.LENGTH_SHORT).show();
        } else {
            adrB = lAddressB.get(0);//lAddressB.get(0);  // Stores the latitude and longitude. 0: fetch the first value
            latLngB = new LatLng(adrB.getLatitude(), adrB.getLongitude());
            mLocB = mMap.addMarker(new MarkerOptions().position(latLngB).title(strLocationB)); // Marker that is setting the location
            CameraPosition cpB = new CameraPosition.Builder()
                    .target(latLngB) // Sets the center of the map to latLngB
                    .zoom(9)                   // Sets the zoom
                    .bearing(0) // Sets the orientation of the camera
                    .tilt(0)    // Sets the tilt of the camera to 0 degrees
                    .build();    // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpB));
        }
    }

    public void onbDistCalc(View v) {

        if (etLocationA == null || etLocationB == null)
            Toast.makeText(this, "Enter location!", Toast.LENGTH_SHORT).show();

        else {
            String stUrl = getDirectionsUrl(latLngA, latLngB);      // Get URL to the Google Directions API

            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(stUrl);        // Start downloading json data from Google Directions API

            CameraPosition cpCD = new CameraPosition.Builder()
                    .target(latLngB) // Sets the center of the map to latLngB
                    .zoom(6)                   // Sets the zoom
                    .bearing(0) // Sets the orientation of the camera
                    .tilt(0)    // Sets the tilt of the camera to 0 degrees
                    .build();    // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpCD));
        }
    }

    private String getDirectionsUrl(LatLng latLngA, LatLng latLngB) {
        String sOrigin = "origin=" + latLngA.latitude + "," + latLngA.longitude;       // Origin of route
        String sDest = "destination=" + latLngB.latitude + "," + latLngB.longitude;        // Destination of route
        String sSensor = "sensor=false";      // Don't update the information
        String sParameters = sOrigin + "&" + sDest + "&" + sSensor;     // Build the parameters to the web service
        String sOutput = "json";     // Output format
        String sUrl = "https://maps.googleapis.com/maps/api/directions/" + sOutput + "?" + sParameters;      // Build the url to the web service
        return sUrl;
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link //MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //   mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker")); // Marker that is setting the location
        //   mMap.setMyLocationEnabled(true);    // Find my location with the button with the target (My location button)
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {     // Fetch data from url passed
        /*public void execute(String stUrl) {
        }*/

        @Override
        protected String doInBackground(String... params) {     // Download data in non-ui thread
            String sData = "";       // Store data from web service

            try {
                sData = downloadUrl(params[0]);     // Fetching the data from web service
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return sData;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            prDialog = new ProgressDialog(context);
            prDialog.setMessage("Please wait...");
            prDialog.setIndeterminate(true);        // A dialog showing a progress indicator and an optional text message or view
            prDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {       // Executes in UI thread, after the execution of doInBackground()
            super.onPostExecute(result);
            prDialog.hide();
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);     // Invokes the thread for parsing the JSON data

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String stData = "";
        InputStream iStream = null;     // InputStream: A readable source of bytes
        HttpURLConnection urlConnection = null;     // HttpURLConnection: Send and receive data over the web
        try {
            URL url = new URL(strUrl);       // A Uniform Resource Locator that identifies the location of an Internet resource as specified by RFC 1738
            urlConnection = (HttpURLConnection) url.openConnection();       // Create an http connection to communicate with url
            urlConnection.connect();        // Connect to url
            iStream = urlConnection.getInputStream();       // Reading data from url.
            // getInputStream(): Use input streams that reads data from the network and return an InputStream for reading data from the resource pointed by this URLConnection

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));       // BufferedReader: wraps an existing Reader and buffers the input

            StringBuilder sb = new StringBuilder();       // A modifiable sequence of characters for use in creating strings

            String line = "";
            while ((line = br.readLine()) != null) {     // The next line of text available from this reader. BufferedReader.readLine(): we use it to convert the InputStream to String
                sb.append(line);
            }

            stData = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception downloading", e.toString());
        } finally {
            // End reading...............

            // Disconnect the HttpURLConnection

            iStream.close();
            urlConnection.disconnect();
        }
        return stData;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {       // A class to parse the Google Places in JSON format
        // HashMap: All optional operations are supported.
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... params) {        // Parse the data in non-ui thread
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(params[0]);
                JSONParser parser = new JSONParser();
                routes = parser.parse(jObject);     // Start parsing data
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            if (result.size() < 1) {        // If there is no path
                Toast.makeText(getBaseContext(), "No such path", Toast.LENGTH_SHORT).show();
                return;
            } else {

                mMap.clear();
                mLocA = mMap.addMarker(new MarkerOptions().position(latLngA).title(strLocationA));
                mLocB = mMap.addMarker(new MarkerOptions().position(latLngB).title(strLocationB));
                for (int i = 0; i < result.size(); i++) {       // Traverse through all the routes
                    points = new ArrayList();
                    lineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = result.get(i);     // Fetching i-th route

                    for (int j = 0; j < path.size(); j++) {        // Fetching all the points in i-th route
                        HashMap<String, String> point = path.get(j);

                        if (j == 0) {       // Get distance from the list
                            distance = point.get("distance");
                            continue;
                        } else if (j == 1) {     // Get duration from the list
                            duration = point.get("duration");
                            continue;
                        }

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Add all the points of the route to lineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.BLUE);
                }

                tvDisDur.setText(distance + " (" + duration + ")");

                mMap.addPolyline(lineOptions);      // Drawing polyline in the Google Map for the i-th route
            }
        }
    }

    // Stop and exit app pushing the back button
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);	// Close the app and don't ask which app you want to use
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Move activities to background
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // MapsActivity will start firstly
        startActivity(intent);

        this.finishAffinity();

    }

    /*@Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Location services connected.");
        Location loc = LocationServices.FusedLocationApi.getLastLocation(gac);
        if (loc == null) {  // The last location might be null if this is the first time Google Play Services is checking location, or perhaps for other reasons
        //    LocationServices.FusedLocationApi.requestLocationUpdates(gac, lr, this);    // Request location updates when the last location is not known
        }
        else {
            handleNewLocation(loc);
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {      //  This is especially important when, as mentioned earlier, the last known location is not available from the fused location provider we are using. It also becomes particularly important in an app that tracks the userâ€™s location, like a run-tracking or navigation app. This method gets called every time a new location is detected by Google Play Services
        handleNewLocation(location);
    }

    private void handleNewLocation(Location loc) {
        Log.d(TAG, loc.toString());
        double curLat = loc.getLatitude();
        double curLon = loc.getLongitude();
        LatLng curLatLon = new LatLng(curLat, curLon);

        MarkerOptions marOpt = new MarkerOptions()
                .position(curLatLon)
                .title("My location!");
        mMap.addMarker(marOpt);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(curLatLon));
    }*/


}
