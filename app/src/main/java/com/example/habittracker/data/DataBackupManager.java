package com.example.habittracker.data;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class DataBackupManager {

    public static String exportDataToJson(Context context) throws Exception {
        AppDatabase db = AppDatabase.getDatabase(context);
        HabitDao dao = db.habitDao();

        List<Habit> habits = dao.getAllHabitsSync();
        List<HabitCompletion> completions = dao.getAllCompletionsSync();

        JSONObject backup = new JSONObject();
        JSONArray habitsArray = new JSONArray();
        for (Habit h : habits) {
            JSONObject habitJson = new JSONObject();
            habitJson.put("id", h.getId());
            habitJson.put("name", h.getName());
            habitJson.put("frequency", h.getFrequency());
            habitJson.put("reminderTime", h.getReminderTime());
            habitJson.put("lastCompletedDate", h.getLastCompletedDate());
            habitJson.put("isRemindMeEnabled", h.isRemindMeEnabled());
            habitJson.put("isMuteNotificationEnabled", h.isMuteNotificationEnabled());
            habitJson.put("startDate", h.getStartDate());
            habitJson.put("reminderSoundUri", h.getReminderSoundUri());
            habitJson.put("isArchived", h.isArchived());
            habitJson.put("isHiddenFromProgress", h.isHiddenFromProgress());
            habitsArray.put(habitJson);
        }
        backup.put("habits", habitsArray);

        JSONArray completionsArray = new JSONArray();
        for (HabitCompletion c : completions) {
            JSONObject compJson = new JSONObject();
            compJson.put("habitId", c.getHabitId());
            compJson.put("date", c.getDate());
            completionsArray.put(compJson);
        }
        backup.put("completions", completionsArray);

        return backup.toString(4);
    }

    public static void importDataFromJson(Context context, String json) throws Exception {
        AppDatabase db = AppDatabase.getDatabase(context);
        HabitDao dao = db.habitDao();

        JSONObject backup = new JSONObject(json);
        JSONArray habitsArray = backup.getJSONArray("habits");
        JSONArray completionsArray = backup.getJSONArray("completions");

        db.runInTransaction(() -> {
            try {
                for (int i = 0; i < habitsArray.length(); i++) {
                    JSONObject habitJson = habitsArray.getJSONObject(i);
                    Habit h = new Habit(habitJson.getString("name"), 
                                       habitJson.getString("frequency"), 
                                       habitJson.getString("reminderTime"));
                    h.setId(habitJson.getInt("id"));
                    if (!habitJson.isNull("lastCompletedDate")) {
                        h.setLastCompletedDate(habitJson.getLong("lastCompletedDate"));
                    }
                    h.setRemindMeEnabled(habitJson.getBoolean("isRemindMeEnabled"));
                    h.setMuteNotificationEnabled(habitJson.getBoolean("isMuteNotificationEnabled"));
                    h.setStartDate(habitJson.getLong("startDate"));
                    if (!habitJson.isNull("reminderSoundUri")) {
                        h.setReminderSoundUri(habitJson.getString("reminderSoundUri"));
                    }
                    h.setArchived(habitJson.getBoolean("isArchived"));
                    h.setHiddenFromProgress(habitJson.getBoolean("isHiddenFromProgress"));
                    
                    dao.insert(h); 
                }

                for (int i = 0; i < completionsArray.length(); i++) {
                    JSONObject compJson = completionsArray.getJSONObject(i);
                    HabitCompletion c = new HabitCompletion(compJson.getInt("habitId"), 
                                                           compJson.getLong("date"));
                    dao.insertCompletion(c);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
