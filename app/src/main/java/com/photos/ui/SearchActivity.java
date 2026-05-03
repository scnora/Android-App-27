package com.photos.ui;

import android.os.Bundle;
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

public class SearchActivity extends AppCompatActivity{
    private Spinner tagTypeSpinner1, tagTypeSpinner2;
    private AutoCompleteTextView tagInput1, tagInput2;
    private RadioGroup group;
    private TextView resultsCountText;
    public RecyclerView searchResultsRecyclerView;
    private ArrayList<Album> albums;
    private PhotoAdapter resultsAdapter;
    final List<Photo> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        albums = StorageUtil.loadAlbums(this);

        tagTypeSpinner1 = findViewById(R.id.tagTypeSpinner1);
        tagTypeSpinner2 = findViewById(R.id.tagTypeSpinner2);
        tagInput1 = findViewById(R.id.tagValueInput1);
        tagInput2 = findViewById(R.id.tagValueInput2);
        group = findViewById(R.id.conjunctionGroup);
        resultsCountText = findViewById(R.id.resultsCountText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        //back
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        //spinners setup
        ArrayAdapter<String> tagTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,  new String[]{"person", "location"});
        tagTypeSpinner1.setAdapter(tagTypeAdapter);
        tagTypeSpinner2.setAdapter(tagTypeAdapter);

        setupAutoComplete(tagInput1, tagTypeSpinner1);
        setupAutoComplete(tagInput2, tagTypeSpinner2);

        //show/hide
        group.setOnCheckedChangeListener((group, checkedId) -> {
            boolean showSecond = (checkedId == R.id.radioAnd || checkedId == R.id.radioOr);
            tagTypeSpinner2.setVisibility(showSecond ? View.VISIBLE : View.GONE);
            tagInput2.setVisibility(showSecond ? View.VISIBLE : View.GONE);
        });

        //search results
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        resultsAdapter = new PhotoAdapter(searchResults, new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(int position) {
                //view photo
            }
            @Override
            public void onPhotoLongClick(int position){}
        });
        searchResultsRecyclerView.setAdapter(resultsAdapter);

        //search button
        findViewById(R.id.searchButton).setOnClickListener(v -> runSearch());
    }

    private void setupAutoComplete(AutoCompleteTextView input, Spinner spinner) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String selectedType = spinner.getSelectedItem().toString();
                List<String> suggestions = getTagSuggestions(selectedType);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
                input.setAdapter(adapter);
            }
        });
    }

    private List<String> getTagSuggestions(String tagType) {
        List<String> suggestions = new ArrayList<>();
        for (Album album: albums){
            for (Photo photo:album.getPhotos()){
                for (Tag tag :photo.getTags()){
                    if (tag.getName().equalsIgnoreCase(tagType)) {
                        String val = tag.getValue();
                        boolean alreadyAdded = false;
                        for (String s : suggestions) {
                            if (s.equalsIgnoreCase(val)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if(!alreadyAdded){
                            suggestions.add(val);
                        }
                    }
                }
            }
        }
        return suggestions;
    }

    private void runSearch() {
        String type1 = tagTypeSpinner1.getSelectedItem().toString();
        String val1 = tagInput1.getText().toString().trim();

        int checkedId = group.getCheckedRadioButtonId();
        boolean isAnd = (checkedId == R.id.radioAnd);
        boolean isOr = (checkedId == R.id.radioOr);

        String type2 = tagTypeSpinner2.getSelectedItem().toString();
        String val2 = tagInput2.getText().toString().trim();

        if (val1.isEmpty()) {
            resultsCountText.setText("Enter a tag value to search");
            return;
        }

        searchResults.clear();

        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                boolean match1 = photoMatchesTag(photo, type1, val1);
                boolean include;
                if(isAnd){
                    include = match1 && photoMatchesTag(photo, type2, val2);
                }
                else if (isOr){
                    include = match1 || photoMatchesTag(photo, type2, val2);
                }
                else {
                    include = match1;
                }
                if(include && !searchResults.contains(photo)) {
                    searchResults.add(photo);
                }
            }
        }

        resultsAdapter.notifyDataSetChanged();
        resultsCountText.setText(searchResults.size() + " result(s) found");
    }

    private boolean photoMatchesTag(Photo photo, String type, String value) {
        for (Tag tag : photo.getTags()) {
            if (tag.getName().equalsIgnoreCase(type)&& tag.getValue().toLowerCase().startsWith(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}