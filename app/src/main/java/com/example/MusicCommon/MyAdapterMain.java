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

public class MyAdapterMain extends RecyclerView.Adapter<MyAdapterMain.ViewHolder> {

    private ArrayList<String> titles; //data: the names displaye
    private ArrayList<String> urls;
    private Context context;
    private RVListener listener;

    /*
    passing in the data and the listener defined in the main activity
     */
    public MyAdapterMain(ArrayList<String> titleList, ArrayList<String> urls, RVListener listener){
        titles = titleList;
        this.urls = urls;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create the view holder for the list items
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View listView = inflater.inflate(R.layout.rv_item2, parent, false);
        ViewHolder viewHolder = new ViewHolder(listView, listener);

        return viewHolder;
    }

    // populate the list items
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(titles.get(position));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    /*
        This class creates a wrapper object around a view that contains the layout for
         an individual item in the list. It also implements the onClickListener so each ViewHolder in the list is clickable.
        It's onclick method will call the onClick method of the RVClickListener defined in
        the main activity.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView title;
        private View itemView;
        private RVListener listener;


        public ViewHolder(@NonNull View itemView, RVListener listener) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.the_title);
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
            String u = urls.get(position);
            listener.onClick(v, u);
        }
    }
}

