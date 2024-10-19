package com.example.pork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pork.databinding.FragmentSetBinding;

public class SetFragment extends Fragment {

    private FragmentSetBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "prefs";
    private static final String KEY_ACCOUNT_NAME = "account_name";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize the binding
        binding = FragmentSetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Access views through binding
        EditText editTextText2 = binding.editTextText2;

        // Load saved account name
        String savedAccountName = sharedPreferences.getString(KEY_ACCOUNT_NAME, "帳戶名稱");
        editTextText2.setText(savedAccountName);

        // Set a focus change listener on EditText
        editTextText2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Save the updated account name to SharedPreferences when EditText loses focus
                    String newName = editTextText2.getText().toString();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_ACCOUNT_NAME, newName);
                    editor.apply();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear the binding when the view is destroyed
        binding = null;
    }
}
