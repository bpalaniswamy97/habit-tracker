package com.example.habittracker.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.data.Habit;
import com.example.habittracker.databinding.ItemHabitProgressBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProgressAdapter extends ListAdapter<Habit, ProgressAdapter.ProgressViewHolder> {

    private Map<Integer, Set<Long>> habitCompletions = new HashMap<>();
    private OnHabitDeleteListener deleteListener;

    public interface OnHabitDeleteListener {
        void onDeleteRequested(Habit habit);
    }

    public ProgressAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnHabitDeleteListener(OnHabitDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setCompletions(Map<Integer, Set<Long>> completions) {
        this.habitCompletions = completions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitProgressBinding binding = ItemHabitProgressBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProgressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        Habit habit = getItem(position);
        holder.bind(habit, habitCompletions.getOrDefault(habit.getId(), new HashSet<>()), deleteListener);
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitProgressBinding binding;

        ProgressViewHolder(ItemHabitProgressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Habit habit, Set<Long> completions, OnHabitDeleteListener listener) {
            binding.habitName.setText(habit.getName());
            binding.habitStats.setText("Total completions: " + completions.size());
            binding.heatmapView.setCompletions(completions);
            binding.buttonDeleteProgress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteRequested(habit);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<Habit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Habit>() {
        @Override
        public boolean areItemsTheSame(@NonNull Habit oldItem, @NonNull Habit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Habit oldItem, @NonNull Habit newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getFrequency().equals(newItem.getFrequency());
        }
    };
}
