package com.example.clonegallery;

import android.animation.LayoutTransition;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageFragment extends Fragment {
    private RecyclerView imageByDateRecyclerView;
    private ListImageRecyclerViewAdapter adapter;
    private HashMap<String,ArrayList<Image>> imageByDateList;
    private HashMap<String, ArrayList<Image>> cameraImageList;
    private MyViewModel viewModel;
    private TabLayout tabLayout;
    public static boolean isOnCameraTab = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageByDateRecyclerView = view.findViewById(R.id.imageByDateRecyclerView);

        adapter = new ListImageRecyclerViewAdapter(imageByDateList,getContext());
        imageByDateRecyclerView.setAdapter(adapter);
        imageByDateRecyclerView.setNestedScrollingEnabled(true);
        imageByDateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewModel.getImagesByDate().observe(this, new Observer<HashMap<String, ArrayList<Image>>>() {
            @Override
            public void onChanged(HashMap<String, ArrayList<Image>> stringArrayListHashMap) {
                try{
                    Log.e("TAG", "onChanged: "+stringArrayListHashMap );
                    imageByDateList = stringArrayListHashMap;
                    if(MyViewModel.currentAlbum.equals("Tất cả")){
                        adapter.setData((HashMap<String, ArrayList<Image>>) imageByDateList.clone());
                        imageByDateRecyclerView.scrollToPosition(0);
                    }
                    else if(MyViewModel.currentAlbum.equals("Camera")){
                        cameraImageList = getCamaraImages(imageByDateList);
                        adapter.setData((HashMap<String, ArrayList<Image>>) cameraImageList.clone());
                        imageByDateRecyclerView.scrollToPosition(0);
                    }
                }
                catch (Exception e){
                    Log.e("TAG", "onChanged: "+e.toString() );
                }
            }
        });
        viewModel.getImagesChecked().observe(this, new Observer<ArrayList<Image>>() {
            @Override
            public void onChanged(ArrayList<Image> images) {
                adapter.notifyDataSetChanged();
            }
        });

        tabLayout = view.findViewById(R.id.bottomTabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().equals("Tất cả")){
                    adapter.setData((HashMap<String, ArrayList<Image>>) imageByDateList.clone());
                    MyViewModel.currentAlbum = "Tất cả";
                }
                else{
                    cameraImageList = getCamaraImages(imageByDateList);
                    adapter.setData((HashMap<String, ArrayList<Image>>) cameraImageList.clone());
                    MyViewModel.currentAlbum = "Camera";
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    public HashMap<String,ArrayList<Image>> getCamaraImages(HashMap<String,ArrayList<Image>> listImageByDate){
        HashMap<String,ArrayList<Image>> cameraImageList = new HashMap<>();
        for(String key:listImageByDate.keySet()){
            ArrayList<Image> tempList = listImageByDate.get(key);
            ArrayList<Image> cameraList = new ArrayList<>();
            for(int i =0;i<tempList.size();i++){
                if(tempList.get(i).getAlbum().equals("Camera")){
                    cameraList.add(tempList.get(i));
                }
            }
            if(cameraList.size() != 0){
                cameraImageList.put(key,cameraList);
            }
        }
        return cameraImageList;
    }


    @Override
    public void onResume() {
        if(imageByDateList!=null){
            if(MyViewModel.currentAlbum.equals("Tất cả")){
                adapter.setData((HashMap<String, ArrayList<Image>>) imageByDateList.clone());
            }
            else{
                cameraImageList = getCamaraImages(imageByDateList);
                adapter.setData((HashMap<String, ArrayList<Image>>) cameraImageList.clone());
            }
        }
        super.onResume();
    }

}