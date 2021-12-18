package com.example.MusicCommon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Service.MusicCommon.MusicCentral;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    protected static final int PERMISSION_REQUEST = 0;

    RecyclerView songView; // the recycler view that holds the songs
    ArrayList<MusicData> list = new ArrayList<>();

    private MusicCentral mMusicCentralService;
    private boolean mIsBound = false;
    private Bundle action;

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            // convert to interface
            mMusicCentralService = MusicCentral.Stub.asInterface(iservice);
            mIsBound = true;

            // get the action from the main activity
            Intent intent = getIntent();
            action = intent.getBundleExtra("THE_ACTION");

            // update values based on that
            updateBasedOnAction(action);

            // play music from singleton based on url
            RVListener listener = (view, url)->{
                MusicPlayerSingleton.getInstance().playByURL(url);
            };

            // create an instance of MyAdapter with the required data
            MyAdapter mAdapter = new MyAdapter(list, listener);

            songView.setHasFixedSize(true);
            songView.setAdapter(mAdapter); // set adapter to my adapter
        }

        public void onServiceDisconnected(ComponentName className) {

            Log.i("CLIENT_INFO", "Disconnect");

            mMusicCentralService = null;
            mIsBound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get recycler view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        songView = (RecyclerView) findViewById(R.id.recycler_view);    // create recycler view

        // check for permissions
        if (checkSelfPermission("com.example.Service.MusicCentralService.MUSIC")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"com.example.Service.MusicCentralService.MUSIC"},
                    PERMISSION_REQUEST);
        }
        // if permitted start the service
        else {
            startTheService();
        }

        // set to linear layout manager
        songView.setLayoutManager(new LinearLayoutManager(this));
    }

    // starts the service
    public void startTheService(){
        // explicit intent to the service
        Intent i = new Intent(MusicCentral.class.getName());

        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        // start in foreground
        startForegroundService(i);

        // if unbound then bind
        if(!mIsBound)
            bindTheService();
    }

    // change what happens on permission PERMISSION_REQUEST
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted, go ahead and start the service

                    startTheService();
                }
                else {
                    Toast.makeText(this, "BUMMER: No Permission :-(", Toast.LENGTH_LONG).show() ;
                }
            }
            default: {
                // do nothing
            }
        }
    }

    // Make sure we still have permission
    @Override
    protected void onStart() {
        super.onStart();
        if (checkSelfPermission("com.example.Service.MusicCentralService.MUSIC")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"com.example.Service.MusicCentralService.MUSIC"},
                    PERMISSION_REQUEST);
        }
    }

    // binds to the MusicCentral service
    public void bindTheService(){
        // explicit intent to service
        Intent i = new Intent(MusicCentral.class.getName());

        boolean b = false;

        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        // bind to service
        b = bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        if (b) {
            Log.i("CLIENT_LOG", "Ugo says bindService() succeeded!");
        } else {
            Log.i("CLIENT_LOG", "Ugo says bindService() failed!");
        }
    }

    // unbind and stop service if destroyed
    @Override
    public void onDestroy(){
        super.onDestroy();

        // if bound unbind
        if(mIsBound)
            unbindService(mConnection);

        // explicit intent to service
        Intent i = new Intent(MusicCentral.class.getName());

        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        // sto the service
        stopService(i);
    }

    // based on the specified action from the intent do something
    public void updateBasedOnAction(Bundle action){
        String type = action.getString("TYPE");
        int position = action.getInt("POSITION");

        switch(type){
            // if show all then get information for all songs
            case "SHOW_ALL":
                Bundle[] songs;

                try {
                    songs = mMusicCentralService.getAllInfo();
                } catch (RemoteException e) {
                    Log.e("CLIENT_INFO", e.toString());
                    return;
                }

                // make a list of all the music data
                for(int i = 0; i < songs.length; i++){
                    Bundle current = songs[i];

                    String title = current.getString("TITLE");
                    String artist = current.getString("ARTIST");
                    String url = current.getString("URL");
                    Bitmap image = (Bitmap) current.getParcelable("BITMAP");

                    MusicData m = new MusicData(title, artist, url, image, i);

                    list.add(m);
                }

                return;
            // if show one get information for just one song
            case "SHOW_ONE":
                Bundle song;

                try {
                    song = mMusicCentralService.getInfo(position);
                } catch (RemoteException e) {
                    Log.e("CLIENT_INFO", e.toString());
                    return;
                }

                // add one item to list
                String title = song.getString("TITLE");
                String artist = song.getString("ARTIST");
                String url = song.getString("URL");
                Bitmap image = (Bitmap) song.getParcelable("BITMAP");

                MusicData m = new MusicData(title, artist, url, image, position);

                list.add(m);

                return;
        }
    }
}
