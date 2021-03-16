package com.example.eindopdrweer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OverviewFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private ArrayList<CityModel> cities = new ArrayList<CityModel>();
    private View rootView;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key == "cities" || key.equals("unit_preference")){
                    setCities();
                }
            }
        };
       sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
       if (sharedPreferences.getString("cities", null) != null){
           setCities();
       }

        rootView = inflater.inflate(R.layout.overview_fragment, container, false);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    private void setCities(){
        cities.clear();
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cities", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> names = gson.fromJson(json, type);

        if (names != null){
            for (String name:names) {
                cities.add(new CityModel(name, sharedPreferences.getString("unit_preference", null)));
            }
        }
        initList();
    }

    private void  initList(){
        for (CityModel citie: cities) {
            getWeather(citie);
        }
    }

    private void getWeather(final CityModel citie){
        if (MainActivity.getConnectivityStatusString(mContext) != "Not connected to Internet" ) {
            String key = "ce5111d059086aaecfceb3fc5984f32a";
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + citie.getName() + "&appid=" + key + "&units=" + sharedPreferences.getString("unit_preference", null);
            //RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                citie.setTemp(response.getJSONObject("main").getDouble("temp"));
                                citie.setImgUrl(response.getJSONArray("weather").getJSONObject(0).getString("icon"));
                                citie.setLat(response.getJSONObject("coord").getDouble("lat"));
                                citie.setLon(response.getJSONObject("coord").getDouble("lon"));

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
                    populateList();
                }
            };

            queue.addRequestFinishedListener(requestFinishedListener);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void populateList(){
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(cities ,getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setOnItemCLickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                ((MainActivity) getActivity()).openCityFragment(cities.get(position));
            }
        });
    }




}
