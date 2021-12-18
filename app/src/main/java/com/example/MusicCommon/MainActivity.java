package com.example.MusicCommon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Service.MusicCommon.MusicCentral;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    protected static final int PERMISSION_REQUEST = 0;

    // View items
    private Button bindButton;
    private Button unbindButton;
    private Button showAllButton;
    private Button submitButton;
    private EditText songText;
    private RecyclerView songView;

    // for tracking and binding to service
    private MusicCentral mMusicCentralService;
    private boolean mIsBound = false;
    private boolean mIsSupposedToBeBound = false;
    private boolean mIsStarted = false;
    private int itemNum = 0;

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            // convert stub to interface for easy calling
            mMusicCentralService = MusicCentral.Stub.asInterface(iservice);
            mIsBound = true;

            Bundle[] songs;

            ArrayList<String> titleList = new ArrayList<>();
            ArrayList<String> urlList = new ArrayList<>();

            // get all the songs for the main activity
            try {
                songs = mMusicCentralService.getAllInfo();
            } catch (RemoteException e) {
                Log.e("CLIENT_INFO", e.toString());
                return;
            }

            // extract data from array of bundles
            for(int i = 0; i < songs.length; i++){
                Bundle current = songs[i];

                String title = current.getString("TITLE");
                String url = current.getString("URL");

                titleList.add(title);
                urlList.add(url);
            }

            // create a listener for list items
            RVListener listener = (view, url)->{
                // use music player singleton to play songs so that playback is linked to second activity
                MusicPlayerSingleton.getInstance().playByURL(url);
            };

            // create instance of our adapter
            MyAdapterMain mAdapter = new MyAdapterMain(titleList, urlList, listener);
            itemNum = titleList.size();

            // attach adapter
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songView = (RecyclerView) findViewById(R.id.songList);

        // find all the view items
        bindButton = (Button) findViewById(R.id.bindButton);
        unbindButton = (Button) findViewById(R.id.unbindButton);
        showAllButton = (Button) findViewById(R.id.showAllButton);
        submitButton = (Button) findViewById(R.id.submitButton);
        songText = (EditText) findViewById(R.id.songInput);

        // set initial state on create
        unbindButton.setEnabled(false);
        showAllButton.setEnabled(false);
        submitButton.setEnabled(false);

        // bind the service on click
        bindButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bindTheService();
            }
        });

        // unbind the service on click, reset visual state, and update recycler view to remove items
        unbindButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // unbind
                unbindService(mConnection);

                // set visual state
                bindButton.setEnabled(true);
                unbindButton.setEnabled(false);
                showAllButton.setEnabled(false);
                submitButton.setEnabled(false);
                songText.setEnabled(false);

                // set boolean trackers
                mIsSupposedToBeBound = false;

                songView.setHasFixedSize(true);

                RVListener listener = (view, url)->{
                    return;
                };

                // update with empty lists
                songView.setAdapter(new MyAdapterMain(new ArrayList<String>(), new ArrayList<String>(), listener));
            }
        });

        // start second activity with show all as the option
        showAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListActivity("SHOW_ALL", -1);
            }
        });

        // read in data and start second activity with show one as the option
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = songText.getText().toString();
                int position;

                // if no text don't do anything
                if(txt == null){
                    songText.setHint(R.string.bad_input);
                    return;
                }
                try{
                    position = Integer.parseInt(txt);
                }
                // if not an int dont do anything
                catch (NumberFormatException e){
                    songText.setHint(R.string.bad_input);
                    return;
                }

                // if out of bounds dont do anything
                if(position > itemNum || position < 1){
                    songText.setHint(R.string.bad_input);
                    return;
                }

                // start activity
                startListActivity("SHOW_ONE", position-1);
            }
        });

        // set layout manager to linear
        songView.setLayoutManager(new LinearLayoutManager(this));

        // if we have a saved instance state, check what the state of binding was
        // if we need to bind then bind
        if(savedInstanceState != null){
            mIsSupposedToBeBound = savedInstanceState.getBoolean("SUPPOSED_TO");

            if(mIsSupposedToBeBound)
                bindTheService();
        }
    }

    // starts the second activity
    public void startListActivity(String flag, int position){
        // intent to start next activity
        Intent intent = new Intent(this, ListActivity.class);

        // data to send to next activity
        Bundle b = new Bundle();
        b.putString("TYPE", flag);
        b.putInt("POSITION", position);
        intent.putExtra("THE_ACTION", b);

        // start the activity
        startActivity(intent);
    }

    // bind to MusicCentral
    public void bindTheService(){
        // create explicit intent to MusicCentral
        Intent i = new Intent(MusicCentral.class.getName());

        boolean b = false;

        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        // bind with explicit intent
        b = bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        // if we bound then update the visual state, and trackers
        if (b) {
            Log.i("CLIENT_LOG", "Ugo says bindService() succeeded!");
            bindButton.setEnabled(false);
            unbindButton.setEnabled(true);
            showAllButton.setEnabled(true);
            submitButton.setEnabled(true);
            songText.setEnabled(true);
            mIsSupposedToBeBound = true;
        }
        // otherwise do nothing
        else {
            Log.i("CLIENT_LOG", "Ugo says bindService() failed!");
        }
    }

    // start the service in the foreground
    public void startTheService(){
        // if the service isn't already started start it
        if(!mIsStarted){
            // explicit intent to service
            Intent i = new Intent(MusicCentral.class.getName());

            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            // start in foreground
            mIsStarted = true;
            startForegroundService(i);

            // if trackers indicate we should be bound then bind
            if(mIsSupposedToBeBound){
                bindTheService();
            }
        }

    }

    // Bind to KeyGenerator Service when app starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("CLIENT_INFO", "Starting");

        // check for permissions or start the service
        if (checkSelfPermission("com.example.Service.MusicCentralService.MUSIC")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"com.example.Service.MusicCentralService.MUSIC"},
                    PERMISSION_REQUEST);
        }
        else {
            startTheService();
        }
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

    // unbind and stop service when the app stops
    @Override
    public void onStop(){
        super.onStop();

        // unbind if we are bound
        if(mIsBound && mIsSupposedToBeBound)
            unbindService(mConnection);

        // explicit intent to service
        Intent i = new Intent(MusicCentral.class.getName());

        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        // if we all clients are unbound it should stop the service
        stopService(i);
        mIsStarted = false;
        Log.i("CLIENT_INFO", "Stopping");
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        // put state trackers in outState
        outState.putBoolean("STARTED", mIsStarted);
        outState.putBoolean("BOUND", mIsBound);
        outState.putBoolean("SUPPOSED_TO", mIsSupposedToBeBound);

        super.onSaveInstanceState(outState);
    }
}