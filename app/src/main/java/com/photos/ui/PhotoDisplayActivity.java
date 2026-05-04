package com.photos.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.photos.R;
import com.photos.model.Album;
import com.photos.model.Photo;
import com.photos.model.Tag;
import com.photos.util.StorageUtil;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class PhotoDisplayActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_INDEX = "album_index";
    public static final String EXTRA_PHOTO_INDEX = "photo_index";

    private ArrayList<Album> albums;
    private Album album;
    private int albumIndex;
    private int photoIndex;

    private TextView backButton;
    private ImageView mainPhoto;
    private RecyclerView photoThumbnailRecyclerView;
    private Button addTagButton;
    private RecyclerView tagRecyclerView;
    private TagAdapter tagAdapter;
    private ThumbnailAdapter thumbnailAdapter;
    private TextView prevPhotoButton;
    private TextView nextPhotoButton;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        backButton = findViewById(R.id.backButton);
        mainPhoto = findViewById(R.id.mainPhoto);
        photoThumbnailRecyclerView = findViewById(R.id.photoThumbnailRecyclerView);
        addTagButton = findViewById(R.id.addTagButton);
        tagRecyclerView = findViewById(R.id.tagRecyclerView);
        prevPhotoButton = findViewById(R.id.prevPhotoButton);
        nextPhotoButton = findViewById(R.id.nextPhotoButton);

        albums = StorageUtil.loadAlbums(this);

        albumIndex = getIntent().getIntExtra(EXTRA_ALBUM_INDEX, -1);
        photoIndex = getIntent().getIntExtra(EXTRA_PHOTO_INDEX, -1);

        if (albumIndex < 0 || albumIndex >= albums.size()) {
            Toast.makeText(this, "Invalid album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        album = albums.get(albumIndex);

        if (photoIndex < 0 || photoIndex >= album.getPhotos().size()) {
            Toast.makeText(this, "Invalid photo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
        setupThumbnailRecyclerView();
        setupTagRecyclerView();
        displayCurrentPhoto();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        addTagButton.setOnClickListener(v -> showAddTagDialog());
        prevPhotoButton.setOnClickListener(v -> showPreviousPhoto());
        nextPhotoButton.setOnClickListener(v -> showNextPhoto());

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(
                    MotionEvent e1,
                    @NonNull MotionEvent e2,
                    float velocityX,
                    float velocityY
            ) {
                if (e1 == null) {
                    return false;
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_DISTANCE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                    if (diffX > 0) {
                        showPreviousPhoto();
                    } else {
                        showNextPhoto();
                    }

                    return true;
                }

                return false;
            }
        });

        mainPhoto.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void showPreviousPhoto() {
        if (album.getPhotos().isEmpty()) {
            return;
        }

        if (photoIndex > 0) {
            photoIndex--;
            displayCurrentPhoto();
            photoThumbnailRecyclerView.smoothScrollToPosition(photoIndex);
        }
    }
    private void showNextPhoto() {
        if (album.getPhotos().isEmpty()) {
            return;
        }

        if (photoIndex < album.getPhotos().size() - 1) {
            photoIndex++;
            displayCurrentPhoto();
            photoThumbnailRecyclerView.smoothScrollToPosition(photoIndex);
        }
    }

    private void setupThumbnailRecyclerView() {
        photoThumbnailRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        thumbnailAdapter = new ThumbnailAdapter(album.getPhotos());
        photoThumbnailRecyclerView.setAdapter(thumbnailAdapter);
    }

    private void setupTagRecyclerView() {
        tagRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        Photo photo = album.getPhotos().get(photoIndex);

        tagAdapter = new TagAdapter(photo.getTags(), position -> {
            showDeleteTagDialog(position);
        });

        tagRecyclerView.setAdapter(tagAdapter);
    }

    private void refreshTagList() {
        Photo photo = album.getPhotos().get(photoIndex);

        tagAdapter = new TagAdapter(photo.getTags(), position -> {
            showDeleteTagDialog(position);
        });

        tagRecyclerView.setAdapter(tagAdapter);
    }
    private void displayCurrentPhoto() {
        Photo photo = album.getPhotos().get(photoIndex);

        mainPhoto.setImageBitmap(
                loadScaledBitmap(this, photo.getFilePath(), 1080, 1600)
        );
        refreshTagList();
        if (thumbnailAdapter != null) {
            thumbnailAdapter.notifyDataSetChanged();
        }
        updateSlideshowButtons();
    }

    private void updateSlideshowButtons() {
        prevPhotoButton.setAlpha(photoIndex == 0 ? 0.35f : 1.0f);
        nextPhotoButton.setAlpha(photoIndex == album.getPhotos().size() - 1 ? 0.35f : 1.0f);
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

    private void showAddTagDialog() {
        Spinner typeSpinner = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"person", "location"}
        );
        typeSpinner.setAdapter(spinnerAdapter);

        EditText valueInput = new EditText(this);
        valueInput.setHint("Tag value");
        valueInput.setSingleLine(true);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(typeSpinner);
        layout.addView(valueInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Tag")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String type = typeSpinner.getSelectedItem().toString();
                    String value = valueInput.getText().toString().trim();

                    if (value.isEmpty()) {
                        Toast.makeText(this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Photo photo = album.getPhotos().get(photoIndex);
                    Tag tag = new Tag(type, value);

                    if (photo.addTag(tag)) {
                        StorageUtil.saveAlbums(this, albums);
                        refreshTagList();
                        Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Tag already exists", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteTagDialog(int position) {
        Photo photo = album.getPhotos().get(photoIndex);
        Tag tag = photo.getTags().get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Tag")
                .setMessage("Delete tag \"" + tag.toString() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    photo.removeTag(tag);
                    StorageUtil.saveAlbums(this, albums);
                    refreshTagList();
                    Toast.makeText(this, "Tag deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

        private final List<Photo> photos;

        public ThumbnailAdapter(List<Photo> photos) {
            this.photos = photos;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView thumbnail;

            ViewHolder(ImageView view) {
                super(view);
                thumbnail = view;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());

            imageView.setLayoutParams(new ViewGroup.LayoutParams(140, 140));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(6, 6, 6, 6);

            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Photo photo = photos.get(position);
            holder.thumbnail.setImageBitmap(
                    loadScaledBitmap(PhotoDisplayActivity.this, photo.getFilePath(), 300, 300)
            );

            holder.itemView.setAlpha(position == photoIndex ? 1.0f : 0.55f);

            holder.itemView.setOnClickListener(v -> {
                photoIndex = holder.getBindingAdapterPosition();
                if (photoIndex != RecyclerView.NO_POSITION) {
                    displayCurrentPhoto();
                }
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }
    }
}