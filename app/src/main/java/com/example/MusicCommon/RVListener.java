package com.example.MusicCommon;

import android.view.View;

// allows us to make calls in MainActivity from MyAdapter
public interface RVListener {

    public void onClick(View view, String url);
}
