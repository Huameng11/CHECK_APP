package com.example.a03_check;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    private List<String> suggestionList;
    private OnSuggestionClickListener listener;

    public SuggestionAdapter(List<String> suggestionList, OnSuggestionClickListener listener) {
        this.suggestionList = suggestionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestionList.get(position);
        holder.suggestionText.setText(suggestion);
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(suggestion));
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView suggestionText;

        SuggestionViewHolder(View itemView) {
            super(itemView);
            suggestionText = itemView.findViewById(R.id.suggestion_text);
        }
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }
}