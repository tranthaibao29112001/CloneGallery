package com.example.clonegallery;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ListImageRecyclerViewAdapter extends RecyclerView.Adapter<ListImageRecyclerViewAdapter.MyViewHolder> {
    private HashMap<String, ArrayList<Image>> imageListByDate = new HashMap<>();
    private Context mContext;
    private HashMap<String, ImageRecyclerViewAdapter> adapterByDate = new HashMap<>();

    public ListImageRecyclerViewAdapter(HashMap<String, ArrayList<Image>> imageListByDate, Context mContext) {
        this.imageListByDate = imageListByDate;
        this.mContext = mContext;
    }
    public void setData(HashMap<String, ArrayList<Image>> imageListByDate){
        if(this.imageListByDate == null){
            this.imageListByDate = imageListByDate;
            notifyDataSetChanged();
            return;
        }
        List<String> oldKeys = new ArrayList(this.imageListByDate.keySet());
        List<String> newKeys = new ArrayList(imageListByDate.keySet());

        if(oldKeys.size() >= newKeys.size()){
            for(String key: oldKeys){
                if(!newKeys.contains(key)){
                    removeImagesByDateItem(key);
                }
            }
        }
        else{
            for(String key: newKeys){
                if(!oldKeys.contains(key)){
                    insertImageByDateItem(key, imageListByDate.get(key));
                }
            }
        }
        this.imageListByDate = imageListByDate;

        for(String key: newKeys){
            if(adapterByDate.get(key)!=null){
                adapterByDate.get(key).setData(this.imageListByDate.get(key));
            }
        }
    }

    @NonNull
    @Override
    public ListImageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.date_image_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        List<String> keys = new ArrayList(imageListByDate.keySet());
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                Date date1 = stringToDate(s);
                Date date2 = stringToDate(t1);
                return date2.compareTo(date1);
            }
        });
        String strDate = keys.get(position);
        holder.date.setText(strDate);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageListByDate.get(strDate).sort(new Comparator<Image>() {
                @Override
                public int compare(Image image, Image t1) {
                    return Integer.compare(t1.getDate(),image.getDate());
                }
            });
        }
        ImageRecyclerViewAdapter imageAdapter = new ImageRecyclerViewAdapter((ArrayList<Image>) imageListByDate.get(strDate).clone(),this,mContext);
        adapterByDate.put(strDate,imageAdapter);
        holder.imageRecyclerView.setAdapter(imageAdapter);
        holder.imageRecyclerView.setLayoutManager(new GridLayoutManager(mContext,4));
    }

    @Override
    public int getItemCount() {
        if(imageListByDate ==null){
            return 0;
        }
        return imageListByDate.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private LinearLayout linearLayout;
        private RecyclerView imageRecyclerView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.imageDate);
            imageRecyclerView = itemView.findViewById(R.id.imageRecyclerView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
    private String formatDate(Date date){
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd 'Thg' MM, yyyy");

        String strDate = dateFormat.format(date);
        return strDate;
    }
    @SuppressLint("SimpleDateFormat")
    private Date stringToDate(String strDate){
        Date date = null;
        try {
            date = new SimpleDateFormat("dd 'Thg' MM, yyyy").parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public void removeImagesByDateItem(String date){
        List<String> keys = new ArrayList(imageListByDate.keySet());
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                Date date1 = stringToDate(s);
                Date date2 = stringToDate(t1);
                return date2.compareTo(date1);
            }
        });
        notifyItemRemoved(keys.indexOf(date));
        imageListByDate.remove(date);
    }
    public void insertImageByDateItem(String date,ArrayList<Image> images){
        List<String> keys = new ArrayList(imageListByDate.keySet());
        keys.add(date);
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                Date date1 = stringToDate(s);
                Date date2 = stringToDate(t1);
                return date2.compareTo(date1);
            }
        });
        notifyItemInserted(keys.indexOf(date));
        imageListByDate.put(date,images);
    }
    public ImageRecyclerViewAdapter getImageAdapterByDate(String date){
        return adapterByDate.get(date);
    }
    public List<String> sortListKeys(List<String> keys){
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                Date date1 = stringToDate(s);
                Date date2 = stringToDate(t1);
                return date2.compareTo(date1);
            }
        });
        return keys;
    }
}
