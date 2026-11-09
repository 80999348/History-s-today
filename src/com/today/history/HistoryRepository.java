package com.today.history;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryRepository {

    public static class Hit {
        public final int month;
        public final int day;
        public final Entry entry;

        Hit(int month, int day, Entry entry) {
            this.month = month;
            this.day = day;
            this.entry = entry;
        }
    }

    private static HistoryRepository instance;

    private final Map<Integer, List<Entry>> data = new HashMap<>();
    private final Map<Integer, String> festivals = new HashMap<>();
    private final List<Entry> all = new ArrayList<>();
    private final List<int[]> allKeys = new ArrayList<>();
    public int total;

    public static synchronized HistoryRepository get(Context c) {
        if (instance == null) instance = new HistoryRepository(c.getApplicationContext());
        return instance;
    }

    private HistoryRepository(Context c) {
        load(c);
    }

    private static int key(int month, int day) {
        return month * 100 + day;
    }

    private void load(Context c) {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(c.getAssets().open("history.dat"), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.length() == 0) continue;
                String[] p = line.split("\\|", 5);
                if (p.length < 4) continue;
                int k;
                int y;
                try {
                    k = Integer.parseInt(p[0]);
                    y = Integer.parseInt(p[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                Entry e = new Entry(y, p[2], p[3], p.length >= 5 ? p[4] : "");
                List<Entry> list = data.get(k);
                if (list == null) {
                    list = new ArrayList<>();
                    data.put(k, list);
                }
                list.add(e);
                all.add(e);
                allKeys.add(new int[]{k / 100, k % 100});
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (r != null) r.close();
            } catch (Exception ignored) {
            }
        }
        total = all.size();

        try {
            r = new BufferedReader(new InputStreamReader(c.getAssets().open("festival.dat"), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                int i = line.indexOf('|');
                if (i > 0) {
                    try {
                        festivals.put(Integer.parseInt(line.substring(0, i)), line.substring(i + 1));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (r != null) r.close();
            } catch (Exception ignored) {
            }
        }
        for (List<Entry> l : data.values()) {
            Collections.sort(l, (a, b) -> Integer.compare(a.year, b.year));
        }
    }

    public List<Entry> of(int month, int day) {
        List<Entry> l = data.get(key(month, day));
        return l == null ? Collections.<Entry>emptyList() : l;
    }

    public String festival(int month, int day) {
        String s = festivals.get(key(month, day));
        return s == null ? "" : s;
    }

    public List<Hit> search(String q, int limit) {
        List<Hit> out = new ArrayList<>();
        if (q == null) return out;
        String kw = q.trim();
        if (kw.isEmpty()) return out;
        String lower = kw.toLowerCase();
        for (int i = 0; i < all.size() && out.size() < limit; i++) {
            Entry e = all.get(i);
            if (e.text.toLowerCase().contains(lower) || String.valueOf(e.year).equals(kw)) {
                int[] k = allKeys.get(i);
                out.add(new Hit(k[0], k[1], e));
            }
        }
        return out;
    }
}
