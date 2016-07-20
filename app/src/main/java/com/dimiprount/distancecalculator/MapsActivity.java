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

public class MapsActivity extends AppCompatActivity {

    // Define a request code to send to Google Play services. This code is returned in Activity.onActivityResult
   //   private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap;
    Marker mLocA, mLocB;
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


            AlertDialog ad = adb.create();

            ad.show();
        }

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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onGoLocA(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(bGoA.getWindowToken(), 0);
        etLocationA = (EditText) findViewById(R.id.etLocationA);
        strLocationA = etLocationA.getText().toString();
        lAddressA = null;
        boolean didItWork = true;

        Geocoder gcA = new Geocoder(this);
        try {
            lAddressA = gcA.getFromLocationName(strLocationA, 1);
        } catch (IOException e) {
            didItWork = false;
            e.printStackTrace();
        }

        if (mLocA != null)
            mLocA.remove();

        if (lAddressA == null || lAddressA.size() == 0) {
            Toast.makeText(this, "This location doesn't exist!", Toast.LENGTH_SHORT).show();
        } else {
            adrA = lAddressA.get(0);
            latLngA = new LatLng(adrA.getLatitude(), adrA.getLongitude());
            mLocA = mMap.addMarker(new MarkerOptions().position(latLngA).title(strLocationA));
            CameraPosition cpA = new CameraPosition.Builder()
                    .target(latLngA)
                    .zoom(9)
                    .bearing(0)
                    .tilt(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpA));
        }
    }

    public void onGoLocB(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(bGoB.getWindowToken(), 0);
        etLocationB = (EditText) findViewById(R.id.etLocationB);
        strLocationB = etLocationB.getText().toString();
        lAddressB = null;

        Geocoder gcB = new Geocoder(this);
        try {
            lAddressB = gcB.getFromLocationName(strLocationB, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mLocB != null)
            mLocB.remove();

        if (lAddressB == null || lAddressB.size() == 0) {
            Toast.makeText(this, "This location doesn't exist!", Toast.LENGTH_SHORT).show();
        } else {
            adrB = lAddressB.get(0);
            latLngB = new LatLng(adrB.getLatitude(), adrB.getLongitude());
            mLocB = mMap.addMarker(new MarkerOptions().position(latLngB).title(strLocationB));
            CameraPosition cpB = new CameraPosition.Builder()
                    .target(latLngB)
                    .zoom(9)
                    .bearing(0) 
                    .tilt(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpB));
        }
    }

    public void onbDistCalc(View v) {

        if (etLocationA == null || etLocationB == null)
            Toast.makeText(this, "Enter location!", Toast.LENGTH_SHORT).show();

        else {
            String stUrl = getDirectionsUrl(latLngA, latLngB);

            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(stUrl);

            CameraPosition cpCD = new CameraPosition.Builder()
                    .target(latLngB)
                    .zoom(6)
                    .bearing(0)
                    .tilt(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpCD));
        }
    }

    private String getDirectionsUrl(LatLng latLngA, LatLng latLngB) {
        String sOrigin = "origin=" + latLngA.latitude + "," + latLngA.longitude;
        String sDest = "destination=" + latLngB.latitude + "," + latLngB.longitude; 
        String sSensor = "sensor=false";
        String sParameters = sOrigin + "&" + sDest + "&" + sSensor;
        String sOutput = "json";
        String sUrl = "https://maps.googleapis.com/maps/api/directions/" + sOutput + "?" + sParameters;
        return sUrl;
    }


    
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    
    private void setUpMap() {
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String sData = "";

            try {
                sData = downloadUrl(params[0]);
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
            prDialog.setIndeterminate(true);
            prDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            prDialog.hide();
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String stData = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream(); 

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            stData = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception downloading", e.toString());
        } finally {
           

            iStream.close();
            urlConnection.disconnect();
        }
        return stData;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... params) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(params[0]);
                JSONParser parser = new JSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No such path", Toast.LENGTH_SHORT).show();
                return;
            } else {

                mMap.clear();
                mLocA = mMap.addMarker(new MarkerOptions().position(latLngA).title(strLocationA));
                mLocB = mMap.addMarker(new MarkerOptions().position(latLngB).title(strLocationB));
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList();
                    lineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = result.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        if (j == 0) {
                            distance = point.get("distance");
                            continue;
                        } else if (j == 1) {
                            duration = point.get("duration");
                            continue;
                        }

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.BLUE);
                }

                tvDisDur.setText(distance + " (" + duration + ")");

                mMap.addPolyline(lineOptions);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        this.finishAffinity();

    }

    

}
