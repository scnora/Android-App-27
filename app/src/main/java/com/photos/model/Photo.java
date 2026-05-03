
package com.photos.model;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String filePath;
    private String caption;
    private final Calendar dateTaken;
    private final List<Tag> tags;

    public Photo(String filePath) {
        this.filePath = filePath;
        this.caption = "";

        File file = new File(filePath);
        this.dateTaken = Calendar.getInstance();
        this.dateTaken.setTimeInMillis(file.lastModified());
        this.dateTaken.set(Calendar.MILLISECOND, 0);

        this.tags = new ArrayList<>();
    }

    public String getFilePath() {return filePath;}

    public String getCaption() {return caption;}

    public void setCaption(String caption) {this.caption = caption;}

    public Calendar getDateTaken() {return dateTaken;}

    public List<Tag> getTags() {return tags;}

    public boolean addTag(Tag tag) {
        if(tags.contains(tag)) return false;
        tags.add(tag);
        return true;
    }

    public boolean removeTag(Tag tag) {return tags.remove(tag);}

    public boolean hasTag(String name, String value) {
        return tags.contains(new Tag(name, value));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;

        if(!(o instanceof Photo)) return false;
        Photo p = (Photo)o;
        return filePath.equals(p.filePath);
    }

    @Override
    public int hashCode() {return filePath.hashCode();}

    @Override
    @NonNull
    public String toString() {
        return caption.isEmpty() ? filePath : caption;
    }

}
