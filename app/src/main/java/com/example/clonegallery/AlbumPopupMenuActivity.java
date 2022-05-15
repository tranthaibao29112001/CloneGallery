package com.example.clonegallery;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumPopupMenuActivity extends AppCompatActivity {
    private AlbumPopupMenuRecyclerViewAdapter adapter;
    private RecyclerView albumRecyclerView;
    private HashMap<String, ArrayList<Image>> listImageByAlbum = new HashMap<>();
    private MyViewModel viewModel;
    private LinearLayout secretAlbum, favoriteAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_popup_menu);

        albumRecyclerView = findViewById(R.id.albumPopupRecyclerView);
        secretAlbum = findViewById(R.id.secretAlbum);
        favoriteAlbum = findViewById(R.id.favorite_album);

        secretAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumPopupMenuActivity.this,MainActivity.class);
                intent.putExtra("album_name","Secret");
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
        favoriteAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumPopupMenuActivity.this,MainActivity.class);
                intent.putExtra("album_name","Favorite");
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
        setTitle("Thêm vào Album");
        adapter = new AlbumPopupMenuRecyclerViewAdapter(this, listImageByAlbum);
        albumRecyclerView.setAdapter(adapter);
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewModel.getImagesByAlbum().observe(this, new Observer<HashMap<String, ArrayList<Image>>>() {
            @Override
            public void onChanged(HashMap<String, ArrayList<Image>> stringArrayListHashMap) {
                adapter.setData(stringArrayListHashMap);
            }
        });


    }
}