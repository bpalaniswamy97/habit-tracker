package com.example.habittracker.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.databinding.FragmentFocusBinding;

import java.util.Locale;

public class FocusFragment extends Fragment {

    private FragmentFocusBinding binding;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFocusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonStart.setOnClickListener(v -> {
            if (isTimerRunning) {
                stopTimer();
            } else {
                startTimer();
            }
        });

        binding.buttonReset.setOnClickListener(v -> resetTimer());

        updateCountDownText(0); // Initial state
    }

    private void startTimer() {
        String minStr = binding.editMinutes.getText().toString();
        String secStr = binding.editSeconds.getText().toString();

        int minutes = minStr.isEmpty() ? 0 : Integer.parseInt(minStr);
        int seconds = secStr.isEmpty() ? 0 : Integer.parseInt(secStr);

        if (minutes == 0 && seconds == 0) {
            Toast.makeText(getContext(), "Please set a duration", Toast.LENGTH_SHORT).show();
            return;
        }

        timeLeftInMillis = (minutes * 60L + seconds) * 1000;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText(timeLeftInMillis);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                binding.buttonStart.setText("Start Focus");
                binding.layoutInputs.setVisibility(View.VISIBLE);
                showTimesUpDialog();
            }
        }.start();

        isTimerRunning = true;
        binding.buttonStart.setText("Stop");
        binding.layoutInputs.setVisibility(View.GONE);
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        binding.buttonStart.setText("Start Focus");
        binding.layoutInputs.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        stopTimer();
        updateCountDownText(0);
        binding.editMinutes.setText("25");
        binding.editSeconds.setText("00");
    }

    private void updateCountDownText(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;

        if (millis == 0 && !isTimerRunning) {
            // Just show the initial values from inputs
            String minStr = binding.editMinutes.getText().toString();
            String secStr = binding.editSeconds.getText().toString();
            try {
                int m = minStr.isEmpty() ? 0 : Integer.parseInt(minStr);
                int s = secStr.isEmpty() ? 0 : Integer.parseInt(secStr);
                binding.textTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
            } catch (NumberFormatException e) {
                binding.textTimerDisplay.setText("00:00");
            }
        } else {
            binding.textTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        }
    }

    private void showTimesUpDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Focus Session Finished")
                .setMessage("Great job! Your focus time is up.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding = null;
    }
}
