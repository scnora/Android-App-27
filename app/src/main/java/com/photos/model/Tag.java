
package com.photos.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable{

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    public Tag(String name, String value){
        this.name = name.trim().toLowerCase();
        this.value = value.trim();
    }

    public String getName(){return name;}

    public String getValue(){return value;}


    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof Tag)) {return false;}
        Tag t = (Tag) o;
        return name.equalsIgnoreCase(t.name) && value.equalsIgnoreCase(t.value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name.toLowerCase(), value.toLowerCase());
    }

    @Override
    @NonNull
    public String toString(){
        return name + "=" + value;
    }
}