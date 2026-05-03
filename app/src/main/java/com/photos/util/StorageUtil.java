package com.photos.util;

import android.content.Context;

import com.photos.model.Album;

import java.io.*;
import java.util.ArrayList;

public class StorageUtil {
    private static final String FILE_NAME = "albums.dat";

    public static void saveAlbums(Context context, ArrayList<Album> albums) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(albums);

            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Album> loadAlbums(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);

            ArrayList<Album> albums = (ArrayList<Album>) ois.readObject();

            ois.close();
            fis.close();

            return albums;
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
