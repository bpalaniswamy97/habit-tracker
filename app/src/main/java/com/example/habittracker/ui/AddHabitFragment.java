package com.example.habittracker.ui;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.habittracker.data.Habit;
import com.example.habittracker.databinding.FragmentAddHabitBinding;
import com.example.habittracker.reminder.ReminderScheduler;

import java.util.Locale;

public class AddHabitFragment extends Fragment {

    private FragmentAddHabitBinding binding;
    private HabitViewModel habitViewModel;
    private String selectedTime = "";
    private String selectedSoundUri = "";
    private Habit editingHabit = null;

    private final ActivityResultLauncher<Intent> ringtonePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        selectedSoundUri = uri.toString();
                        updateSoundText(uri);
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        // Check if we are in edit mode
        if (getArguments() != null && getArguments().containsKey("habit_id")) {
            int habitId = getArguments().getInt("habit_id");
            new Thread(() -> {
                editingHabit = com.example.habittracker.data.AppDatabase.getDatabase(requireContext())
                        .habitDao().getHabitById(habitId);
                if (editingHabit != null) {
                    requireActivity().runOnUiThread(() -> {
                        populateFields(editingHabit);
                        binding.buttonRemoveRoutine.setVisibility(View.VISIBLE);
                    });
                }
            }).start();
        }

        binding.buttonTimePicker.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view1, hourOfDay, minute) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        binding.textSelectedTime.setText(selectedTime);
                    }, 12, 0, true);
            timePickerDialog.show();
        });

        binding.buttonSoundPicker.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, 
                    selectedSoundUri.isEmpty() ? null : Uri.parse(selectedSoundUri));
            ringtonePickerLauncher.launch(intent);
        });

        binding.buttonSave.setOnClickListener(v -> saveHabit());

        binding.buttonRemoveRoutine.setOnClickListener(v -> removeFromRoutine());
    }

    private void removeFromRoutine() {
        if (editingHabit != null) {
            editingHabit.setArchived(true);
            habitViewModel.update(editingHabit);
            Toast.makeText(getContext(), "Habit removed from daily routine", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(AddHabitFragment.this).navigateUp();
        }
    }

    private void updateSoundText(Uri uri) {
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
        if (ringtone != null) {
            binding.textSelectedSound.setText(ringtone.getTitle(getContext()));
        }
    }

    private void populateFields(Habit habit) {
        binding.editHabitName.setText(habit.getName());
        selectedTime = habit.getReminderTime();
        binding.textSelectedTime.setText(selectedTime);
        binding.switchRemindMe.setChecked(habit.isRemindMeEnabled());
        binding.switchMute.setChecked(habit.isMuteNotificationEnabled());
        
        selectedSoundUri = habit.getReminderSoundUri() != null ? habit.getReminderSoundUri() : "";
        if (!selectedSoundUri.isEmpty()) {
            updateSoundText(Uri.parse(selectedSoundUri));
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                com.example.habittracker.R.array.frequency_options, android.R.layout.simple_spinner_item);
        int position = adapter.getPosition(habit.getFrequency());
        binding.spinnerFrequency.setSelection(position);
    }

    private void saveHabit() {
        String name = binding.editHabitName.getText().toString().trim();
        String frequency = binding.spinnerFrequency.getSelectedItem().toString();
        boolean isRemindMe = binding.switchRemindMe.isChecked();
        boolean isMute = binding.switchMute.isChecked();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingHabit != null) {
            editingHabit.setName(name);
            editingHabit.setFrequency(frequency);
            editingHabit.setReminderTime(selectedTime);
            editingHabit.setRemindMeEnabled(isRemindMe);
            editingHabit.setMuteNotificationEnabled(isMute);
            editingHabit.setReminderSoundUri(selectedSoundUri.isEmpty() ? null : selectedSoundUri);
            habitViewModel.update(editingHabit);
            ReminderScheduler.scheduleReminder(requireContext(), editingHabit);
            Toast.makeText(getContext(), "Habit updated!", Toast.LENGTH_SHORT).show();
        } else {
            Habit habit = new Habit(name, frequency, selectedTime);
            habit.setRemindMeEnabled(isRemindMe);
            habit.setMuteNotificationEnabled(isMute);
            habit.setReminderSoundUri(selectedSoundUri.isEmpty() ? null : selectedSoundUri);
            habitViewModel.insert(habit, id -> {
                habit.setId((int) id);
                ReminderScheduler.scheduleReminder(requireContext(), habit);
            });
            Toast.makeText(getContext(), "Habit saved!", Toast.LENGTH_SHORT).show();
        }
        NavHostFragment.findNavController(AddHabitFragment.this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
