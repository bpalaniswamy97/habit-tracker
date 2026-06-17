package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitCompletion;
import com.example.habittracker.databinding.FragmentFirstBinding;
import com.example.habittracker.ui.HabitAdapter;
import com.example.habittracker.ui.HabitViewModel;

import java.util.Calendar;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private HabitViewModel habitViewModel;
    private long selectedDateMillis;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        // Initialize with today's date (start of day)
        selectedDateMillis = getStartOfDay(System.currentTimeMillis());

        HabitAdapter adapter = new HabitAdapter((habit, isChecked) -> {
            if (isChecked) {
                markHabitAsDone(habit, selectedDateMillis);
            } else {
                unmarkHabitAsDone(habit, selectedDateMillis);
            }
        });

        binding.recyclerViewHabits.setAdapter(adapter);

        // Observe habits
        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            updateUI(adapter, habits);
        });

        // Observe completions for selected date
        observeCompletions(adapter);

        binding.calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = calendar.getTimeInMillis();
            
            // Re-observe for the new date
            observeCompletions(adapter);
        });
    }

    private void observeCompletions(HabitAdapter adapter) {
        // Remove previous observers to avoid leaks and multiple triggers
        habitViewModel.getCompletedHabitIdsForDate(selectedDateMillis).removeObservers(getViewLifecycleOwner());
        habitViewModel.getCompletedHabitIdsForDate(selectedDateMillis).observe(getViewLifecycleOwner(), ids -> {
            adapter.setCompletedHabitIds(ids);
        });
    }

    private void updateUI(HabitAdapter adapter, java.util.List<Habit> habits) {
        if (habits == null || habits.isEmpty()) {
            binding.textEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewHabits.setVisibility(View.GONE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
            binding.recyclerViewHabits.setVisibility(View.VISIBLE);
            adapter.submitList(habits);
        }
    }

    private void markHabitAsDone(Habit habit, long dateMillis) {
        habitViewModel.insertCompletion(new HabitCompletion(habit.getId(), dateMillis));
        
        // Update streak only if marked for today
        if (dateMillis == getStartOfDay(System.currentTimeMillis())) {
            Habit updatedHabit = new Habit(habit);
            updatedHabit.setCurrentStreak(updatedHabit.getCurrentStreak() + 1);
            updatedHabit.setLastCompletedDate(System.currentTimeMillis());
            habitViewModel.update(updatedHabit);
        }
    }

    private void unmarkHabitAsDone(Habit habit, long dateMillis) {
        habitViewModel.deleteCompletion(habit.getId(), dateMillis);
        
        // Update streak only if unmarked for today
        if (dateMillis == getStartOfDay(System.currentTimeMillis())) {
            Habit updatedHabit = new Habit(habit);
            if (updatedHabit.getCurrentStreak() > 0) {
                updatedHabit.setCurrentStreak(updatedHabit.getCurrentStreak() - 1);
            }
            // Note: Ideally we'd find the previous completion date, but for simplicity:
            updatedHabit.setLastCompletedDate(null); 
            habitViewModel.update(updatedHabit);
        }
    }

    private long getStartOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
