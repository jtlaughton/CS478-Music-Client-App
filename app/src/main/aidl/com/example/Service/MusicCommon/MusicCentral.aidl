// MusicCentral.aidl
package com.example.Service.MusicCommon;

// Declare any non-default types here with import statements

interface MusicCentral {
    Bundle[] getAllInfo();
    Bundle getInfo(int position);
    String getUrl(int position);
}