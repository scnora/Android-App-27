
package com.photos.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Album implements Serializable{

    private static final long serialVersionUID = 1L;

    private String name;
    private final List<Photo> photos;

    public Album(String name){
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public List<Photo> getPhotos(){
        return photos;
    }

    public int getPhotoCount(){
        return photos.size();
    }

    public boolean addPhoto(Photo photo){
        //add photo to alb
        if (photos.contains(photo)){
            return false;
        }
        photos.add(photo);
        return true;
    }

    public boolean removePhoto(Photo photo){
        return photos.remove(photo);
    }

    public Calendar getEarliestDay(){
        return photos.stream()
                .map(Photo::getDateTaken)
                .min(Calendar::compareTo)
                .orElse(null);
    }

    public Calendar getLatestDay(){
        return photos.stream()
                .map(Photo::getDateTaken)
                .max(Calendar::compareTo)
                .orElse(null);
    }

    @Override
    @NonNull
    public String toString(){
        return name + " (" + photos.size() + " photos)";
    }
}