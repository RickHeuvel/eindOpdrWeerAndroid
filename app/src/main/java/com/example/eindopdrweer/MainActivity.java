package com.example.eindopdrweer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements AddCityDialog.AddCityDialogListener {

    public SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    public static final int RequestPermissionCode = 1;
    private ArrayList<String> cityNames = new ArrayList<String>();
    private FragmentManager fm;

    private TextView cName;
    private TextView cTemp;
    private ImageView cIcon;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location curLocation;

    private static int TYPE_WIFI = 1;
    private static int TYPE_MOBILE = 2;
    private static int TYPE_NOT_CONNECTED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleIntent();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Latitude", "onLocchange Lat:"+location.getLatitude()+"Lon:"+location.getLongitude());
                curLocation = location;
                setCurrentLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Latitude", "statuschange");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Latitude", "prov enabled");
                setCurrentLocation();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Latitude", "prov disabled");
                cName.setText("No GPS");
                cTemp.setText("");
                cIcon.setVisibility(View.INVISIBLE);
            }
        };
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RequestPermissionCode);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, locationListener);
        }

        //set shared preferences if not existing
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString("unit_preference", null) == null) {
            sharedPreferences.edit().putString("unit_preference", "metric").commit();
        }
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                getCityNames();
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setCurrentLocation();
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        cityNames = getCityNames();
        cName = findViewById(R.id.cName);
        cTemp = findViewById(R.id.cTemp);
        cIcon = findViewById(R.id.cIcon);

        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        OverviewFragment overviewFragment = new OverviewFragment();
        overviewFragment.setRetainInstance(true);
        ft.replace(R.id.overviewFrame, overviewFragment, "overview");
        ft.commit();

    }
    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {
            case RequestPermissionCode:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"Not all necessary permissions are granted", Toast.LENGTH_LONG);
                        return;
                    }else{
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, locationListener);
                    }
                }
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        MenuInflater mymenu = getMenuInflater();
        mymenu.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void applyText(String cityName) {
        //add cityname to preferences
        if (getConnectivityStatusString(this) != "Not connected to Internet" ){
            cityNames.add(cityName);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(cityNames);
            editor.putString("cities", json);
            editor.apply();
        }else{
            Toast.makeText(this,"No internet connection available", Toast.LENGTH_LONG);
        }

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.toolbar_add:
                openAddDialog();
                return true;
            case R.id.toolbar_settings:
                openSettingsFragment();
                return true;
            case R.id.toolbar_contacts:
                openContactActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleIntent () {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type.equals("text/plain")) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                checkName(sharedText);
            }
        }
    }

    private static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = MainActivity.getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    private void setCurrentLocation() {
        cName.setText("Current Location");
        cIcon.setVisibility(View.VISIBLE);
        getWeather();
    }

    private void getWeather() {
        if (MainActivity.getConnectivityStatusString(this) != "Not connected to Internet" ){
            if (curLocation != null) {
                String key = "ce5111d059086aaecfceb3fc5984f32a";
                String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + curLocation.getLatitude() + "&lon=" + curLocation.getLongitude() + "&appid=" + key + "&units=" + sharedPreferences.getString("unit_preference", null);
                RequestQueue queue = Volley.newRequestQueue(this);

                final CityModel city = new CityModel(sharedPreferences.getString("unit_preference", null));
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    city.setTemp(response.getJSONObject("main").getDouble("temp"));
                                    city.setImgUrl(response.getJSONArray("weather").getJSONObject(0).getString("icon"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Volley Error", error.toString());
                            }
                        });
                queue.add(jsonObjectRequest);

                RequestQueue.RequestFinishedListener requestFinishedListener = new RequestQueue.RequestFinishedListener() {
                    @Override
                    public void onRequestFinished(final Request request) {
                        cTemp.setText(city.getTemp());
                        String url = city.getImgUrl();
                        Picasso.with(getApplicationContext()).load(url).into(cIcon);
                    }
                };

                queue.addRequestFinishedListener(requestFinishedListener);
            }
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No internet connection!")
                    .setTitle("Network")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkName(final String name){
        if (MainActivity.getConnectivityStatusString(this) != "Not connected to Internet" ){
        String key = "ce5111d059086aaecfceb3fc5984f32a";
        String url = "https://api.openweathermap.org/data/2.5/weather?q="+name+"&appid="+key+"&units=metric";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if ( response.getInt("cod") == 200){
                                applyText(name);
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 404){
                            Toast.makeText(getApplicationContext(), "invalid city name", Toast.LENGTH_LONG).show();
                            Log.i("Volley Error 404", error.toString());
                        }
                        Log.i("Volley Error", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);}
        else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("No internet connection!")
                        .setTitle("Network")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
        }
    }

    private void openContactActivity() {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    private void openSettingsFragment(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openAddDialog(){
        AddCityDialog dialog = new AddCityDialog();
        dialog.show(getSupportFragmentManager(),"add city dialog");
    }

    public void openCityFragment(CityModel city){
        FragmentTransaction ft = fm.beginTransaction();
        DetailFragment fragment = new DetailFragment();
        fragment.setCity(city);
        fragment.setRetainInstance(true);
        ft.replace(R.id.detailFrame, fragment, "detail");
        ft.commit();
    }

    private ArrayList<String> getCityNames(){
        if (sharedPreferences.getString("cities", null) != null){
            Gson gson = new Gson();
            String json = sharedPreferences.getString("cities", null);
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            cityNames = gson.fromJson(json, type);
            return cityNames;
        }else {
            return new ArrayList<String>();
        }

    }

}
