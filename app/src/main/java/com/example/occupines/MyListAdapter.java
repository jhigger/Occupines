package com.example.occupines;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private final ArrayList<PropertyPost> listData;

    // RecyclerView recyclerView;
    public MyListAdapter(ArrayList<PropertyPost> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.post_template, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PropertyPost myListData = listData.get(position);
        Picasso.get().load(myListData.getLocalFile())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(holder.photo);
        holder.type.setText(myListData.getType());
        holder.price.setText(String.valueOf(myListData.getPrice()));
        holder.location.setText(myListData.getLocation());
        holder.owner.setText(myListData.getOwner());
        holder.info.setText(myListData.getInfo());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView photo;
        public final TextView type;
        public final TextView price;
        public final TextView location;
        public final TextView owner;
        public final TextView info;

        public ViewHolder(View itemView) {
            super(itemView);
            this.photo = itemView.findViewById(R.id.photoResult);
            this.type = itemView.findViewById(R.id.typeResult);
            this.price = itemView.findViewById(R.id.priceResult);
            this.location = itemView.findViewById(R.id.locationResult);
            this.owner = itemView.findViewById(R.id.ownerResult);
            this.info = itemView.findViewById(R.id.infoResult);
        }
    }
}
