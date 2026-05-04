package com.photos.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.photos.R;
import com.photos.model.Album;
import com.photos.model.Photo;
import com.photos.model.Tag;
import com.photos.util.StorageUtil;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView searchInput, searchInput2;
    private Spinner tagTypeSpinner, tagTypeSpinner2;
    private RadioGroup conjunctionGroup;
    private TextView resultsCountText;
    private RecyclerView searchResultsRecyclerView;
    private ArrayList<Album> albums;
    private PhotoAdapter resultsAdapter;
    private final List<Photo> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        albums = StorageUtil.loadAlbums(this);

        searchInput= findViewById(R.id.searchInput);
        searchInput2 = findViewById(R.id.searchInput2);
        tagTypeSpinner = findViewById(R.id.tagTypeSpinner);
        tagTypeSpinner2= findViewById(R.id.tagTypeSpinner2);
        conjunctionGroup = findViewById(R.id.conjunctionGroup);
        resultsCountText = findViewById(R.id.resultsCountText);
        searchResultsRecyclerView =findViewById(R.id.searchResultsRecyclerView);

        //back btn
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        //spinners setup
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,  new String[]{"person", "location"});
        tagTypeSpinner.setAdapter(typeAdapter);
        tagTypeSpinner2.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,new String[]{"person", "location"}));

        //setup auto complete, both inputs
        setupAutoComplete(searchInput, tagTypeSpinner);
        setupAutoComplete(searchInput2, tagTypeSpinner2);

        conjunctionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAnd || checkedId == R.id.radioOr) {
                tagTypeSpinner2.setVisibility(View.VISIBLE);
                searchInput2.setVisibility(View.VISIBLE);

            } else {
                tagTypeSpinner2.setVisibility(View.GONE);
                searchInput2.setVisibility(View.GONE);

            }
        });
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        resultsAdapter = new PhotoAdapter(searchResults, new PhotoAdapter.OnPhotoClickListener() {
            @Override public void onPhotoClick(int position) {}
            @Override public void onPhotoLongClick(int position) {}
        });
        searchResultsRecyclerView.setAdapter(resultsAdapter);

        findViewById(R.id.searchButton).setOnClickListener(v -> performSearch(searchInput.getText().toString()));

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
    private void setupAutoComplete(AutoCompleteTextView input, Spinner spinner) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                refreshSuggestions(input, spinner);
            }
        });
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                refreshSuggestions(input, spinner);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    private void refreshSuggestions(AutoCompleteTextView input, Spinner spinner){
        String selectedType = spinner.getSelectedItem().toString();
        List<String> suggestions = getTagSuggestions(selectedType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line, suggestions);
        input.setAdapter(adapter);
    }
    private List<String> getTagSuggestions(String tagType) {
        List<String> suggestions = new ArrayList<>();
        for (Album album : albums){
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getName().equalsIgnoreCase(tagType)) {
                        String val = tag.getValue();
                        boolean exists = false;
                        for (String s : suggestions) {
                            if (s.equalsIgnoreCase(val)) { exists = true; break; }
                        }
                        if (!exists){
                            suggestions.add(val);
                        }
                    }
                }
            }
        }
        return suggestions;
    }
    private void performSearch(String query){
        //lmk if this all makes sense
        if (query.trim().isEmpty()) {
            searchResults.clear();
            resultsAdapter.notifyDataSetChanged();
            resultsCountText.setText("");
            return;
        }

        String type1 = tagTypeSpinner.getSelectedItem().toString();
        int checkedId= conjunctionGroup.getCheckedRadioButtonId();
        boolean isAnd = (checkedId ==R.id.radioAnd);
        boolean isOr= (checkedId ==R.id.radioOr);
        String type2= tagTypeSpinner2.getSelectedItem().toString();
        String val2 = searchInput2.getText().toString().trim();
        searchResults.clear();

        for (Album album : albums){
            for (Photo photo : album.getPhotos()) {
                boolean match1 = photoMatchesTag(photo, type1, query.trim());
                boolean match2 = (isAnd|| isOr) && photoMatchesTag(photo, type2, val2);
                boolean include;
                if(isAnd){
                    include =(match1 && match2);
                }
                else if(isOr) {
                    include =(match1||match2);
                }
                else{
                    include = match1;
                }

                if (include && !searchResults.contains(photo)) {
                    searchResults.add(photo);
                }
            }
        }
        resultsAdapter.notifyDataSetChanged();
        resultsCountText.setText(searchResults.size() + " result(s) found");
    }
    private boolean photoMatchesTag(Photo photo, String type, String value) {
        if (value.isEmpty()) return false;
        for (Tag tag : photo.getTags()){
            if (tag.getName().equalsIgnoreCase(type) &&
                    tag.getValue().toLowerCase().startsWith(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}