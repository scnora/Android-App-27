package com.photos.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.photos.R;
import com.photos.model.Photo;
import java.io.File;
import java.util.List;
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder>{
    public interface OnPhotoClickListener{
        void onPhotoClick(int position);
        void onPhotoLongClick(int position);
    }

    private final List<Photo> photos;
    private final OnPhotoClickListener listener;
    public PhotoAdapter(List<Photo> photos, OnPhotoClickListener listener){
        this.photos = photos;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoThumb;
        TextView photoName;

        public ViewHolder(View view) {
            super(view);
            photoThumb = view.findViewById(R.id.photoThumbnail);
            photoName = view.findViewById(R.id.photoName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        ViewHolder vh = new ViewHolder(view);

        view.setOnClickListener(v -> {
            int pos = vh.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onPhotoClick(pos);
        });

        view.setOnLongClickListener(v -> {
            int pos = vh.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION){
                listener.onPhotoLongClick(pos);
            }
            return true;
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo photo = photos.get(position);
        String filePath = photo.getFilePath();

        // Load image from file path
        File imgFile = new File(filePath);
        if (imgFile.exists()) {
            holder.photoThumb.setImageURI(Uri.fromFile(imgFile));
        } else {
            holder.photoThumb.setImageResource(R.drawable.default_album_thumbnail);
        }

        // Show just the filename
        String name = imgFile.getName();
        holder.photoName.setText(name);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
}