package com.example.occupines.adapters;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.occupines.LoadingDialog;
import com.example.occupines.R;
import com.example.occupines.Utility;
import com.example.occupines.fragments.FormFragment;
import com.example.occupines.fragments.PropertyFragment;
import com.example.occupines.models.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {
    private final ArrayList<Property> listData;
    private final PropertyFragment propertyFragment;

    // RecyclerView recyclerView;
    public PropertyAdapter(ArrayList<Property> listData, PropertyFragment propertyFragment) {
        this.listData = listData;
        this.propertyFragment = propertyFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.property_template, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Property myListData = listData.get(position);
        Picasso.get().load(myListData.getImageFile1())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(holder.photo);
        holder.type.setText(myListData.getType());
        holder.price.setText(new DecimalFormat("#,##0.00").format(myListData.getPrice()));
        holder.location.setText(myListData.getLocation());
        holder.owner.setText(myListData.getOwner());
        holder.info.setText(myListData.getInfo());

        Button delete = holder.itemView.findViewById(R.id.delete);
        delete.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        holder.loadingDialog.start();
                        holder.db.collection("properties").document(myListData.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    for (int i = 1; i <= 5; i++) {
                                        StorageReference propertyImageRef = holder.storageRef
                                                .child("images")
                                                .child(holder.userId)
                                                .child("property" + Integer.parseInt(myListData.getId().substring(myListData.getId().length() - 1)))
                                                .child("image" + i);
                                        propertyImageRef.delete();
                                    }
                                    //Complete
                                    Utility.showToast(holder.itemView.getContext(), "Property deleted.");
                                    holder.loadingDialog.dismiss();

                                    holder.context.getSupportFragmentManager()
                                            .beginTransaction()
                                            .detach(propertyFragment)
                                            .attach(propertyFragment)
                                            .commit();
                                });

                        holder.db.collection("users").document(holder.userId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        assert document != null;
                                        if (document.exists()) {
                                            int propertyCount = Objects.requireNonNull(document.getLong("propertyCount")).intValue();

                                            holder.db.collection("users").document(holder.userId)
                                                    .update("propertyCount", --propertyCount);

                                            if (propertyCount <= 0) {
                                                //Change fragment
                                                holder.context.getSupportFragmentManager().popBackStack();
                                            }
                                        }
                                    }
                                });
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(holder.context);
            builder.setMessage("Delete selected property?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });

        Button edit = holder.itemView.findViewById(R.id.edit);
        edit.setOnClickListener(v ->
                holder.context.getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("PropertyFragment")
                        .replace(R.id.flFragment, FormFragment.newInstance(listData.get(position)))
                        .commit());
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
        private final AppCompatActivity context;
        private final String userId;
        private final FirebaseFirestore db;
        private final StorageReference storageRef;
        private final LoadingDialog loadingDialog;

        public ViewHolder(View itemView) {
            super(itemView);

            this.context = ((AppCompatActivity) itemView.getContext());
            this.userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            this.db = FirebaseFirestore.getInstance();
            this.storageRef = FirebaseStorage.getInstance().getReference();
            this.loadingDialog = new LoadingDialog(context);

            this.photo = itemView.findViewById(R.id.photoResult1);
            this.type = itemView.findViewById(R.id.typeResult);
            this.price = itemView.findViewById(R.id.priceResult);
            this.location = itemView.findViewById(R.id.locationResult);
            this.owner = itemView.findViewById(R.id.ownerResult);
            this.info = itemView.findViewById(R.id.infoResult);
        }
    }
}
