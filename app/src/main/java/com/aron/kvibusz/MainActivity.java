package com.aron.kvibusz;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public Dictionary<String, String> busStops = new Hashtable<>();
    public Dictionary<String, List<String>> routes = new Hashtable<>();
    TextView currentAddressTextView;
    ImageView infoImage;
    ImageView refreshImage;
    ImageView helpArrow;
    TextView helpText;
    TextView infoText;
    RecyclerView busRecyclerView;
    Geocoder geocoder;
    ArrayList<String> busStopNames = new ArrayList<>();
    ArrayList<String> busNumbers = new ArrayList<>();
    ArrayList<String> lineColors = new ArrayList<>();
    ArrayList<String> textColors = new ArrayList<>();
    ArrayList<String> directions = new ArrayList<>();
    ArrayList<Long> whenTimes = new ArrayList<>();
    Double lon, lat;
    Double currentTime;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

//// Static data used for testing
//        busStopNames = getResources().getStringArray(R.array.busStopNames);
//        busNumbers = getResources().getStringArray(R.array.busNumbers);
//        lineColors = getResources().getStringArray(R.array.lineColors);
//        directions = getResources().getStringArray(R.array.directions);
//        remainingTimes = getResources().getStringArray(R.array.remainingTimes);


        currentAddressTextView = findViewById(R.id.currentAddressTextView);
        busRecyclerView = findViewById(R.id.busRecyclerView);
        infoText = findViewById(R.id.infoText);
        infoImage = findViewById(R.id.infoImage);
        refreshImage = findViewById(R.id.refreshButton);
        helpArrow = findViewById(R.id.helpArrow);
        helpText = findViewById(R.id.helpText);
        updateLocation(this);

        ObjectAnimator pulseLoading = ObjectAnimator.ofPropertyValuesHolder(
                infoImage,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        pulseLoading.setDuration(310);
        pulseLoading.setRepeatCount(ObjectAnimator.INFINITE);
        pulseLoading.setRepeatMode(ObjectAnimator.REVERSE);

        pulseLoading.start();

    }

    public void loadStopsJson(final double lon, final double lat, final Context c) {
        AndroidNetworking.get("https://utas.vbusz.hu/api/query/v1/ws/otp/api/where/arrivals-and-departures-for-location.json?includeReferences=stops")
                .addHeaders("Content-Type", "application/json")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject stopsObject = response.getJSONObject("data").getJSONObject("references").getJSONObject("stops");
                            Iterator<String> stopsIterator = stopsObject.keys();
                            while (stopsIterator.hasNext()) {
                                String key = stopsIterator.next();
//                                Log.i("jsondolgok", String.valueOf(stopsObject.getJSONObject(key).get("id")) + " " + String.valueOf(stopsObject.getJSONObject(key).get("name")));
                                busStops.put(String.valueOf(stopsObject.getJSONObject(key).get("id")), String.valueOf(stopsObject.getJSONObject(key).get("name")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loadRoutesJson(lon, lat, c);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i("jsondolgok", "Json hiba van");
                    }
                });
    }

    public void loadRoutesJson(final double lon, final double lat, final Context c) {
        AndroidNetworking.get("https://utas.vbusz.hu/api/query/v1/ws/otp/api/where/arrivals-and-departures-for-location.json?includeReferences=routes")
                .addHeaders("Content-Type", "application/json")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject routesObject = response.getJSONObject("data").getJSONObject("references").getJSONObject("routes");
                            Iterator<String> routesIterator = routesObject.keys();
                            while (routesIterator.hasNext()) {
                                String key = routesIterator.next();
                                List<String> tmp = new LinkedList<>();
//                                Log.i("jsondolgok", String.valueOf(routesObject.getJSONObject(key).get("id")) + " "
//                                        + String.valueOf(routesObject.getJSONObject(key).get("shortName")) + " "
//                                        + String.valueOf(routesObject.getJSONObject(key).getJSONObject("style").get("color")) + " "
//                                        + String.valueOf(routesObject.getJSONObject(key).getJSONObject("style").getJSONObject("icon").get("textColor")));
                                tmp.add(String.valueOf(routesObject.getJSONObject(key).get("shortName")));
                                tmp.add(String.valueOf(routesObject.getJSONObject(key).getJSONObject("style").get("color")));
                                tmp.add(String.valueOf(routesObject.getJSONObject(key).getJSONObject("style").getJSONObject("icon").get("textColor")));
                                routes.put(String.valueOf(routesObject.getJSONObject(key).get("id")), tmp);
                            }
//                            Log.i("jsonBetoltve", routes.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loadBusesJson(lon, lat, c);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i("jsondolgok", "Json hiba van");
                    }
                });
    }

    public void loadBusesJson(double lon, double lat, final Context c) {
        Log.i("jsonURL", "https://utas.vbusz.hu/api/query/v1/ws/otp/api/where/arrivals-and-departures-for-location.json?includeReferences=false&lon=" + lon + "&lat=" + lat + "&radius=500&onlyDepartures=true&limit=40&minutesBefore=0&minutesAfter=60&clientLon=" + lon + "&clientLat=" + lat);
        AndroidNetworking.get("https://utas.vbusz.hu/api/query/v1/ws/otp/api/where/arrivals-and-departures-for-location.json?includeReferences=false&lon=" + lon + "&lat=" + lat + "&radius=500&onlyDepartures=true&limit=40&minutesBefore=0&minutesAfter=60&clientLon=" + lon + "&clientLat=" + lat)
                .addHeaders("Content-Type", "application/json")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        busStopNames.clear();
                        busNumbers.clear();
                        lineColors.clear();
                        textColors.clear();
                        directions.clear();
                        whenTimes.clear();
                        try {
                            currentTime = Math.floor(response.getLong("currentTime") / 1e3);
                            JSONArray busArray = response.getJSONObject("data").getJSONArray("list");

                            for (int i = 0; i < busArray.length(); i++) {
                                JSONObject currentObject = busArray.getJSONObject(i);
//                                Log.i("jsondolgok",currentObject.toString());
//                                Log.i("jsondolgok",currentObject.get("routeId").toString());
//                                Log.i("jsondolgok", routes.get(currentObject.get("routeId")).get(0)); // shortname
//                                Log.i("jsondolgok", routes.get(currentObject.get("routeId")).get(1)); // color
//                                Log.i("jsondolgok", routes.get(currentObject.get("routeId")).get(2)); // textcolor
//
//                                Log.i("jsondolgok",currentObject.get("headsign").toString()); // headsign
//
//                                Log.i("timeDolgok", "DepartureTime: " + String.valueOf(currentObject.getJSONArray("stopTimes").getJSONObject(0).get("departureTime"))); // departureTime
//                                Log.i("timeDolgok", String.valueOf(Math.floor(System.currentTimeMillis()/1e3)));
//                                Log.i("jsondolgok", busStops.get(currentObject.getJSONArray("stopTimes").getJSONObject(0).get("stopId"))); // busStop
                                try {
                                    busStopNames.add(busStops.get(currentObject.getJSONArray("stopTimes").getJSONObject(0).get("stopId")));
                                } catch (JSONException e) {
                                    busStopNames.add("?");
                                }
                                try {
                                    busNumbers.add(routes.get(currentObject.get("routeId")).get(0));
                                } catch (NullPointerException e) {
                                    busNumbers.add("?");
                                }
                                try {
                                    lineColors.add("#" + routes.get(currentObject.get("routeId")).get(1));
                                } catch (NullPointerException e) {
                                    lineColors.add("#000000");
                                }
                                try {
                                    textColors.add("#" + routes.get(currentObject.get("routeId")).get(2));
                                } catch (NullPointerException e) {
                                    textColors.add("#FFFFFF");
                                }
                                try {
                                    directions.add("► " + currentObject.get("headsign").toString());
                                } catch (NullPointerException e) {
                                    directions.add("?");
                                }
                                try {
                                    whenTimes.add(Long.parseLong(String.valueOf(currentObject.getJSONArray("stopTimes").getJSONObject(0).get("departureTime"))));
                                } catch (NullPointerException e) {
                                    whenTimes.add(System.currentTimeMillis() / 1000);
                                }
                            }
                            busAdapter busAdapter = new busAdapter(c, busStopNames, busNumbers, lineColors, textColors, directions, whenTimes, currentTime);
                            busRecyclerView.setAdapter(busAdapter);
                            busRecyclerView.setLayoutManager(new LinearLayoutManager(c));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (busNumbers.size() == 0) {
                            infoText.setText("Valami hiba történt, vagy ma már nem jön több busz :(");
                            infoImage.setImageDrawable(AppCompatResources.getDrawable(c, R.drawable.ic_error_outline_24px));
                            helpArrow.setVisibility(View.VISIBLE);
                            helpText.setVisibility(View.VISIBLE);
                        } else {
                            infoText.setVisibility(View.INVISIBLE);
                            infoImage.setVisibility(View.INVISIBLE);
                        }
                        refreshImage.clearAnimation();
                        refreshImage.animate().cancel();
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.i("jsondolgok", "Json hiba van");
                    }
                });
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onClick(View v) {
        v.clearAnimation();
        v.animate().cancel();
//        refreshImage.clearAnimation();
        v.animate().rotation(1800).setDuration(2000).start();
        updateLocation(v.getContext());
    }


    private void updateLocation(final Context c) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    Log.d("GPSdebug", "Latitude: " + lat);
                    Log.d("GPSdebug", "Longitude: " + lon);
                    loadStopsJson(lon, lat, c);
                    Log.d("GPSdebug", "accuracy: " + location.getAccuracy());
                    try {
                        List<Address> tmp = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                        Log.i("json", tmp.toString());
                        currentAddressTextView.setText(tmp.get(0).getThoroughfare() + " " + tmp.get(0).getFeatureName() + ".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}