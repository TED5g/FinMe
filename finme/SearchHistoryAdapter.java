package com.example.finme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private final List<String> searchHistory;
    private final LayoutInflater mInflater;
    private final ItemClickListener mClickListener;

    public SearchHistoryAdapter(Context context, List<String> data, ItemClickListener clickListener) {
        this.mInflater = LayoutInflater.from(context);
        this.searchHistory = data;
        this.mClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.myTextView.setText(searchHistory.get(position));
        holder.itemView.setOnClickListener(v -> mClickListener.onItemClick(searchHistory.get(position)));
    }

    @Override
    public int getItemCount() {
        return searchHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.info_text);
        }
    }

    public interface ItemClickListener {
        void onItemClick(String cityName);
    }
}
