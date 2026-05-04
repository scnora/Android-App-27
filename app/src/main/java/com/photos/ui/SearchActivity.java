package com.photos.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.photos.R;

public class SearchActivity extends AppCompatActivity {
    public static final String EXTRA_TAG_TYPE = "tag_type";
    private EditText searchInput;
    private RecyclerView searchResultsRecyclerView;
    private String tagType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        tagType = getIntent().getStringExtra(EXTRA_TAG_TYPE);

        if (tagType == null) {
            tagType = "location";
        }

        searchInput = findViewById(R.id.searchInput);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        if (tagType.equals("location")) {
            searchInput.setHint("Search by location...");
        } else if (tagType.equals("person")) {
            searchInput.setHint("Search by people...");
        }
        // back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // recycler view setup
        searchResultsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // TODO: set adapter later

        // search listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        // TODO:
        // - search through albums/photos/tags
        // - update RecyclerView adapter
    }
}