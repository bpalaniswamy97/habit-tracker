package com.example.habittracker.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private final HabitDao habitDao;
    private final LiveData<List<Habit>> allHabits;
    private final ExecutorService executorService;

    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        habitDao = db.habitDao();
        allHabits = habitDao.getAllHabits();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public interface InsertCallback {
        void onInserted(long id);
    }

    public void insert(Habit habit, InsertCallback callback) {
        executorService.execute(() -> {
            long id = habitDao.insert(habit);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void update(Habit habit) {
        executorService.execute(() -> habitDao.update(habit));
    }

    public void delete(Habit habit) {
        executorService.execute(() -> habitDao.delete(habit));
    }

    public LiveData<List<Integer>> getCompletedHabitIdsForDate(long date) {
        return habitDao.getCompletedHabitIdsForDate(date);
    }

    public void insertCompletion(HabitCompletion completion) {
        executorService.execute(() -> habitDao.insertCompletion(completion));
    }

    public void deleteCompletion(int habitId, long date) {
        executorService.execute(() -> habitDao.deleteCompletion(habitId, date));
    }

    public LiveData<Integer> getCompletionCount(int habitId) {
        return habitDao.getCompletionCount(habitId);
    }
}
