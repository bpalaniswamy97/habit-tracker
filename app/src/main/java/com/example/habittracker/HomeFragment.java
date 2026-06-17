package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitCompletion;
import com.example.habittracker.databinding.FragmentHomeBinding;
import com.example.habittracker.ui.HabitAdapter;
import com.example.habittracker.ui.HabitViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HabitViewModel habitViewModel;
    private long selectedDateMillis;
    private List<View> dateViews = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        selectedDateMillis = getStartOfDay(System.currentTimeMillis());

        HabitAdapter adapter = new HabitAdapter(habitViewModel, new HabitAdapter.OnHabitInteractionListener() {
            @Override
            public void onHabitChecked(Habit habit, boolean isChecked) {
                if (isChecked) {
                    habitViewModel.insertCompletion(new HabitCompletion(habit.getId(), selectedDateMillis));
                } else {
                    habitViewModel.deleteCompletion(habit.getId(), selectedDateMillis);
                }
            }

            @Override
            public void onHabitClicked(Habit habit) {
                Bundle args = new Bundle();
                args.putInt("habit_id", habit.getId());
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_global_addHabitFragment, args);
            }
        });

        binding.recyclerViewHabits.setAdapter(adapter);

        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            updateFilteredList(adapter, habits);
        });

        setupScrollableCalendar(adapter);
    }

    private void setupScrollableCalendar(HabitAdapter adapter) {
        binding.calendarContainer.removeAllViews();
        dateViews.clear();

        Calendar cal = Calendar.getInstance();
        long todayMillis = getStartOfDay(cal.getTimeInMillis());
        
        // Start from 14 days ago
        cal.add(Calendar.DAY_OF_YEAR, -14);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());

        // Calculate item width to show exactly 7 days
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (8 * getResources().getDisplayMetrics().density * 2); // scroll_calendar padding
        int itemWidth = (screenWidth - padding) / 7;

        int todayIndex = -1;

        for (int i = 0; i < 29; i++) {
            View dateView = getLayoutInflater().inflate(R.layout.item_date, binding.calendarContainer, false);
            
            // Set fixed width
            ViewGroup.LayoutParams lp = dateView.getLayoutParams();
            lp.width = itemWidth;
            dateView.setLayoutParams(lp);

            TextView textDay = dateView.findViewById(R.id.text_day);
            TextView textDate = dateView.findViewById(R.id.text_date);

            textDay.setText(dayFormat.format(cal.getTime()));
            textDate.setText(dateFormat.format(cal.getTime()));

            long dateMillis = getStartOfDay(cal.getTimeInMillis());
            dateView.setTag(dateMillis);

            if (dateMillis == todayMillis) {
                todayIndex = i;
            }

            dateView.setOnClickListener(v -> {
                selectedDateMillis = (long) v.getTag();
                updateSelectedDateUI();
                updateFilteredList(adapter, habitViewModel.getAllHabits().getValue());
                observeCompletions(adapter);
            });

            binding.calendarContainer.addView(dateView);
            dateViews.add(dateView);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        updateSelectedDateUI();
        observeCompletions(adapter);

        // Center on Today
        if (todayIndex != -1) {
            final int scrollToIndex = todayIndex;
            binding.scrollCalendar.post(() -> {
                // Scroll so that today is in the middle (index 3 of the 7 visible ones)
                int scrollX = (scrollToIndex - 3) * itemWidth;
                binding.scrollCalendar.scrollTo(scrollX, 0);
            });
        }
    }

    private void updateFilteredList(HabitAdapter adapter, List<Habit> allHabits) {
        if (allHabits == null) return;
        List<Habit> filtered = new ArrayList<>();
        for (Habit h : allHabits) {
            if (shouldShowHabit(h, selectedDateMillis)) {
                filtered.add(h);
            }
        }

        if (filtered.isEmpty()) {
            binding.textEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewHabits.setVisibility(View.GONE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
            binding.recyclerViewHabits.setVisibility(View.VISIBLE);
            adapter.submitList(filtered);
        }
    }

    private boolean shouldShowHabit(Habit h, long dateMillis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(getStartOfDay(h.getStartDate()));
        
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(dateMillis);

        if (current.before(start)) return false;

        switch (h.getFrequency()) {
            case "Every Day":
                return true;
            case "Every 2 Days":
                long diffDays = (dateMillis - start.getTimeInMillis()) / (24 * 60 * 60 * 1000);
                return diffDays % 2 == 0;
            case "Weekly":
                return start.get(Calendar.DAY_OF_WEEK) == current.get(Calendar.DAY_OF_WEEK);
            case "Biweekly":
                long diffWeeks = (dateMillis - start.getTimeInMillis()) / (7 * 24 * 60 * 60 * 1000);
                return (diffWeeks % 2 == 0) && (start.get(Calendar.DAY_OF_WEEK) == current.get(Calendar.DAY_OF_WEEK));
            case "Monthly":
                return start.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH);
            default:
                return true;
        }
    }

    private void updateSelectedDateUI() {
        int colorOnPrimary = ContextCompat.getColor(requireContext(), android.R.color.white);
        
        // Fetch theme colors
        android.util.TypedValue typedValue = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int colorOnSurface = typedValue.data;
        
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true);
        int colorOnSurfaceVariant = typedValue.data;

        for (View view : dateViews) {
            long dateMillis = (long) view.getTag();
            TextView textDay = view.findViewById(R.id.text_day);
            TextView textDate = view.findViewById(R.id.text_date);

            if (dateMillis == selectedDateMillis) {
                view.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_date_selected));
                textDay.setTextColor(colorOnPrimary);
                textDate.setTextColor(colorOnPrimary);
            } else {
                view.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_date_unselected));
                textDay.setTextColor(colorOnSurfaceVariant);
                textDate.setTextColor(colorOnSurface);
            }
        }
    }

    private void observeCompletions(HabitAdapter adapter) {
        habitViewModel.getCompletedHabitIdsForDate(selectedDateMillis).removeObservers(getViewLifecycleOwner());
        habitViewModel.getCompletedHabitIdsForDate(selectedDateMillis).observe(getViewLifecycleOwner(), ids -> {
            adapter.setCompletedHabitIds(ids, selectedDateMillis);
        });
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
