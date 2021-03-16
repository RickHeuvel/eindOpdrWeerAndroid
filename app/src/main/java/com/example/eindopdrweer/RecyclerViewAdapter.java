package com.example.eindopdrweer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    private ArrayList<CityModel> cities;
    private Context context;
    private OnItemClickListener mOnItemCLickListener;


    public RecyclerViewAdapter(ArrayList<CityModel> cityNames, Context context) {
        this.cities = cityNames;
        this.context = context;
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.mOnItemCLickListener = onItemCLickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.cityName.setText(cities.get(position).getName());
        if (cities.get(position).getTemp() != null){
            holder.temp.setText(cities.get(position).getTemp());
            String url = cities.get(position).getImgUrl();
            Picasso.with(context).load(url).into(holder.img);
        }

        if(mOnItemCLickListener!=null){
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemCLickListener.OnItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView cityName;
        TextView temp;
        ImageView img;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.cityName);
            temp = itemView.findViewById(R.id.temp);
            img = itemView.findViewById(R.id.icon);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }


}
