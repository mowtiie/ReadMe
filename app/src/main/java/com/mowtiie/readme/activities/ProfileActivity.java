package com.mowtiie.readme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mowtiie.readme.adapters.GenericAdapter;
import com.mowtiie.readme.data.Data;
import com.mowtiie.readme.databinding.ActivityProfileBinding;
import com.mowtiie.readme.databinding.SheetEditNameBinding;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvName.setText(Data.USER_NAME);
        binding.tvEmail.setText(Data.USER_EMAIL);

        binding.btnEdit.setOnClickListener(v -> showEditSheet());
        binding.btnLogout.setOnClickListener(v -> {
            Data.logout();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        // row = {loan id, book id, title, author, borrowed_on, due_on, returned_on}
        adapter = new GenericAdapter<>(new ArrayList<>(), (row, loan) -> {
            row.tvTitle.setText(loan[2]);
            row.tvSubtitle.setText(loan[3]);
            if (loan[6].isEmpty()) {
                row.tvMeta.setText("Due " + loan[5]);
                row.card.setOnClickListener(v -> confirmReturn(loan));
            } else {
                row.tvMeta.setText("Borrowed " + loan[4] + " • Returned " + loan[6]);
                row.card.setOnClickListener(null);
                row.card.setClickable(false);
            }
        });
        binding.rvLoans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoans.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadLoans();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLoans();
    }

    private void loadLoans() {
        boolean returned = binding.tabLayout.getSelectedTabPosition() == 1;
        ArrayList<String[]> loans = Data.myLoans(this, returned);
        adapter.setItems(loans);
        binding.viewEmpty.tvEmptyTitle.setText(returned ? "No returned books" : "No borrowed books");
        binding.viewEmpty.tvEmptyMessage.setText(returned ? "Books you return will show up here." : "Check out a book from the catalog.");
        binding.viewEmpty.getRoot().setVisibility(loans.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showEditSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        SheetEditNameBinding sheetBinding = SheetEditNameBinding.inflate(getLayoutInflater());
        sheet.setContentView(sheetBinding.getRoot());
        sheetBinding.etName.setText(Data.USER_NAME);
        sheetBinding.btnSave.setOnClickListener(v -> {
            sheetBinding.tilName.setError(null);
            String name = sheetBinding.etName.getText().toString().trim();
            if (name.isEmpty()) {
                sheetBinding.tilName.setError("Enter your name");
                return;
            }
            Data.updateName(this, name);
            binding.tvName.setText(name);
            sheet.dismiss();
            Snackbar.make(binding.getRoot(), "Name updated", Snackbar.LENGTH_SHORT).show();
        });
        sheet.show();
    }

    private void confirmReturn(String[] loan) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Return this book?")
                .setMessage(loan[2])
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Return", (dialog, which) -> {
                    Data.returnBook(this, loan[1]);
                    Snackbar.make(binding.getRoot(), "Book returned", Snackbar.LENGTH_SHORT).show();
                    loadLoans();
                })
                .show();
    }
}
