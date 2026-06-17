package com.example.habittracker.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String frequency; // DAILY, EVERY_2_DAYS, WEEKLY, BIWEEKLY, MONTHLY
    private String reminderTime; // e.g., "08:00"
    private Long lastCompletedDate; // timestamp in millis
    private boolean isRemindMeEnabled;
    private boolean isMuteNotificationEnabled;
    private long startDate;

    public Habit(String name, String frequency, String reminderTime) {
        this.name = name;
        this.frequency = frequency;
        this.reminderTime = reminderTime;
        this.lastCompletedDate = null;
        this.isRemindMeEnabled = false;
        this.isMuteNotificationEnabled = false;
        this.startDate = System.currentTimeMillis();
    }

    // Copy constructor
    public Habit(Habit other) {
        this.id = other.id;
        this.name = other.name;
        this.frequency = other.frequency;
        this.reminderTime = other.reminderTime;
        this.lastCompletedDate = other.lastCompletedDate;
        this.isRemindMeEnabled = other.isRemindMeEnabled;
        this.isMuteNotificationEnabled = other.isMuteNotificationEnabled;
        this.startDate = other.startDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
    public Long getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(Long lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }
    public boolean isRemindMeEnabled() { return isRemindMeEnabled; }
    public void setRemindMeEnabled(boolean remindMeEnabled) { isRemindMeEnabled = remindMeEnabled; }
    public boolean isMuteNotificationEnabled() { return isMuteNotificationEnabled; }
    public void setMuteNotificationEnabled(boolean muteNotificationEnabled) { isMuteNotificationEnabled = muteNotificationEnabled; }
    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }
}
