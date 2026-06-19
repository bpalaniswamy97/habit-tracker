package com.example.habittracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.data.DataBackupManager;
import com.example.habittracker.databinding.FragmentHelpBinding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HelpFragment extends Fragment {

    private FragmentHelpBinding binding;

    private final ActivityResultLauncher<String> createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    exportData(uri);
                }
            }
    );

    private final ActivityResultLauncher<String[]> openDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    importData(uri);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonExport.setOnClickListener(v -> {
            createDocumentLauncher.launch("habit_tracker_backup.json");
        });

        binding.buttonImport.setOnClickListener(v -> {
            openDocumentLauncher.launch(new String[]{"application/json"});
        });
    }

    private void exportData(Uri uri) {
        new Thread(() -> {
            try {
                String json = DataBackupManager.exportDataToJson(requireContext());
                try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }
                }
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Data exported successfully", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void importData(Uri uri) {
        new Thread(() -> {
            try {
                String json;
                try (InputStream is = requireContext().getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    json = reader.lines().collect(Collectors.joining("\n"));
                }
                DataBackupManager.importDataFromJson(requireContext(), json);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Data imported successfully", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
