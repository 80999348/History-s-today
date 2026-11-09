package com.today.history;

public class Entry {
    public final int year;
    public final String type;
    public final String text;
    public final String desc;

    public Entry(int year, String type, String text, String desc) {
        this.year = year;
        this.type = type;
        this.text = text;
        this.desc = desc == null ? "" : desc;
    }
}
