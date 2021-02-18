package com.example.occupines.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.occupines.DayViewContainer;
import com.example.occupines.MonthViewContainer;
import com.example.occupines.R;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public class FourthFragment extends Fragment {

    public static LocalDate selectedDate = null;
    private static CalendarView calendarView;
    private final LocalDate today = LocalDate.now();

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
            if (currentSelection != null) {
                // We need to also reload the previously selected
                // date so we can REMOVE the selection background.
                calendarView.notifyDateChanged(currentSelection);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fourth, container, false);

        calendarView = view.findViewById(R.id.calendarView);

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
                View dotView = dayViewContainer.getDotView();

                textView.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));

                if (calendarDay.getOwner() == DayOwner.THIS_MONTH) {
                    // Show the month dates. Remember that views are recycled!
                    textView.setVisibility(View.VISIBLE);
                    if (calendarDay.getDate().equals(today)) {
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundResource(R.drawable.today_bg);
                        dotView.setVisibility(View.INVISIBLE);
                    } else if (calendarDay.getDate().equals(selectedDate)) {
                        // If this is the selected date, show a round background and change the text color.
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundResource(R.drawable.selected_bg);
                        dotView.setVisibility(View.INVISIBLE);
                    } else {
                        // If this is NOT the selected date, remove the background and reset the text color.
                        textView.setTextColor(Color.BLACK);
                        textView.setBackground(null);
                        dotView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    // Hide in and out dates
                    textView.setVisibility(View.INVISIBLE);
                    dotView.setVisibility(View.INVISIBLE);
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

        return view;
    }
}