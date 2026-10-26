package com.mowtiie.readme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.readme.data.Data;
import com.mowtiie.readme.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSignUp.setOnClickListener(v -> {
            binding.tilName.setError(null);
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            binding.tilConfirm.setError(null);
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();
            String confirm = binding.etConfirm.getText().toString();

            if (name.isEmpty()) {
                binding.tilName.setError("Enter your name");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Enter a valid email");
                return;
            }
            if (Data.emailExists(this, email)) {
                binding.tilEmail.setError("An account with this email already exists");
                return;
            }
            if (password.length() < 6) {
                binding.tilPassword.setError("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(confirm)) {
                binding.tilConfirm.setError("Passwords do not match");
                return;
            }
            Data.signUp(this, name, email, password);
            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CatalogActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}
