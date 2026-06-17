package com.example.habittracker.reminder;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.habittracker.data.Habit;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Habit habit) {
        if (habit.getReminderTime() == null || habit.getReminderTime().isEmpty()) {
            return;
        }

        String[] parts = habit.getReminderTime().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.set(Calendar.HOUR_OF_DAY, hour);
        scheduledTime.set(Calendar.MINUTE, minute);
        scheduledTime.set(Calendar.SECOND, 0);

        long delay = scheduledTime.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            // If time has passed today, schedule for tomorrow or based on frequency
            scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
            delay = scheduledTime.getTimeInMillis() - System.currentTimeMillis();
        }

        Data inputData = new Data.Builder()
                .putString("habit_name", habit.getName())
                .putInt("habit_id", habit.getId())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("habit_" + habit.getId())
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "habit_reminder_" + habit.getId(),
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }
}
