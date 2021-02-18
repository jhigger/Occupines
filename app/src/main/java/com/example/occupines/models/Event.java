package com.example.occupines.models;

import java.time.LocalDate;

public class Event {

    private final LocalDate date;
    private String id;
    private String text;

    public Event(String text, LocalDate date) {
        this.text = text;
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }
}
