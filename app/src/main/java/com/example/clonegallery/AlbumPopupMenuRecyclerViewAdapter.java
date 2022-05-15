package com.example.clonegallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumPopupMenuRecyclerViewAdapter extends RecyclerView.Adapter<AlbumPopupMenuRecyclerViewAdapter.MyViewHolder> {
    private Context mContext;
    private AppCompatActivity activity;
    private HashMap<String, ArrayList<Image>> listImageByAlbum = new HashMap<>();

    public AlbumPopupMenuRecyclerViewAdapter(Context mContext, HashMap<String, ArrayList<Image>> listImageByAlbum) {
        this.mContext = mContext;
        this.listImageByAlbum = listImageByAlbum;
    }

    public void setData(HashMap<String,ArrayList<Image>> listImageByAlbum){
        listImageByAlbum.remove("Secret");
        listImageByAlbum.remove("Favorite");
        this.listImageByAlbum = listImageByAlbum;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumPopupMenuRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_album_menu_item,parent,false);
        return new AlbumPopupMenuRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumPopupMenuRecyclerViewAdapter.MyViewHolder holder, int position) {
        List<String> keys = new ArrayList(listImageByAlbum.keySet());
        String albumName = keys.get(position);
        int albumSize = listImageByAlbum.get(albumName).size();
        if(albumSize!=0){
            RequestOptions requestOptions = new RequestOptions();
            requestOptions =requestOptions.transform(new CenterCrop(),new RoundedCorners(16));
            Uri albumImageUri = listImageByAlbum.get(albumName).get(0).getUri();
            Glide.with(mContext).load(albumImageUri).apply(requestOptions).into(holder.albumImage);
        }
        holder.albumName.setText(albumName);
        Log.e("TAG", "onBindViewHolder: "+albumName);
        Image image = listImageByAlbum.get(albumName).get(0);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,MainActivity.class);
                intent.putExtra("path",image.getPath());
                intent.putExtra("album_name",albumName);
                Log.e("TAG", "onClick: "+image.getData() );
                activity.setResult(Activity.RESULT_OK,intent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listImageByAlbum.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView albumImage;
        public TextView albumName, albumSize;
        public LinearLayout layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.albumLayout);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            albumSize = itemView.findViewById(R.id.albumSize);

            activity = (AppCompatActivity) mContext;

        }
    }
}