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
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
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

        public ViewHolder(View view) {
            super(view);
            photoThumb = view.findViewById(R.id.photoThumbnail);
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

    private Bitmap loadScaledBitmap(Context context, String uriString, int reqWidth, int reqHeight) {
        try {
            Uri uri = Uri.parse(uriString);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            InputStream input1 = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(input1, null, options);
            if (input1 != null) input1.close();

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            InputStream input2 = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input2, null, options);
            if (input2 != null) input2.close();

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo photo = photos.get(position);

        holder.photoThumb.setImageBitmap(
                loadScaledBitmap(holder.itemView.getContext(), photo.getFilePath(), 600, 600)
        );
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
}