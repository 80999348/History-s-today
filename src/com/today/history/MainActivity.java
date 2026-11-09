package com.today.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

    private static final String[] WEEKS = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
    private static final int C_EVENT = 0xFFD9A85C;
    private static final int C_BIRTH = 0xFF71A9C8;
    private static final int C_DEATH = 0xFF9B93B8;
    private static final int C_FESTIVAL = 0xFFC8604D;
    private static final int C_TEXT = 0xFFEDF1F7;
    private static final int C_BODY = 0xFFDCE3ED;
    private static final int C_DIM = 0xFF8A94A6;
    private static final int C_FAINT = 0xFF5A6474;

    private HistoryRepository repo;
    private Calendar current;

    private ScrollView scroll;
    private LinearLayout listContainer;
    private LinearLayout header;
    private LinearLayout bottomBar;
    private TextView tvDate, tvSub, tvMeta, footerText, btnToday;

    private GestureDetector detector;
    private int statusBarH, navBarH;
    private final Random random = new Random();

    // 详情层
    private FrameLayout detailOverlay;
    private LinearLayout detailPanel;
    private View detailScrim;
    private TextView detailYear, detailDate, detailBody, detailDesc, detailTag, detailAgo, detailCopy;
    private String detailText = "";
    private boolean detailOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = HistoryRepository.get(this);
        current = Calendar.getInstance();

        scroll = (ScrollView) findViewById(R.id.scroll);
        listContainer = (LinearLayout) findViewById(R.id.listContainer);
        header = (LinearLayout) findViewById(R.id.header);
        bottomBar = (LinearLayout) findViewById(R.id.bottomBar);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvSub = (TextView) findViewById(R.id.tvSub);
        tvMeta = (TextView) findViewById(R.id.tvMeta);
        footerText = (TextView) findViewById(R.id.footerText);
        btnToday = (TextView) findViewById(R.id.btnToday);

        setupWindow();

        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > dp(56) && Math.abs(dx) > Math.abs(dy) * 1.6f && Math.abs(vx) > 300) {
                    shift(dx < 0 ? 1 : -1);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.btnPrev).setOnClickListener(v -> shift(-1));
        findViewById(R.id.btnNext).setOnClickListener(v -> shift(1));
        btnToday.setOnClickListener(v -> {
            current = Calendar.getInstance();
            render(true);
        });
        findViewById(R.id.btnRandom).setOnClickListener(v -> randomDay());
        findViewById(R.id.btnSearch).setOnClickListener(v -> showSearch());
        tvDate.setOnClickListener(v -> pickDate());

        footerText.setText("内容源自百度百科 · 全库 " + NumberFormat.getIntegerInstance(Locale.CHINA).format(repo.total)
                + " 则 · 完全离线");

        buildDetailSheet();
        render(false);
    }

    private void setupWindow() {
        Window w = getWindow();
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(0xFF0A0D12);
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        statusBarH = dimen("status_bar_height", 24);
        navBarH = dimen("navigation_bar_height", 0);

        header.setPadding(dp(22), statusBarH + dp(10), dp(22), 0);
        bottomBar.setPadding(dp(18), dp(12), dp(18), dp(12) + navBarH);
    }

    private int dimen(String name, int fallbackDp) {
        int id = getResources().getIdentifier(name, "dimen", "android");
        if (id > 0) return getResources().getDimensionPixelSize(id);
        return dp(fallbackDp);
    }

    // ------------------------------------------------------------------ render

    private void shift(int days) {
        current.add(Calendar.DAY_OF_MONTH, days);
        render(true);
    }

    private void randomDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, random.nextInt(365));
        current.set(Calendar.MONTH, c.get(Calendar.MONTH));
        current.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        render(true);
    }

    private void pickDate() {
        DatePickerDialog d = new DatePickerDialog(this, android.R.style.Theme_Material_Dialog_Alert,
                (DatePicker view, int year, int month, int day) -> {
                    current.set(year, month, day);
                    render(true);
                },
                current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
        d.show();
    }

    private boolean isToday() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR);
    }

    private void render(boolean animate) {
        int y = current.get(Calendar.YEAR);
        int m = current.get(Calendar.MONTH) + 1;
        int d = current.get(Calendar.DAY_OF_MONTH);

        tvDate.setText(m + "月" + (d < 10 ? "0" + d : String.valueOf(d)) + "日");

        int[] lunar = LunarCalendar.solarToLunar(y, m, d);
        String week = WEEKS[current.get(Calendar.DAY_OF_WEEK) - 1];
        tvSub.setText(y + " 年 · " + week + " · 农历"
                + LunarCalendar.monthName(lunar[1], lunar[3] == 1) + LunarCalendar.dayName(lunar[2]));

        int doy = current.get(Calendar.DAY_OF_YEAR);
        int totalDays = current.getActualMaximum(Calendar.DAY_OF_YEAR);
        int left = totalDays - doy;

        List<Entry> list = repo.of(m, d);
        String festival = repo.festival(m, d);

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(festival)) sb.append(festival).append("\n");
        sb.append(LunarCalendar.ganzhi(lunar[0])).append(LunarCalendar.zodiac(lunar[0])).append("年")
                .append(" · 距年末 ").append(left).append(" 天")
                .append(" · ").append(list.size()).append(" 则记载");
        tvMeta.setText(sb.toString());

        listContainer.removeAllViews();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            listContainer.addView(buildItem(list.get(i), i == 0, i == n - 1, y, m, d));
        }
        if (n == 0) {
            TextView empty = new TextView(this);
            empty.setText("这一天暂无记载");
            empty.setTextColor(C_FAINT);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(48), 0, dp(48));
            listContainer.addView(empty);
        }

        boolean today = isToday();
        btnToday.setText(today ? "今天" : "回到今天");
        btnToday.setTextColor(today ? C_DIM : C_EVENT);

        scroll.scrollTo(0, 0);

        if (animate) {
            listContainer.setAlpha(0f);
            listContainer.setTranslationY(dp(18));
            listContainer.animate().alpha(1f).translationY(0f)
                    .setDuration(260).setInterpolator(new DecelerateInterpolator()).start();
        } else {
            listContainer.setAlpha(1f);
            listContainer.setTranslationY(0f);
        }
    }

    private View buildItem(Entry e, boolean first, boolean last, int yearNow, int month, int day) {
        int color = colorOf(e.type);

        FrameLayout row = new FrameLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Timeline timeline = new Timeline(this, color, first, last);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(dp(24), ViewGroup.LayoutParams.MATCH_PARENT);
        row.addView(timeline, tlp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(13), dp(16), dp(15));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF141A23);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), 0xFF1F2733);
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x1AFFFFFF), bg, null));

        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.leftMargin = dp(24);
        clp.bottomMargin = last ? 0 : dp(10);
        row.addView(card, clp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView year = new TextView(this);
        year.setText(String.valueOf(e.year));
        year.setTextColor(color);
        year.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        year.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        year.setLetterSpacing(0.02f);
        top.addView(year);

        TextView unit = new TextView(this);
        unit.setText(" 年");
        unit.setTextColor(C_FAINT);
        unit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        top.addView(unit);

        int ago = yearNow - e.year;
        if (ago > 0) {
            TextView tvAgo = new TextView(this);
            tvAgo.setText("距今 " + ago + " 年");
            tvAgo.setTextColor(0xFF4E5868);
            tvAgo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            tvAgo.setPadding(dp(7), 0, 0, 0);
            top.addView(tvAgo);
        }

        View spacer = new View(this);
        top.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView tag = new TextView(this);
        tag.setText(labelOf(e.type) + "  ›");
        tag.setTextColor(color);
        tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(withAlpha(color, 0x24));
        pill.setCornerRadius(dp(5));
        tag.setBackground(new RippleDrawable(ColorStateList.valueOf(0x30FFFFFF), pill, null));
        tag.setPadding(dp(7), dp(3), dp(7), dp(3));
        tag.setClickable(true);
        tag.setFocusable(true);
        tag.setOnClickListener(v -> openDetail(e, month, day));
        top.addView(tag);

        card.addView(top);

        TextView body = new TextView(this);
        body.setText(e.text);
        body.setTextColor(C_BODY);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        body.setLineSpacing(dp(5), 1f);
        body.setPadding(0, dp(9), 0, 0);
        card.addView(body);

        card.setOnClickListener(v -> openDetail(e, month, day));
        return row;
    }

    // ------------------------------------------------------------- detail sheet

    private void buildDetailSheet() {
        FrameLayout root = (FrameLayout) findViewById(R.id.root);

        detailOverlay = new FrameLayout(this);
        detailOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        detailOverlay.setVisibility(View.GONE);
        detailOverlay.setClickable(true);

        detailScrim = new View(this);
        detailScrim.setBackgroundColor(0xC4000000);
        detailScrim.setOnClickListener(v -> closeDetail());
        detailOverlay.addView(detailScrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        detailPanel = new LinearLayout(this);
        detailPanel.setOrientation(LinearLayout.VERTICAL);
        detailPanel.setClickable(true);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(0xFF0C1119);
        panelBg.setCornerRadii(new float[]{dp(26), dp(26), dp(26), dp(26), 0, 0, 0, 0});
        detailPanel.setBackground(panelBg);

        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.gravity = Gravity.BOTTOM;
        detailOverlay.addView(detailPanel, plp);

        // 顶部操作条：关闭 + 复制
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(18), dp(14), dp(18), 0);

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        close.setTextColor(C_TEXT);
        close.setGravity(Gravity.CENTER);
        GradientDrawable cb = new GradientDrawable();
        cb.setColor(0xFF161D27);
        cb.setCornerRadius(dp(100));
        close.setBackground(cb);
        close.setOnClickListener(v -> closeDetail());
        bar.addView(close, new LinearLayout.LayoutParams(dp(38), dp(38)));

        TextView barTitle = new TextView(this);
        barTitle.setText("事件详情");
        barTitle.setTextColor(C_FAINT);
        barTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        barTitle.setLetterSpacing(0.18f);
        barTitle.setGravity(Gravity.CENTER);
        bar.addView(barTitle, new LinearLayout.LayoutParams(0, dp(38), 1f));

        detailCopy = new TextView(this);
        detailCopy.setText("复制");
        detailCopy.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        detailCopy.setTextColor(C_DIM);
        detailCopy.setGravity(Gravity.CENTER);
        detailCopy.setBackground(getResources().getDrawable(R.drawable.chip));
        detailCopy.setPadding(dp(14), dp(6), dp(14), dp(6));
        detailCopy.setOnClickListener(v -> copyText(detailText));
        bar.addView(detailCopy);

        detailPanel.addView(bar);

        MaxHeightScrollView sv = new MaxHeightScrollView(this);
        sv.setMaxHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.70f));
        sv.setVerticalScrollBarEnabled(false);
        sv.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(22), dp(24), dp(26) + navBarH);

        LinearLayout yearRow = new LinearLayout(this);
        yearRow.setOrientation(LinearLayout.HORIZONTAL);
        yearRow.setGravity(Gravity.CENTER_VERTICAL);

        detailYear = new TextView(this);
        detailYear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        detailYear.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        detailYear.setLetterSpacing(0.01f);
        yearRow.addView(detailYear);

        TextView unit = new TextView(this);
        unit.setText(" 年");
        unit.setTextColor(C_FAINT);
        unit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        yearRow.addView(unit);

        View sp = new View(this);
        yearRow.addView(sp, new LinearLayout.LayoutParams(0, 1, 1f));

        detailTag = new TextView(this);
        yearRow.addView(detailTag);

        content.addView(yearRow);

        detailDate = new TextView(this);
        detailDate.setTextColor(C_DIM);
        detailDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        detailDate.setPadding(0, dp(10), 0, 0);
        content.addView(detailDate);

        View line1 = new View(this);
        line1.setBackgroundColor(0xFF1A2130);
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        l1.topMargin = dp(22);
        content.addView(line1, l1);

        detailBody = new TextView(this);
        detailBody.setTextColor(0xFFE9EEF6);
        detailBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        detailBody.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        detailBody.setLineSpacing(dp(8), 1f);
        detailBody.setPadding(0, dp(22), 0, 0);
        content.addView(detailBody);

        detailDesc = new TextView(this);
        detailDesc.setTextColor(0xFFB7C1D1);
        detailDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        detailDesc.setLineSpacing(dp(8), 1f);
        detailDesc.setPadding(0, dp(16), 0, 0);
        content.addView(detailDesc);

        View line2 = new View(this);
        line2.setBackgroundColor(0xFF1A2130);
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        l2.topMargin = dp(24);
        content.addView(line2, l2);

        detailAgo = new TextView(this);
        detailAgo.setTextColor(C_FAINT);
        detailAgo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        detailAgo.setLineSpacing(dp(4), 1f);
        detailAgo.setPadding(0, dp(14), 0, 0);
        content.addView(detailAgo);

        sv.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        detailPanel.addView(sv);

        root.addView(detailOverlay);
    }

    private void openDetail(Entry e, int month, int day) {
        detailText = e.text;
        int color = colorOf(e.type);

        detailYear.setText(String.valueOf(e.year));
        detailYear.setTextColor(color);
        styleTag(detailTag, color, labelOf(e.type));

        int[] lunar = LunarCalendar.solarToLunar(current.get(Calendar.YEAR), month, day);
        detailDate.setText(month + " 月 " + day + " 日 · "
                + WEEKS[current.get(Calendar.DAY_OF_WEEK) - 1] + " · 农历"
                + LunarCalendar.monthName(lunar[1], lunar[3] == 1) + LunarCalendar.dayName(lunar[2]));

        detailBody.setText(e.text);

        if (e.desc.isEmpty()) {
            detailDesc.setText("暂无更详细的介绍");
            detailDesc.setTextColor(C_FAINT);
            detailDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        } else {
            detailDesc.setText(e.desc);
            detailDesc.setTextColor(0xFFB7C1D1);
            detailDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }

        int ago = current.get(Calendar.YEAR) - e.year;
        detailAgo.setText((ago > 0 ? "距今 " + ago + " 年 · " : "")
                + LunarCalendar.ganzhi(lunar[0]) + LunarCalendar.zodiac(lunar[0]) + "年\n内容源自百度百科");

        detailOpen = true;
        detailOverlay.setVisibility(View.VISIBLE);
        detailScrim.setAlpha(0f);
        detailScrim.animate().alpha(1f).setDuration(200).start();

        detailPanel.setTranslationY(getResources().getDisplayMetrics().heightPixels);
        detailPanel.post(() -> detailPanel.animate().translationY(0f)
                .setDuration(320).setInterpolator(new DecelerateInterpolator(1.4f)).start());
    }

    private void closeDetail() {
        if (!detailOpen) return;
        detailOpen = false;
        detailPanel.animate().translationY(detailPanel.getHeight())
                .setDuration(240).setInterpolator(new AccelerateInterpolator()).start();
        detailScrim.animate().alpha(0f).setDuration(240).withEndAction(() -> {
            if (!detailOpen) detailOverlay.setVisibility(View.GONE);
        }).start();
    }

    private void styleTag(TextView tag, int color, String label) {
        tag.setText(label);
        tag.setTextColor(color);
        tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(withAlpha(color, 0x24));
        pill.setCornerRadius(dp(6));
        tag.setBackground(pill);
        tag.setPadding(dp(8), dp(3), dp(8), dp(3));
    }

    private void copyText(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("history", text));
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
    }

    private static class MaxHeightScrollView extends ScrollView {
        private int maxHeight = Integer.MAX_VALUE;

        MaxHeightScrollView(Context c) {
            super(c);
        }

        void setMaxHeight(int h) {
            maxHeight = h;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (getMeasuredHeight() > maxHeight) {
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
            }
        }
    }

    // ------------------------------------------------------------------ search

    private void showSearch() {
        final Dialog dlg = new Dialog(this);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(10), dp(16), dp(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF10161F);
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), 0xFF273040);
        root.setBackground(bg);

        EditText et = new EditText(this);
        et.setHint("搜索人物、事件或年份");
        et.setHintTextColor(C_FAINT);
        et.setTextColor(C_TEXT);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        et.setSingleLine(true);
        et.setBackground(null);
        et.setPadding(0, dp(10), 0, dp(10));
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        root.addView(et);

        View line = new View(this);
        line.setBackgroundColor(0xFF1E2634);
        root.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        FrameLayout center = new FrameLayout(this);
        root.addView(center, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        ListView lv = new ListView(this);
        lv.setDivider(null);
        lv.setDividerHeight(0);
        lv.setBackgroundColor(Color.TRANSPARENT);
        lv.setVerticalScrollBarEnabled(false);
        lv.setPadding(0, dp(4), 0, dp(4));
        center.addView(lv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView hint = new TextView(this);
        hint.setText("输入关键词，检索 " + repo.total + " 则记载");
        hint.setTextColor(C_FAINT);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hint.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = dp(40);
        center.addView(hint, hlp);

        final SearchAdapter adapter = new SearchAdapter();
        lv.setAdapter(adapter);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                String q = s.toString();
                adapter.setData(repo.search(q, 80));
                boolean empty = q.trim().isEmpty() || adapter.getCount() == 0;
                hint.setVisibility(empty ? View.VISIBLE : View.GONE);
                hint.setText(q.trim().isEmpty()
                        ? "输入关键词，检索 " + repo.total + " 则记载"
                        : "没有找到相关记载");
                lv.setVisibility(adapter.getCount() == 0 ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        lv.setOnItemClickListener((parent, view, position, id) -> {
            HistoryRepository.Hit h = adapter.getItem(position);
            current.set(Calendar.MONTH, h.month - 1);
            current.set(Calendar.DAY_OF_MONTH, h.day);
            render(true);
            dlg.dismiss();
        });

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dlg.show();

        if (w != null) {
            int width = getResources().getDisplayMetrics().widthPixels - dp(32);
            int height = Math.min(dp(520), getResources().getDisplayMetrics().heightPixels - statusBarH - dp(120));
            w.setLayout(width, height);
            w.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.y = statusBarH + dp(40);
            w.setAttributes(lp);
        }
        et.requestFocus();
    }

    private class SearchAdapter extends BaseAdapter {
        private List<HistoryRepository.Hit> items = new java.util.ArrayList<>();

        void setData(List<HistoryRepository.Hit> list) {
            items = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public HistoryRepository.Hit getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout box = new LinearLayout(MainActivity.this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(0, dp(11), 0, dp(11));

            HistoryRepository.Hit h = items.get(position);

            TextView meta = new TextView(MainActivity.this);
            meta.setText(h.month + " 月 " + h.day + " 日 · " + h.entry.year + " 年");
            meta.setTextColor(C_EVENT);
            meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            box.addView(meta);

            TextView text = new TextView(MainActivity.this);
            text.setText(h.entry.text);
            text.setTextColor(C_BODY);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            text.setMaxLines(2);
            text.setEllipsize(TextUtils.TruncateAt.END);
            text.setLineSpacing(dp(3), 1f);
            text.setPadding(0, dp(4), 0, 0);
            box.addView(text);

            View divider = new View(MainActivity.this);
            divider.setBackgroundColor(0xFF1A2130);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
            dlp.topMargin = dp(11);
            box.addView(divider, dlp);

            return box;
        }
    }

    // ------------------------------------------------------------------ helpers

    private static int colorOf(String type) {
        if ("birth".equals(type)) return C_BIRTH;
        if ("death".equals(type)) return C_DEATH;
        if ("festival".equals(type)) return C_FESTIVAL;
        return C_EVENT;
    }

    private static String labelOf(String type) {
        if ("birth".equals(type)) return "诞生";
        if ("death".equals(type)) return "逝世";
        if ("festival".equals(type)) return "节日";
        return "事件";
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (detector != null && !detailOpen) detector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (detailOpen) {
            closeDetail();
            return;
        }
        super.onBackPressed();
    }

    private class Timeline extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int color;
        private final boolean first, last;

        Timeline(Context c, int color, boolean first, boolean last) {
            super(c);
            this.color = color;
            this.first = first;
            this.last = last;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() / 2f;
            float cy = dp(20);
            float w = dp(0.7f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF1E2634);
            if (!first) canvas.drawRect(cx - w, 0, cx + w, cy - dp(7), paint);
            if (!last) canvas.drawRect(cx - w, cy + dp(7), cx + w, getHeight(), paint);

            paint.setColor(withAlpha(color, 0x28));
            canvas.drawCircle(cx, cy, dp(6.5f), paint);
            paint.setColor(color);
            canvas.drawCircle(cx, cy, dp(3.6f), paint);
        }
    }
}
