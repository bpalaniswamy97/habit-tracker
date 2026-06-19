package com.example.habittracker.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitCompletion;
import com.example.habittracker.data.HabitRepository;

import java.util.List;

public class HabitViewModel extends AndroidViewModel {
    private final HabitRepository repository;
    private final LiveData<List<Habit>> allHabits;
    private final LiveData<List<Habit>> activeHabits;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRepository(application);
        allHabits = repository.getAllHabits();
        activeHabits = repository.getActiveHabits();
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public LiveData<List<Habit>> getActiveHabits() {
        return activeHabits;
    }

    public void insert(Habit habit, HabitRepository.InsertCallback callback) {
        repository.insert(habit, callback);
    }

    public void update(Habit habit) {
        repository.update(habit);
    }

    public void delete(Habit habit) {
        repository.delete(habit);
    }

    public void deleteCompletionsByHabitId(int habitId) {
        repository.deleteCompletionsByHabitId(habitId);
    }

    public LiveData<List<Integer>> getCompletedHabitIdsForDate(long date) {
        return repository.getCompletedHabitIdsForDate(date);
    }

    public void insertCompletion(HabitCompletion completion) {
        repository.insertCompletion(completion);
    }

    public void deleteCompletion(int habitId, long date) {
        repository.deleteCompletion(habitId, date);
    }

    public LiveData<Integer> getCompletionCount(int habitId) {
        return repository.getCompletionCount(habitId);
    }

    public LiveData<List<HabitCompletion>> getCompletionsForHabit(int habitId) {
        return repository.getCompletionsForHabit(habitId);
    }

    public LiveData<List<HabitCompletion>> getAllCompletions() {
        return repository.getAllCompletions();
    }
}
