package com.mowtiie.readme.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.readme.data.Data;
import com.mowtiie.readme.databinding.ActivityCatalogBinding;

// Placeholder until the catalog is built: only shows who is logged in
public class CatalogActivity extends AppCompatActivity {

    private ActivityCatalogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // USER_* is empty if the process was killed and the task restored
        if (Data.USER_ID == 0) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }
        binding.tvWelcome.setText("Welcome, " + Data.USER_NAME);
    }
}
