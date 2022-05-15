package com.example.clonegallery;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Comparator;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<Image> images;
    private Context mContext;
    private ListImageRecyclerViewAdapter parentAdapter;
    private MyViewModel myViewModel;
    private OnActionModeListener onActionModeListener;
    private Animation scaleUp, scaleDown;
    public ImageRecyclerViewAdapter(ArrayList<Image> images,ListImageRecyclerViewAdapter adapter, Context mContext) {
        this.images = images;
        this.mContext = mContext;
        this.parentAdapter = adapter;
        myViewModel = new ViewModelProvider((ViewModelStoreOwner) mContext).get(MyViewModel.class);
    }
    public void setData(ArrayList<Image> images){
        if(images == null){
            return;
        }
        if(this.images == null){
            this.images = images;
            notifyDataSetChanged();
            return;
        }
        if(this.images.size()>=images.size()){
            for(int i=0;i<this.images.size();i++){
                if(!images.contains(this.images.get(i))){
                    removeItem(this.images.get(i).getName());
                    i--;
                }
            }
        }
        else{
            for(int i=0;i<images.size();i++){

                if(!this.images.contains(images.get(i))){
                    insertItem(images.get(i));
                }
            }
        }

    }

    @NonNull
    @Override
    public ImageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_item,parent,false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ImageRecyclerViewAdapter.MyViewHolder holder, int position) {
        Image image = images.get(position);
        Uri imageUri = image.getUri();
        Glide.with(mContext).load(imageUri).centerCrop().into(holder.imageView);
//        if(Image.getAllNewImage().contains(image)){
//            if(image.getAlbum().equals("Screenshots")){
//                holder.imageView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_in));
//                Image.getAllNewImage().remove(image);
//            }
//        }
        if(onActionModeListener.isOnActionMode()){
            holder.checkBox.setVisibility(View.VISIBLE);
        }
        else{
            holder.checkBox.setVisibility(View.GONE);
        }
        if(myViewModel.isCheckedImage(image)){
            holder.checkBox.setChecked(true);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onActionModeListener.isOnActionMode()){
                    if(holder.checkBox.isChecked()){
                        myViewModel.removeCheckedImage(image);
                        holder.checkBox.setChecked(false);
                    }
                    else {
                        myViewModel.addNewCheckedImage(image);
                        holder.checkBox.setChecked(true);
                    }
                    return;
                }
                try{
                    Intent intent = new Intent(mContext,ImageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("my_photo",images.get(position));
                    intent.putExtras(bundle);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,holder.imageView,holder.imageView.getTransitionName());
                    mContext.startActivity(intent,options.toBundle());
                }
                catch (Exception e){
                    Log.e("TAG", "onClick: "+e );
                }

            }
        });
        scaleUp = AnimationUtils.loadAnimation(mContext,R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(mContext,R.anim.scale_down);
        holder.imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    holder.imageView.startAnimation(scaleDown);
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    holder.imageView.startAnimation(scaleUp);
                }
                return false;
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onActionModeListener.openActionMode();
                parentAdapter.notifyDataSetChanged();
                return true;
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
        private ImageView imageView;
        private RelativeLayout layout;
        private CheckBox checkBox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            layout = itemView.findViewById(R.id.relativeLayoutImage);
            checkBox = itemView.findViewById(R.id.chkBox);
            if(mContext!=null){
                if(mContext instanceof OnActionModeListener){
                    onActionModeListener = (OnActionModeListener) mContext;
                }
            }
        }
    }
    public void removeItem(String imageName){
        for(int i =0 ;i<images.size();i++){
            if(imageName.equals(images.get(i).getName())){
                images.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i,getItemCount());
            }
        }
    }
    public void insertItem(Image image){
        if(images.contains(image)){
            return;
        }
        images.add(image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            images.sort(new Comparator<Image>() {
                @Override
                public int compare(Image image, Image t1) {
                    return Integer.compare(t1.getDate(),image.getDate());
                }
            });
        }
        notifyItemInserted(images.indexOf(image));
        notifyItemRangeChanged(images.indexOf(image),getItemCount());
    }
}
