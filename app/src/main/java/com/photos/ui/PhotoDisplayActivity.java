package com.photos.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.photos.R;
import com.photos.model.Album;
import com.photos.model.Photo;
import com.photos.model.Tag;
import com.photos.util.StorageUtil;

import java.io.File;
import java.util.ArrayList;

public class PhotoDisplayActivity extends AppCompatActivity{
    public static final String EXTRA_ALBUM_INDEX = "album_index";
    public static final String EXTRA_PHOTO_INDEX = "photo_index";

    private ArrayList<Album> albums;
    private int albumIndex;
    private int photoIndex;
    private Album album;

    private ImageView imageView;
    private TextView textView;
    private TagAdapter tagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        albums = StorageUtil.loadAlbums(this);
        albumIndex = getIntent().getIntExtra(EXTRA_ALBUM_INDEX, 0);
        photoIndex = getIntent().getIntExtra(EXTRA_PHOTO_INDEX, 0);
        album = albums.get(albumIndex);

        imageView = findViewById(R.id.photoImageView);
        textView = findViewById(R.id.photoNameText);

        //back
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        //add tag
        findViewById(R.id.addTagButton).setOnClickListener(v -> showAddTagDialog());

        //slideshow buttons
        findViewById(R.id.prevButton).setOnClickListener(v -> {
            if(photoIndex > 0){
                photoIndex-=1;
                displayCurrentPhoto();
            }
            else {
                Toast.makeText(this, "First photo", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.nextButton).setOnClickListener(v -> {
            if (photoIndex < album.getPhotos().size()-1){
                photoIndex++;
                displayCurrentPhoto();
            }
            else{
                Toast.makeText(this, "Last photo", Toast.LENGTH_SHORT).show();
            }
        });

        //tag RecyclerView
        RecyclerView tagsRecyclerView = findViewById(R.id.tagsRecyclerView);
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Photo currentPhoto = album.getPhotos().get(photoIndex);
        tagAdapter = new TagAdapter(currentPhoto.getTags(), position -> showDeleteTagDialog(position));
        tagsRecyclerView.setAdapter(tagAdapter);

        displayCurrentPhoto();
    }

    private void displayCurrentPhoto() {
        Photo photo = album.getPhotos().get(photoIndex);
        String filePath = photo.getFilePath();
        try{
            Uri uri = Uri.parse(filePath);
            imageView.setImageURI(uri);
        } catch (Exception e) {
            File file = new File(filePath);
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file));
            }
        }
        String name = new File(filePath).getName();
        if (name.equals(filePath)) {

            name = Uri.parse(filePath).getLastPathSegment();
        }
        textView.setText(name);

        tagAdapter.notifyDataSetChanged();
    }

    private void showAddTagDialog() {
        //spinner for tag
        Spinner typeSpinner = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, new String[]{"person", "location"});
        typeSpinner.setAdapter(spinnerAdapter);
        EditText valueInput = new EditText(this);
        valueInput.setHint("Tag value");

        //layout
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

                    if(value.isEmpty()){
                        Toast.makeText(this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Photo photo = album.getPhotos().get(photoIndex);
                    Tag tag = new Tag(type, value);

                    if (photo.addTag(tag)) {
                        tagAdapter.notifyItemInserted(photo.getTags().size() - 1);
                        StorageUtil.saveAlbums(this, albums);
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
                .setMessage("Delete tag \"" + tag + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    photo.getTags().remove(position);
                    tagAdapter.notifyItemRemoved(position);
                    StorageUtil.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}