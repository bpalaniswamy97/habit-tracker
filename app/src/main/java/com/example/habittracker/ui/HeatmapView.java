package com.example.habittracker.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class HeatmapView extends View {
    private static final int COLUMNS = 7;
    private static final int ROWS = 8; // Showing last 8 weeks
    private final Paint paint = new Paint();
    private final Set<Long> completionDates = new HashSet<>();
    private final Calendar calendar = Calendar.getInstance();

    public HeatmapView(Context context) {
        super(context);
        init();
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setCompletions(Set<Long> completions) {
        this.completionDates.clear();
        for (Long date : completions) {
            this.completionDates.add(getStartOfDay(date));
        }
        invalidate();
    }

    private long getStartOfDay(long millis) {
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = 4f;
        float width = getWidth();
        float height = getHeight();
        float cellSize = Math.min((width - (COLUMNS + 1) * padding) / COLUMNS, 
                                 (height - (ROWS + 1) * padding) / ROWS);

        long today = getStartOfDay(System.currentTimeMillis());
        calendar.setTimeInMillis(today);
        
        // Find the most recent Sunday
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // Start from the end of the 8th week back
        calendar.add(Calendar.DAY_OF_YEAR, -(COLUMNS * ROWS - 1));

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                long currentMillis = getStartOfDay(calendar.getTimeInMillis());
                
                if (completionDates.contains(currentMillis)) {
                    paint.setColor(Color.parseColor("#4CAF50")); // Green for completed
                } else if (currentMillis > today) {
                    paint.setColor(Color.parseColor("#E0E0E0")); // Light grey for future
                } else {
                    paint.setColor(Color.parseColor("#FFCDD2")); // Reddish for missed past days
                    // Actually, "missed days without highlight" might mean light grey.
                    // Let's use light grey for missed/future and Green for completed.
                    paint.setColor(Color.parseColor("#F5F5F5"));
                }

                float left = c * (cellSize + padding) + padding;
                float top = r * (cellSize + padding) + padding;
                canvas.drawRoundRect(left, top, left + cellSize, top + cellSize, 4f, 4f, paint);

                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }
}
