package com.example.habittracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitCompletion;
import com.example.habittracker.databinding.FragmentProgressBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProgressFragment extends Fragment {

    private FragmentProgressBinding binding;
    private HabitViewModel habitViewModel;
    private ProgressAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        adapter = new ProgressAdapter();
        adapter.setOnHabitDeleteListener(this::showDeleteConfirmation);

        binding.progressRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.progressRecyclerView.setAdapter(adapter);

        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            adapter.submitList(habits);
        });

        habitViewModel.getAllCompletions().observe(getViewLifecycleOwner(), completions -> {
            Map<Integer, Set<Long>> completionMap = new HashMap<>();
            for (HabitCompletion completion : completions) {
                Set<Long> dates = completionMap.getOrDefault(completion.getHabitId(), new HashSet<>());
                dates.add(completion.getDate());
                completionMap.put(completion.getHabitId(), dates);
            }
            adapter.setCompletions(completionMap);
        });
    }

    private void showDeleteConfirmation(Habit habit) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove from Progress Bar?")
                .setMessage("Do you also want to remove the habit history?")
                .setPositiveButton("Remove Habit & History", (dialog, which) -> {
                    habit.setHiddenFromProgress(true);
                    habitViewModel.update(habit);
                    habitViewModel.deleteCompletionsByHabitId(habit.getId());
                })
                .setNeutralButton("Remove Habit Only", (dialog, which) -> {
                    habit.setHiddenFromProgress(true);
                    habitViewModel.update(habit);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
