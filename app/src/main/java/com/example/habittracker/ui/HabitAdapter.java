package com.example.habittracker.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.data.Habit;
import com.example.habittracker.databinding.ItemHabitBinding;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitAdapter extends ListAdapter<Habit, HabitAdapter.HabitViewHolder> {

    private final OnHabitInteractionListener listener;
    private final Set<Integer> completedHabitIds = new HashSet<>();
    private long selectedDateMillis;
    private final HabitViewModel viewModel;

    public interface OnHabitInteractionListener {
        void onHabitChecked(Habit habit, boolean isChecked);
        void onHabitClicked(Habit habit);
    }

    public HabitAdapter(HabitViewModel viewModel, OnHabitInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
        this.listener = listener;
    }

    public void setCompletedHabitIds(List<Integer> ids, long dateMillis) {
        this.selectedDateMillis = dateMillis;
        completedHabitIds.clear();
        if (ids != null) {
            completedHabitIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<Habit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Habit>() {
        @Override
        public boolean areItemsTheSame(@NonNull Habit oldItem, @NonNull Habit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Habit oldItem, @NonNull Habit newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getFrequency().equals(newItem.getFrequency()) &&
                    oldItem.isRemindMeEnabled() == newItem.isRemindMeEnabled() &&
                    oldItem.isMuteNotificationEnabled() == newItem.isMuteNotificationEnabled();
        }
    };

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitBinding binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitBinding binding;

        HabitViewHolder(ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Habit habit) {
            binding.textHabitName.setText(habit.getName());
            
            // Streak as total count
            viewModel.getCompletionCount(habit.getId()).observeForever(count -> {
                binding.textStreak.setText(binding.getRoot().getContext().getString(
                        com.example.habittracker.R.string.streak_format, count != null ? count : 0));
            });

            boolean isDoneOnSelectedDate = completedHabitIds.contains(habit.getId());
            binding.checkboxDone.setOnCheckedChangeListener(null);
            binding.checkboxDone.setChecked(isDoneOnSelectedDate);

            // Enforce restrictions
            boolean canEdit = isWithinAllowedRange(selectedDateMillis);
            binding.checkboxDone.setEnabled(canEdit);

            if (isDoneOnSelectedDate) {
                binding.textHabitName.setPaintFlags(binding.textHabitName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                binding.textHabitName.setPaintFlags(binding.textHabitName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            binding.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onHabitChecked(habit, isChecked));
            binding.getRoot().setOnClickListener(v -> listener.onHabitClicked(habit));
        }

        private boolean isWithinAllowedRange(long dateMillis) {
            long todayStart = getStartOfDay(System.currentTimeMillis());
            if (dateMillis > todayStart) return false; // No future

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(todayStart);
            cal.add(Calendar.DAY_OF_YEAR, -2); // 3 days total: today, yesterday, day before
            long threeDaysAgoStart = cal.getTimeInMillis();

            return dateMillis >= threeDaysAgoStart;
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
    }
}
