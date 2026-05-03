package com.photos.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.photos.R;
import com.photos.model.Album;
import com.photos.model.Photo;
import com.photos.util.StorageUtil;

import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {
    //open album

    public static final String EXTRA_ALBUM_INDEX = "album_index";

    private ArrayList<Album> albums;
    private int albumIndex;
    private Album album;
    private PhotoAdapter photoAdapter;

    private ActivityResultLauncher<Intent> pickPhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        albums = StorageUtil.loadAlbums(this);
        albumIndex = getIntent().getIntExtra(EXTRA_ALBUM_INDEX, 0);
        album = albums.get(albumIndex);

        TextView titleText = findViewById(R.id.albumTitleText);
        titleText.setText(album.getName());

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Add photo button
        findViewById(R.id.addPhotoButton).setOnClickListener(v -> openPhotoPicker());

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.photosRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        photoAdapter = new PhotoAdapter(album.getPhotos(), new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(int position) {
                openPhotoDisplay(position);
            }

            @Override
            public void onPhotoLongClick(int position) {
                showPhotoOptionsDialog(position);
            }
        });
        recyclerView.setAdapter(photoAdapter);

        // Photo picker launcher
        pickPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Persist permission
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            String path = uri.toString();
                            Photo photo = new Photo(path);
                            if (album.addPhoto(photo)) {
                                photoAdapter.notifyItemInserted(album.getPhotos().size() - 1);
                                StorageUtil.saveAlbums(this, albums);
                            } else {
                                Toast.makeText(this, "Photo already in album", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickPhotoLauncher.launch(intent);
    }

    private void openPhotoDisplay(int position) {
        Intent intent = new Intent(this, PhotoDisplayActivity.class);
        intent.putExtra(PhotoDisplayActivity.EXTRA_ALBUM_INDEX, albumIndex);
        intent.putExtra(PhotoDisplayActivity.EXTRA_PHOTO_INDEX, position);
        startActivity(intent);
    }

    private void showPhotoOptionsDialog(int position) {
        String[] options = {"View", "Move to Album", "Remove"};
        new AlertDialog.Builder(this)
                .setTitle("Photo Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openPhotoDisplay(position);
                    else if (which == 1) showMovePhotoDialog(position);
                    else if (which == 2) removePhoto(position);
                })
                .show();
    }

    private void removePhoto(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Remove this photo from the album?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    album.getPhotos().remove(position);
                    photoAdapter.notifyItemRemoved(position);
                    StorageUtil.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMovePhotoDialog(int position) {
        // Build list of other albums
        ArrayList<String> otherNames = new ArrayList<>();
        ArrayList<Integer> otherIndices = new ArrayList<>();

        for (int i = 0; i < albums.size(); i++) {
            if (i != albumIndex) {
                otherNames.add(albums.get(i).getName());
                otherIndices.add(i);
            }
        }

        if (otherNames.isEmpty()) {
            Toast.makeText(this, "No other albums to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] namesArray = otherNames.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Move to Album")
                .setItems(namesArray, (dialog, which) -> {
                    int targetIndex = otherIndices.get(which);
                    Photo photo = album.getPhotos().get(position);

                    if (albums.get(targetIndex).addPhoto(photo)) {
                        album.getPhotos().remove(position);
                        photoAdapter.notifyItemRemoved(position);
                        StorageUtil.saveAlbums(this, albums);
                        Toast.makeText(this, "Photo moved to " +
                                albums.get(targetIndex).getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Photo already in that album",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh in case tags were added in PhotoDisplayActivity
        albums = StorageUtil.loadAlbums(this);
        album = albums.get(albumIndex);
        photoAdapter.notifyDataSetChanged();
    }
}