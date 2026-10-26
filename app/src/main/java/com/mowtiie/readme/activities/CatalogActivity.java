package com.mowtiie.readme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.readme.R;
import com.mowtiie.readme.adapters.GenericAdapter;
import com.mowtiie.readme.data.Data;
import com.mowtiie.readme.databinding.ActivityCatalogBinding;

import java.util.ArrayList;

public class CatalogActivity extends AppCompatActivity {

    private ActivityCatalogBinding binding;
    private GenericAdapter<String[]> adapter;

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

        binding.toolbar.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });

        // row = {id, title, author, genre, status}
        adapter = new GenericAdapter<>(new ArrayList<>(), (row, book) -> {
            row.tvTitle.setText(book[1]);
            row.tvSubtitle.setText(book[2]);
            row.tvMeta.setText(book[3] + " • " + Data.statusText(book[4]));
            row.card.setOnClickListener(v -> startActivity(new Intent(this, BookDetailActivity.class).putExtra("book_id", book[0])));
        });
        binding.rvBooks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBooks.setAdapter(adapter);
        binding.viewEmpty.tvEmptyTitle.setText("No books found");
        binding.viewEmpty.tvEmptyMessage.setText("Try a different title, author or genre.");

        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> loadBooks());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadBooks();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) loadBooks();
    }

    private void loadBooks() {
        int chip = binding.chipGroup.getCheckedChipId();
        String filter = Data.SEARCH_ALL;
        if (chip == R.id.chipTitle) filter = Data.SEARCH_TITLE;
        else if (chip == R.id.chipAuthor) filter = Data.SEARCH_AUTHOR;
        else if (chip == R.id.chipGenre) filter = Data.SEARCH_GENRE;
        binding.tilSearch.setHelperText(filter.equals(Data.SEARCH_ALL) ? "Title, author or genre" : "Searching by " + filter);
        ArrayList<String[]> books = Data.searchBooks(this, binding.etSearch.getText().toString(), filter);
        adapter.setItems(books);
        binding.viewEmpty.getRoot().setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
