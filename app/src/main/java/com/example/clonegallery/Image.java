package com.example.clonegallery;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Image implements Parcelable {
    private final Uri uri;
    private final String name;
    private final int date;
    private final int size;
    private final String resolution;
    private final String path;
    private final String album;
    private final String data;
    private static ArrayList<Image> allNewImage = new ArrayList<>();
    public Image(Uri uri, String name, int date, int size, String resolution, String path,String album, String data) {
        this.uri = uri;
        this.name = name;
        this.date = date;
        this.size = size;
        this.resolution = resolution;
        this.path = path;
        this.album = album;
        this.data = data;
    }

    protected Image(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        name = in.readString();
        date = in.readInt();
        size = in.readInt();
        resolution = in.readString();
        path = in.readString();
        album = in.readString();
        data = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        Image temp = (Image)obj;
        return this.getUri().toString().equals(temp.getUri().toString());
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

    public int getSize() {
        return size;
    }

    public String getResolution() {
        return resolution;
    }

    public String getPath() {
        return path;
    }

    public String getAlbum() {
        return album;
    }

    public String getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(uri, i);
        parcel.writeString(name);
        parcel.writeInt(date);
        parcel.writeInt(size);
        parcel.writeString(resolution);
        parcel.writeString(path);
        parcel.writeString(album);
        parcel.writeString(data);
    }
    public static ArrayList<Image> getAllNewImage(){
        return allNewImage;
    }

}
