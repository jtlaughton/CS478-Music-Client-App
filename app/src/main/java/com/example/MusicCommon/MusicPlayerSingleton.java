package com.example.MusicCommon;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

// Need a singleton so the 2 activities can only play to one audio output
public class MusicPlayerSingleton {
    // singleton
    private static MusicPlayerSingleton single_instance = null;

    // mediaplayer and tracking data
    private MediaPlayer mediaPlayer;
    private boolean playerPlaying = false;

    private MusicPlayerSingleton(){
        // create a new mediaPlayer and set the audio type
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        // when finished preparing start the music and update tracking data
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                playerPlaying = true;
                mp.start();

            }
        });
    }

    // return the instance of the singleton
    public static MusicPlayerSingleton getInstance(){
        if(single_instance == null)
            single_instance = new MusicPlayerSingleton();

        return single_instance;
    }

    // play the music using a url
    public void playByURL(String url){
        // stop if already playing
        if(playerPlaying){
            mediaPlayer.stop();
            playerPlaying = false;
        }

        // start music from url
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        }
        catch(Exception e){
            Log.i("CLIENT_INFO", "Sorry music not found");
            return;
        }

        //mediaPlayer.start();

    }
}
