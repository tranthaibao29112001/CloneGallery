package com.example.clonegallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumFragment extends Fragment {
    private AlbumRecyclerViewAdapter adapter;
    private RecyclerView albumRecyclerView;
    private HashMap<String, ArrayList<Image>> listImageByAlbum = new HashMap<>();
    private MyViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumRecyclerView = view.findViewById(R.id.albumRecyclerView);
        albumRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity activity = (MainActivity) getActivity();
                if(activity!=null){
                   if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                       activity.touchTrigger();
                   }
                }
                return false;
            }
        });

        adapter = new AlbumRecyclerViewAdapter(getContext(),listImageByAlbum);
        albumRecyclerView.setAdapter(adapter);
        albumRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewModel.getImagesByAlbum().observe(this, new Observer<HashMap<String, ArrayList<Image>>>() {
            @Override
            public void onChanged(HashMap<String, ArrayList<Image>> stringArrayListHashMap) {
                Log.e("TAG", "onChanged: "+stringArrayListHashMap.keySet() );
                listImageByAlbum = stringArrayListHashMap;
                adapter.setData((HashMap<String, ArrayList<Image>>) listImageByAlbum.clone());
            }
        });
    }


}