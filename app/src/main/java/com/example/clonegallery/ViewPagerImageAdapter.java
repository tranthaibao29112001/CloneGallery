package com.example.clonegallery;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.ui.photoview.PhotoView;

import java.util.ArrayList;
import java.util.Comparator;

public class ViewPagerImageAdapter extends RecyclerView.Adapter<ViewPagerImageAdapter.MyViewHolder> {
    private ArrayList<Image> images;
    private Context mContext;

    public ViewPagerImageAdapter(ArrayList<Image> images, Context mContext) {
        this.images = images;
        this.mContext = mContext;
    }
    public void setData(ArrayList<Image> images){
        this.images = images;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewPagerImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.viewpager_image_item,parent,false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Image image = images.get(position);
        Uri imageUri = image.getUri();
        Glide.with(mContext).load(imageUri).dontTransform().into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageActivity imageActivity = (ImageActivity) mContext;
                imageActivity.onPageTouch();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(images !=null){
            return images.size();
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private PhotoView imageView;
        private RelativeLayout layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            layout = itemView.findViewById(R.id.viewPagerRelativeLayout);
        }
    }
}
