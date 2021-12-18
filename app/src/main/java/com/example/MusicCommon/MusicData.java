package com.example.MusicCommon;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MusicData{
    public String title;
    public String artist;
    public String url;
    public Bitmap image;
    public int position;

    public MusicData(){
        // Do Nothing
    }

    public MusicData(String title, String artist, String url, Bitmap image, int position){
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.image = image;
        this.position = position;
    }
}
