package com.photos.ui;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.photos.R;
import com.photos.model.Album;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    public interface OnAlbumClickListener {
        void onAlbumClick(int position);
        void onAlbumLongClick(int position);
    }
    private final ArrayList<Album> albums;
    private final OnAlbumClickListener listener;

    public AlbumAdapter(ArrayList<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumThumbnail;
        TextView albumName;

        public ViewHolder(View view) {
            super(view);
            albumThumbnail = view.findViewById(R.id.albumThumbnail);
            albumName = view.findViewById(R.id.albumName);

            view.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    listener.onAlbumClick(position);
                }
            });

            view.setOnLongClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    listener.onAlbumLongClick(position);
                }
                return true;
            });
        }
    }

    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.ViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.albumName.setText(album.getName());
        holder.albumThumbnail.setImageResource(R.drawable.default_album_thumbnail);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}