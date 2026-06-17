package com.example.habittracker.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_completions",
        foreignKeys = @ForeignKey(entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("habitId")})
public class HabitCompletion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int habitId;
    private long date; // Start of day timestamp (millis)

    public HabitCompletion(int habitId, long date) {
        this.habitId = habitId;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHabitId() { return habitId; }
    public void setHabitId(int habitId) { this.habitId = habitId; }
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
}
