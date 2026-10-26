package com.mowtiie.readme.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.readme.data.Data;
import com.mowtiie.readme.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnReset.setOnClickListener(v -> {
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Enter a valid email");
                return;
            }
            if (password.length() < 6) {
                binding.tilPassword.setError("Password must be at least 6 characters");
                return;
            }
            if (!Data.resetPassword(this, email, password)) {
                binding.tilEmail.setError("No account found with this email");
                return;
            }
            Toast.makeText(this, "Password updated. Log in with your new password.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
