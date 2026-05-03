package com.photos.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.photos.R;
import com.photos.model.Tag;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder>{
    public interface OnTagLongClickListener{
        void onTagLongClick(int position);
    }

    private final List<Tag> tags;
    private final OnTagLongClickListener listener;

    public TagAdapter(List<Tag> tags, OnTagLongClickListener listener){
        this.tags = tags;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tagText;
        public ViewHolder(View view){
            super(view);
            tagText = view.findViewById(R.id.tagText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);

        ViewHolder vh = new ViewHolder(view);
        view.setOnLongClickListener(v -> {
            int pos = vh.getAbsoluteAdapterPosition();
            if(pos!=RecyclerView.NO_POSITION){
                listener.onTagLongClick(pos);
            }
            return true;
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        holder.tagText.setText(tags.get(position).toString());
    }

    @Override
    public int getItemCount(){
        return tags.size();
    }
}