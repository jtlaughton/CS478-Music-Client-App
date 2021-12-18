package com.example.MusicCommon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<MusicData> songList; //data: the songs displayed
    private Context context;
    private RVListener listener;

    /*
    passing in the data and the listener defined in the main activity
     */
    public MyAdapter(ArrayList<MusicData> theList, RVListener listener){
        songList = theList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create view holder for list items
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View listView = inflater.inflate(R.layout.rv_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listView, listener);

        return viewHolder;
    }

    // populate with data from songList
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(songList.get(position).title);
        holder.artist.setText(songList.get(position).artist);
        holder.image.setImageBitmap(songList.get(position).image);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    /*
        This class creates a wrapper object around a view that contains the layout for
         an individual item in the list. It also implements the onClickListener so each ViewHolder in the list is clickable.
        It's onclick method will call the onClick method of the RVClickListener defined in
        the main activity.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView title;
        public TextView artist;
        public ImageView image;
        private View itemView;
        private RVListener listener;


        public ViewHolder(@NonNull View itemView, RVListener listener) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.the_title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            image = (ImageView) itemView.findViewById(R.id.imageView);
            this.itemView = itemView;
            this.listener = listener;
            /*
                don't forget to set the listener defined here to the view (list item) that was
                passed in to the constructor.
             */
            itemView.setOnClickListener(this); //set short click listener
        }

        @Override
        public void onClick(View v) {
            // get url based on position and call listener witht that url
            int position = getAdapterPosition();
            MusicData item = songList.get(position);
            listener.onClick(v, item.url);
        }
    }
}

