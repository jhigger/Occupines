package com.example.occupines.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.occupines.DayViewContainer;
import com.example.occupines.LoadingDialog;
import com.example.occupines.MonthViewContainer;
import com.example.occupines.R;
import com.example.occupines.Utility;
import com.example.occupines.adapters.EventAdapter;
import com.example.occupines.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FourthFragment extends Fragment {

    private static final String TAG = "FourthFragment";
    private static final DateTimeFormatter selectionFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
    public static LocalDate selectedDate = null;

    private final LocalDate today = LocalDate.now();
    private static CalendarView calendarView;
    @SuppressLint("StaticFieldLeak")
    private static TextView selectedDateText;

    private static FirebaseUser currentUser;
    @SuppressLint("StaticFieldLeak")
    private static FirebaseFirestore db;
    @SuppressLint("StaticFieldLeak")
    private static LoadingDialog loadingDialog;

    private RecyclerView recyclerView;
    private static EventAdapter eventAdapter;
    private static Set<Event> calendarEvents;
    private static List<Event> events;

    public FourthFragment() {
        // Required empty public constructor
    }

    public static void selectDate(LocalDate date) {
        // Keep a reference to any previous selection
        // in case we overwrite it and need to reload it.
        LocalDate currentSelection = selectedDate;
        if (currentSelection == date) {
            // If the user clicks the same date, clear selection.
            selectedDate = null;
            // Reload this date so the dayBinder is called
            // and we can REMOVE the selection background.
            calendarView.notifyDateChanged(currentSelection);
        } else {
            selectedDate = date;
            // Reload the newly selected date so the dayBinder is
            // called and we can ADD the selection background.
            calendarView.notifyDateChanged(date);
            updateAdapterForDate(date);
            if (currentSelection != null) {
                // We need to also reload the previously selected
                // date so we can REMOVE the selection background.
                calendarView.notifyDateChanged(currentSelection);
            }
        }
    }

    public static void updateAdapterForDate(LocalDate date) {
        getEvents(date);
        eventAdapter.notifyDataSetChanged();
        selectedDateText.setText(selectionFormatter.format(date));
    }

    private static void getEvents(LocalDate date) {
        events.clear();

        db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (document.exists()) {
                                //Get the event data of a document
                                Event event = new Event(
                                        document.getId(),
                                        document.getString("userId"),
                                        document.getString("text"),
                                        LocalDate.parse(document.getString("date")));

                                if (event.getUserId().equals(currentUser.getUid())) {
                                    if (event.getDate().equals(date)) {
                                        if (!events.contains(event)) {
                                            events.add(event);
                                            calendarEvents.add(event);
                                        }
                                    }
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                        calendarView.notifyDateChanged(date);
                        if (eventAdapter != null) eventAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private static void getEvents() {
        //Start loading animation
        loadingDialog.start();

        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        loadingDialog.dismiss();
                        return;
                    }
                    //Redraw on data change
                    calendarEvents.clear();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(value)) {
                        if (document.exists()) {
                            //Get the event data of a document
                            Event event = new Event(
                                    document.getId(),
                                    document.getString("userId"),
                                    document.getString("text"),
                                    LocalDate.parse(document.getString("date")));

                            if (event.getUserId().equals(currentUser.getUid())) {
                                calendarEvents.add(event);
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                    calendarView.notifyCalendarChanged();
                    loadingDialog.dismiss();
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fourth, container, false);

        setupRecyclerView(view);

        calendarView = view.findViewById(R.id.calendarView);
        selectedDateText = view.findViewById(R.id.selectedDateText);

        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(10);
        YearMonth lastMonth = currentMonth.plusMonths(10);
        DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        // Show today's events initially.
        selectDate(today);

        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer dayViewContainer, @NonNull CalendarDay calendarDay) {
                dayViewContainer.setDay(calendarDay);
                TextView textView = dayViewContainer.getTextView();

                textView.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));

                if (calendarDay.getOwner() == DayOwner.THIS_MONTH) {
                    // Show the month dates. Remember that views are recycled!
                    textView.setVisibility(View.VISIBLE);
                    if (calendarDay.getDate().equals(today)) {
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundResource(R.drawable.today_bg);
                    } else if (calendarDay.getDate().equals(selectedDate)) {
                        // If this is the selected date, show a round background and change the text color.
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundResource(R.drawable.selected_bg);
                    } else {
                        // If this is NOT the selected date, remove the background and reset the text color.
                        textView.setTextColor(Color.BLACK);
                        textView.setBackground(null);
                        // If this date has an event, show a red circle
                        if (containsDate(calendarDay.getDate())) {
                            textView.setBackgroundResource(R.drawable.event_bg);
                        }
                    }
                } else {
                    // Hide in and out dates
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        });

        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer monthViewContainer, @NonNull CalendarMonth calendarMonth) {
                String month = calendarMonth.getYearMonth().getMonth().name().toLowerCase();
                String caps = month.substring(0, 1).toUpperCase() + month.substring(1);
                String monthYear = caps + " " + calendarMonth.getYear();
                monthViewContainer.getTextView().setText(monthYear);
            }
        });

        FloatingActionButton calendarAddButton = view.findViewById(R.id.calendarAddButton);
        calendarAddButton.setOnClickListener(v -> inputDialog());

        return view;
    }

    private void setupRecyclerView(View view) {
        // 1. get a reference to recyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        // 2. set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // this is data for recycler view
        events = new ArrayList<>();
        calendarEvents = new LinkedHashSet<>();
        getEvents();
        // 3. create an adapter
        eventAdapter = new EventAdapter(events);
        // 4. set adapter
        recyclerView.setAdapter(eventAdapter);
        // 5. set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void inputDialog() {
        final EditText input = new EditText(getContext());

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    if (!input.getText().toString().trim().isEmpty()) {
                        addEvent(new Event(currentUser.getUid(), input.getText().toString(), selectedDate));
                    } else {
                        Utility.showToast(getContext(), "Field is empty");
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setView(input);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setMessage("Add event").setPositiveButton("Save", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();

    }

    private void addEvent(Event event) {
        loadingDialog.start();

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("userId", currentUser.getUid());
        eventMap.put("text", event.getText());
        eventMap.put("date", event.getDate().toString());
        eventMap.put("createdAt", FieldValue.serverTimestamp());

        db.collection("events").document()
                .set(eventMap)
                .addOnCompleteListener(task -> loadingDialog.dismiss())
                .addOnSuccessListener(aVoid -> {
                    events.add(event);
                    calendarView.notifyDateChanged(event.getDate());
                    eventAdapter.notifyDataSetChanged();
                    updateAdapterForDate(event.getDate());
                    Utility.showToast(getContext(), "Event Added");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getContext(), "Error: Submission failed");
                    Log.w(TAG, "Error writing document", e);
                });
    }

    private boolean containsDate(LocalDate date) {
        return calendarEvents.stream().anyMatch(o -> o.getDate().equals(date));
    }

    @Override
    public void onDestroy() {
        //Set values to null to prevent memory leak
        eventAdapter = null;
        recyclerView.setAdapter(null);
        super.onDestroy();
    }
}