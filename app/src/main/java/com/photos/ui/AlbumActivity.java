package com.photos.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;

import com.photos.R;
import com.photos.model.Album;
import com.photos.model.Photo;
import com.photos.util.StorageUtil;

import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_INDEX = "album_index";

    private ArrayList<Album> albums;
    private int albumIndex;
    private Album album;
    private PhotoAdapter photoAdapter;

    private TextView backButton;
    private TextView addButton;
    private TextView menuButton;
    private TextView albumNameText;
    private RecyclerView photoGridRecyclerView;

    private ActivityResultLauncher<Intent> pickPhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        albums = StorageUtil.loadAlbums(this);
        albumIndex = getIntent().getIntExtra(EXTRA_ALBUM_INDEX, -1);

        if (albumIndex < 0 || albumIndex >= albums.size()) {
            Toast.makeText(this, "Invalid album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        album = albums.get(albumIndex);

        initViews();
        setupPhotoPicker();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        addButton = findViewById(R.id.addButton);
        menuButton = findViewById(R.id.menuButton);
        albumNameText = findViewById(R.id.albumNameText);
        photoGridRecyclerView = findViewById(R.id.photoGridRecyclerView);

        albumNameText.setText(album.getName());
    }

    private void setupRecyclerView() {
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

        photoGridRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photoGridRecyclerView.setAdapter(photoAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        addButton.setOnClickListener(v -> openPhotoPicker());

        menuButton.setOnClickListener(v -> showAlbumMenuDialog());
    }

    private void setupPhotoPicker() {
        pickPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }

                    Uri uri = result.getData().getData();

                    if (uri == null) {
                        return;
                    }

                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }

                    Photo photo = new Photo(uri.toString());

                    if (album.addPhoto(photo)) {
                        StorageUtil.saveAlbums(this, albums);
                        photoAdapter.notifyItemInserted(album.getPhotos().size() - 1);
                    } else {
                        Toast.makeText(this, "Photo already in album", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickPhotoLauncher.launch(intent);
    }

    private void openPhotoDisplay(int position) {
        Intent intent = new Intent(this, PhotoDisplayActivity.class);
        intent.putExtra(PhotoDisplayActivity.EXTRA_ALBUM_INDEX, albumIndex);
        intent.putExtra(PhotoDisplayActivity.EXTRA_PHOTO_INDEX, position);
        startActivity(intent);
    }

    private void showAlbumMenuDialog() {
        String[] options = {"Rename Album", "Delete Album", "Add Photo"};

        new AlertDialog.Builder(this)
                .setTitle(album.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameAlbumDialog();
                    } else if (which == 1) {
                        showDeleteAlbumDialog();
                    } else if (which == 2) {
                        openPhotoPicker();
                    }
                })
                .show();
    }

    private void showRenameAlbumDialog() {
        final EditText input = new EditText(this);
        input.setText(album.getName());
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (int i = 0; i < albums.size(); i++) {
                        if (i != albumIndex &&
                                albums.get(i).getName().equalsIgnoreCase(newName)) {
                            Toast.makeText(this, "Album name already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    album.setName(newName);
                    albumNameText.setText(newName);
                    StorageUtil.saveAlbums(this, albums);
                    setResult(RESULT_OK);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAlbumDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Delete this album?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    albums.remove(albumIndex);
                    StorageUtil.saveAlbums(this, albums);
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showPhotoOptionsDialog(int position) {
        String[] options = {"View", "Move to Album", "Remove"};

        new AlertDialog.Builder(this)
                .setTitle("Photo Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openPhotoDisplay(position);
                    } else if (which == 1) {
                        showMovePhotoDialog(position);
                    } else if (which == 2) {
                        removePhoto(position);
                    }
                })
                .show();
    }

    private void removePhoto(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Remove this photo from the album?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    album.getPhotos().remove(position);
                    StorageUtil.saveAlbums(this, albums);
                    photoAdapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMovePhotoDialog(int position) {
        ArrayList<String> otherAlbumNames = new ArrayList<>();
        ArrayList<Integer> otherAlbumIndices = new ArrayList<>();

        for (int i = 0; i < albums.size(); i++) {
            if (i != albumIndex) {
                otherAlbumNames.add(albums.get(i).getName());
                otherAlbumIndices.add(i);
            }
        }

        if (otherAlbumNames.isEmpty()) {
            Toast.makeText(this, "No other albums to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = otherAlbumNames.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Move to Album")
                .setItems(names, (dialog, which) -> {
                    int targetAlbumIndex = otherAlbumIndices.get(which);
                    Photo photo = album.getPhotos().get(position);

                    if (albums.get(targetAlbumIndex).addPhoto(photo)) {
                        album.getPhotos().remove(position);
                        StorageUtil.saveAlbums(this, albums);
                        photoAdapter.notifyItemRemoved(position);

                        Toast.makeText(
                                this,
                                "Photo moved to " + albums.get(targetAlbumIndex).getName(),
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(this, "Photo already in that album", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        albums = StorageUtil.loadAlbums(this);

        if (albumIndex < 0 || albumIndex >= albums.size()) {
            finish();
            return;
        }

        album = albums.get(albumIndex);
        albumNameText.setText(album.getName());

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

        photoGridRecyclerView.setAdapter(photoAdapter);
    }
}