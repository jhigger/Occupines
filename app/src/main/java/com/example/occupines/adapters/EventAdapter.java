package com.example.occupines.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.occupines.R;
import com.example.occupines.models.Event;

import java.util.List;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item_view, parent, false);
        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        holder.bind(events.get(position));

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView itemEventText;

        public final AppCompatActivity context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemEventText = itemView.findViewById(R.id.itemEventText);
            this.context = (AppCompatActivity) itemView.getContext();
        }

        public void bind(Event event) {
            itemEventText.setText(event.getText());
        }
    }
}
