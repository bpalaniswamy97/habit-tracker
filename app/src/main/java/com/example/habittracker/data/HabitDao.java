package com.example.habittracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    long insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);

    @Query("SELECT * FROM habits ORDER BY id DESC")
    LiveData<List<Habit>> getAllHabits();

    @Query("SELECT * FROM habits WHERE id = :id")
    Habit getHabitById(int id);

    // Completion operations
    @Insert
    void insertCompletion(HabitCompletion completion);

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    void deleteCompletion(int habitId, long date);

    @Query("SELECT habitId FROM habit_completions WHERE date = :date")
    LiveData<List<Integer>> getCompletedHabitIdsForDate(long date);

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    LiveData<Integer> getCompletionCount(int habitId);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    List<HabitCompletion> getCompletionsForHabit(int habitId);
}
