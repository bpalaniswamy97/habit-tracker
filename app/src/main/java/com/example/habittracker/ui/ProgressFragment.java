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

import com.example.habittracker.data.HabitCompletion;
import com.example.habittracker.databinding.FragmentProgressBinding;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
