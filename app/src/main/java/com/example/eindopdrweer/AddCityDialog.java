package com.example.eindopdrweer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AddCityDialog extends DialogFragment {

    private Context context;
    private EditText editTextCityName;
    private AddCityDialogListener listener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_city_dialog,null);

        builder.setView(view)
                .setTitle("add city")
                .setMessage("Add a city name")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String cityName = editTextCityName.getText().toString();
                        checkName(cityName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        editTextCityName = view.findViewById(R.id.dialog_cityname);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
        try {
            listener = (AddCityDialogListener) context;
        } catch (ClassCastException e) {
            throw  new ClassCastException(context.toString() + "must implement AddCityDialogListener");
        }
    }

    public interface AddCityDialogListener{
        void applyText(String cityName);
    }

    public void checkName(final String name){
        if (MainActivity.getConnectivityStatusString(context) != "Not connected to Internet" ){
            String key = "ce5111d059086aaecfceb3fc5984f32a";
            String url = "https://api.openweathermap.org/data/2.5/weather?q="+name+"&appid="+key+"&units=metric";
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                if ( response.getInt("cod") == 200){
                                    listener.applyText(name);
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse.statusCode == 404){
                                Toast.makeText(context, "invalid city name", Toast.LENGTH_LONG).show();
                                Log.i("Volley Error 404", error.toString());
                            }
                            Log.i("Volley Error", error.toString());
                        }
                    });
            queue.add(jsonObjectRequest);
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

}
