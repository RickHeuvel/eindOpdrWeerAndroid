package com.example.eindopdrweer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DetailFragment extends Fragment {

    private Context mContext;
    private View rootView;
    private CityModel city;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        rootView = inflater.inflate(R.layout.detail_fragment, container, false);
        TextView name = rootView.findViewById(R.id.detailCityName);
        TextView temp = rootView.findViewById(R.id.detailTemp);
        ImageView icon = rootView.findViewById(R.id.detailIcon);
        Button btDelete = rootView.findViewById(R.id.btDelete);
        Button btMap = rootView.findViewById(R.id.btMap);
        Button btClose = rootView.findViewById(R.id.btClose);

        name.setText(city.getName());
        temp.setText(city.getTemp());
        Picasso.with(mContext).load(city.getImgUrl()).into(icon);

        btMap.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapClick();
            }
        });
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClick();
            }
        });
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public void setCity(CityModel city){
        this.city = city;
    }

    private void closeFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
    private void deleteClick() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cities", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        //delete city from list
        ArrayList<String> citys = gson.fromJson(json, type);
        citys.remove(city.getName());

        //convert back to string and remove from preferences
        String result = gson.toJson(citys);
        sharedPreferences.edit().putString("cities", result).apply();

        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private void mapClick() {
        Uri gmmIntentUri = Uri.parse("geo:"+city.getLat()+","+city.getLon());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }



}
