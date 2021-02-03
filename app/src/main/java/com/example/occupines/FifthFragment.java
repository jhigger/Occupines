package com.example.occupines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

public class FifthFragment extends Fragment {

    String[] notifications;
    ArrayList<String> list;
    int added;

    public FifthFragment() {
        notifications = new String[]{"1", "2", "3", "4", "5", "6", "7"};
        list = new ArrayList<>(Arrays.asList(notifications));
        added = getNotificationCount();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fifth, container, false);
        ListView listView = view.findViewById(R.id.notificationList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.fragment_fifth, R.id.textNotifications, list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {

            list.remove(position); // remove item at index in list data source
            arrayAdapter.notifyDataSetChanged(); // call it for refresh ListView
        });

        added = 0;

        return view;
    }

    public int getNotificationCount() {
        return list.size();
    }

    public int getAdded() {
        return added;
    }

    public void addNotification(String message) {
        list.add(message);
        added += 1;
    }
}