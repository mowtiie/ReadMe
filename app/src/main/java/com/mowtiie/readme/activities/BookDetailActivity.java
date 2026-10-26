package com.mowtiie.readme.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.readme.data.Data;
import com.mowtiie.readme.databinding.ActivityBookDetailBinding;

public class BookDetailActivity extends AppCompatActivity {

    private ActivityBookDetailBinding binding;
    private String bookId;
    private boolean holdsBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("book_id");
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnAction.setOnClickListener(v -> {
            if (holdsBook) confirmReturn();
            else checkout();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        // {id, title, author, genre, summary, published, status}
        String[] book = Data.getBook(this, bookId);
        binding.tvTitle.setText(book[1]);
        binding.tvAuthor.setText(book[2]);
        binding.tvGenre.setText("Genre: " + book[3]);
        binding.tvPublished.setText("Published: " + book[5]);
        binding.tvStatus.setText(Data.statusText(book[6]));
        binding.tvSummary.setText(book[4]);

        boolean available = book[6].equals(Data.STATUS_AVAILABLE);
        holdsBook = !available && Data.holdsBook(this, bookId);
        if (holdsBook) {
            binding.btnAction.setEnabled(true);
            binding.btnAction.setText("Return");
        } else {
            binding.btnAction.setEnabled(available);
            binding.btnAction.setText(available ? "Check out" : "Unavailable");
        }
    }

    // Anchored above the sticky bar so it does not cover the button
    private void snack(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).setAnchorView(binding.llBottomBar).show();
    }

    private void checkout() {
        // The screen may be stale, so check the database before asking
        if (!Data.getBook(this, bookId)[6].equals(Data.STATUS_AVAILABLE)) {
            snack("This book is no longer available");
            refresh();
            return;
        }
        String due = Data.date(Data.LOAN_DAYS);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Check out this book?")
                .setMessage("It will be due on " + due + ".")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Check out", (dialog, which) -> {
                    if (Data.checkout(this, bookId)) {
                        snack("Checked out. Due " + due);
                    } else {
                        snack("This book is no longer available");
                    }
                    refresh();
                })
                .show();
    }

    private void confirmReturn() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Return this book?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Return", (dialog, which) -> {
                    Data.returnBook(this, bookId);
                    snack("Book returned");
                    refresh();
                })
                .show();
    }
}
