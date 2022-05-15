package com.example.clonegallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.MyViewHolder> {
    private Context mContext;
    private HashMap<String, ArrayList<Image>> listImageByAlbum = new HashMap<>();

    public AlbumRecyclerViewAdapter(Context mContext, HashMap<String, ArrayList<Image>> listImageByAlbum) {
        this.mContext = mContext;
        this.listImageByAlbum = listImageByAlbum;
    }

    public void setData(HashMap<String,ArrayList<Image>> listImageByAlbum){
        this.listImageByAlbum = listImageByAlbum;
        listImageByAlbum.remove("Secret");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.album_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
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
        holder.albumSize.setText(String.valueOf(albumSize));

        holder.albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,AlbumActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("album_image",listImageByAlbum.get(albumName));
                bundle.putString("album_name",albumName);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                MyViewModel.currentAlbum = albumName;
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
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            albumSize = itemView.findViewById(R.id.albumSize);
        }
    }
}
