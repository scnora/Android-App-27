package com.photos.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputFilter;
import android.content.Intent;

import com.photos.R;
import com.photos.util.StorageUtil;
import com.photos.model.Album;

public class HomeActivity extends AppCompatActivity {

    // UI components
    private TextView addButton;
    private TextView searchButton;
    private TextView menuButton;
    private RecyclerView albumsRecyclerView;

    private ArrayList<Album> albums;
    private AlbumAdapter albumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        initViews();

        setupRecyclerView();

        setupListeners();
    }

    private void initViews() {
        addButton = findViewById(R.id.addButton);
        searchButton = findViewById(R.id.searchButton);
        menuButton = findViewById(R.id.menuButton);
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView);
    }

    private void setupRecyclerView() {
        albums = StorageUtil.loadAlbums(this);

        albumAdapter = new AlbumAdapter(albums, new AlbumAdapter.OnAlbumClickListener() {
            @Override
            public void onAlbumClick(int position) {
                // TODO: Implement opening albums and such.
                // TODO: Implement opening photos as well, along with the necessary features.
                //Toast.makeText(HomeActivity.this,
                //"Open " + albums.get(position).getName(),
                        //Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, AlbumActivity.class);
                intent.putExtra(AlbumActivity.EXTRA_ALBUM_INDEX, position);
                startActivity(intent);
            }

            @Override
            public void onAlbumLongClick(int position) {
                Intent intent = new Intent(HomeActivity.this, AlbumActivity.class);
                intent.putExtra(AlbumActivity.EXTRA_ALBUM_INDEX, position);
                startActivity(intent);

            }
        });

        albumsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        albumsRecyclerView.setAdapter(albumAdapter);
    }

    private boolean albumExists(String name) {
        for (Album album : albums) {
            if(album.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    private boolean albumExistsExcept(String name, int position) {
        for(int i = 0; i < albums.size(); i++) {
            if (i != position && albums.get(i).getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void showAddAlbumDialog() {
        EditText input = new EditText(this);
        input.setHint("Album name");

        input.setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(20)
        });
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Create Album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String albumName = input.getText().toString().trim();
                    albumName = albumName.replace("\n", " ").replace("\r", " ");
                    if (albumName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (albumExists(albumName)) {
                        Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    albums.add(new Album(albumName));
                    albumAdapter.notifyItemInserted(albums.size() - 1);
                    StorageUtil.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRenameAlbumDialog(int position) {
        EditText input = new EditText(this);
        input.setText(albums.get(position).getName());
        input.setSingleLine(true);
        input.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(20)
        });

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    newName = newName.replace("\n", "").replace("\r", "");
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (albumExistsExcept(newName, position)) {
                        Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    albums.get(position).setName(newName);
                    albumAdapter.notifyItemChanged(position);
                    StorageUtil.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAlbumDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Delete \"" + albums.get(position) + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    albums.remove(position);
                    albumAdapter.notifyItemRemoved(position);
                    StorageUtil.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showAlbumOptionsDialog(int position) {
        String[] options = {"Rename", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle(albums.get(position).getName())
                .setItems(options, (dialog, which) -> {
                    if(which == 0) showRenameAlbumDialog(position);
                    else if(which == 1) showDeleteAlbumDialog(position);
                })
                .show();
    }

    private String[] getAlbumNamesArray() {
        String[] names = new String[albums.size()];

        for(int i = 0; i < albums.size(); i++) {
            names[i] = albums.get(i).getName();
        }
        return names;
    }
    private void showChooseAlbumToRenameDialog() {
        if(albums.isEmpty()) {
            Toast.makeText(this, "No albums to rename", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] albumNames = getAlbumNamesArray();

        new AlertDialog.Builder(this)
                .setTitle("Choose Album to Rename")
                .setItems(albumNames, (dialog, which) -> {
                    showRenameAlbumDialog(which);
                })
                .show();
    }
    private void showChooseAlbumToDeleteDialog() {
        if(albums.isEmpty()) {
            Toast.makeText(this, "No albums to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] albumNames = getAlbumNamesArray();

        new AlertDialog.Builder(this)
                .setTitle("Choose Album to Delete")
                .setItems(albumNames, (dialog, which) -> {
                    showDeleteAlbumDialog(which);
                })
                .show();
    }
    private void showMainMenuDialog() {
        String[] options = {"Rename Album", "Delete Album", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Album Options")
                .setItems(options, (dialog, which) -> {
                    if(which == 0) showChooseAlbumToRenameDialog();
                    else if(which == 1) showChooseAlbumToDeleteDialog();
                    else dialog.dismiss();
                })
                .show();
    }
    private void setupListeners() {

        addButton.setOnClickListener(v -> showAddAlbumDialog());

        searchButton.setOnClickListener(v -> {
            // TODO: Search using tag-value pairs. Matches should Auto-complete.
            // TODO: might have to use a new .xml page for this
        });

        menuButton.setOnClickListener(v -> showMainMenuDialog());
    }
}