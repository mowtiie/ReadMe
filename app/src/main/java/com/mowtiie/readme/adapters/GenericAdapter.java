package com.mowtiie.readme.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowtiie.readme.databinding.ItemRowBinding;

import java.util.ArrayList;
import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {

    public interface Binder<T> {
        void bind(ItemRowBinding row, T item);
    }

    private final List<T> items = new ArrayList<>();
    private final Binder<T> binder;

    public GenericAdapter(List<T> items, Binder<T> binder) {
        this.items.addAll(items);
        this.binder = binder;
    }

    public void setItems(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        binder.bind(holder.row, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemRowBinding row;

        ViewHolder(ItemRowBinding row) {
            super(row.getRoot());
            this.row = row;
        }
    }
}
