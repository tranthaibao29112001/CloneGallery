package com.example.clonegallery;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.RecoverableSecurityException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AlbumActivity extends AppCompatActivity implements OnActionModeListener {
    private HashMap<String,ArrayList<Image>> listImageByDate = new HashMap<>();
    private ListImageRecyclerViewAdapter adapter;
    private RecyclerView albumImageRecyclerView;
    private Toolbar toolbar;
    private MyViewModel myViewModel;
    private BottomNavigationView bottomActionNavigation;
    private Animation bottomViewAnimation;
    private ArrayList<Image> listImageChecked = new ArrayList<>();
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent intent = result.getData();
            String path = intent.getStringExtra("path");
            String albumName = intent.getStringExtra("album_name");
            if(albumName.equals("Favorite")){
                for(int i=0;i<listImageChecked.size();i++){
                    myViewModel.addNewFavoriteImage(listImageChecked.get(i));
                }
                myViewModel.clearAllChecked();
                Toast.makeText(AlbumActivity.this, "Add To Favorite Success", Toast.LENGTH_SHORT).show();
            }
            else if(albumName.equals("Secret")){
                for(int i=0;i<listImageChecked.size();i++){
                    myViewModel.addNewSecretImage(listImageChecked.get(i));
                }
                myViewModel.clearAllChecked();
                myViewModel.updateImageData();
                myViewModel.loadImage();
            }
            else{
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AlbumActivity.this,R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_dialog,(LinearLayout)findViewById(R.id.bottomSheetLayout));
                bottomSheetView.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isSuccess = true;
                        for(int i=0;i<listImageChecked.size();i++){
                            File from = new File(listImageChecked.get(i).getData());
                            File to = new File("/storage/emulated/0/"+path+listImageChecked.get(i).getName());
                            try {
                                isSuccess = copyFile(from,to);
                            } catch (IOException e) {
                                isSuccess = false;
                            }
                            if(!isSuccess){
                                listImageChecked.remove(i);
                                i--;
                            }
                        }
                        if(isSuccess){
                            myViewModel.updateSecretAlbum();
                            myViewModel.clearAllChecked();
                            Toast.makeText(AlbumActivity.this, "Copy Success", Toast.LENGTH_SHORT).show();
                        }
                        bottomSheetDialog.dismiss();

                    }
                });
                bottomSheetView.findViewById(R.id.move).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isSuccess = true;
                        Log.e("TAG", "onClick: "+listImageChecked );
                        for(int i=0;i<listImageChecked.size();i++){
                            File from = new File(listImageChecked.get(i).getData());
                            File to = new File("/storage/emulated/0/"+path+listImageChecked.get(i).getName());
                            try {
                                isSuccess = copyFile(from,to);
                            } catch (IOException e) {
                                isSuccess = false;
                                e.printStackTrace();
                            }
                            if(!isSuccess){
                                listImageChecked.remove(i);
                                i--;
                            }
                        }
                        if(isSuccess){
                            Log.e("TAG", "onClick: "+listImageChecked );
                            deleteImages(listImageChecked);
                            myViewModel.updateSecretAlbum();
                            Toast.makeText(AlbumActivity.this, "Move Success", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            myViewModel.clearAllChecked();
                        }
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                        myViewModel.clearAllChecked();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        }
    });

    public static boolean copyFile(File src, File dst) throws IOException
    {
        if(!src.exists()){
            return false;
        }
        Log.e("TAG", "copyFile: "+src.getAbsolutePath() );
        Log.e("TAG", "copyFile: "+dst.getAbsolutePath() );
        if(src.getAbsolutePath().equals(dst.getAbsolutePath())){

            return false;
        }
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();

        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
            return true;
        }
    }

    private ActivityResultLauncher<IntentSenderRequest> mIntentSenderLaucher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == 0){
                myViewModel.clearAllChecked();
                return;
            }
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
                deleteImages(listImageChecked);
            }
            for(int i=0;i<listImageChecked.size();i++){
                if(myViewModel.isFavoriteImage(listImageChecked.get(i))){
                    myViewModel.removeFavoriteImage(listImageChecked.get(i));
                }
                myViewModel.removeNewSecretImage(listImageChecked.get(i));
            }
            myViewModel.updateImageData();
            myViewModel.loadImage();
            myViewModel.updateSecretAlbum();
        }
    });
    private androidx.appcompat.view.ActionMode mActionMode;
    private androidx.appcompat.view.ActionMode.Callback actionModeCallBack = new androidx.appcompat.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.mutil_select_menu,menu);
            mode.setTitle("Chọn mục");
            toolbar.setVisibility(View.GONE);
            bottomActionNavigation.setVisibility(View.VISIBLE);
            bottomViewAnimation = AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.slide_up);
            bottomActionNavigation.startAnimation(bottomViewAnimation);
            myViewModel = new ViewModelProvider(AlbumActivity.this).get(MyViewModel.class);
            myViewModel.getImagesChecked().observe(AlbumActivity.this, new Observer<ArrayList<Image>>() {
                @Override
                public void onChanged(ArrayList<Image> images) {
                    listImageChecked = (ArrayList<Image>) images.clone();
                    mode.setTitle("Đã chọn "+ images.size()+ " mục") ;
                }
            });
            return true;
        }

        @Override
        public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.select_all){
                myViewModel.checkedAllImage();
                mode.getMenu().clear();
                mode.getMenuInflater().inflate(R.menu.clear_all_menu,mode.getMenu());
            }
            if(item.getItemId() == R.id.clear_all){
                myViewModel.clearAllChecked();
                mode.getMenu().clear();
                mode.getMenuInflater().inflate(R.menu.mutil_select_menu,mode.getMenu());
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
            mActionMode = null;
            myViewModel.clearAllChecked();
            toolbar.setVisibility(View.VISIBLE);
            bottomViewAnimation = AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.slide_down);
            bottomActionNavigation.startAnimation(bottomViewAnimation);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Bundle bundle = getIntent().getExtras();
        String albumName = bundle.getString("album_name");

        toolbar = findViewById(R.id.albumImageToolBar);
        albumImageRecyclerView = findViewById(R.id.albumImageRecyclerView);
        bottomActionNavigation = findViewById(R.id.bottomNavigationActionMode);

        bottomActionNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.action_delete){
                    if(listImageChecked.size() != 0){
                        deleteImages(listImageChecked);
                    }
                }
                if(item.getItemId() == R.id.add_to_album){
                    if(listImageChecked.size()!=0){
                        Intent intent = new Intent(AlbumActivity.this,AlbumPopupMenuActivity.class);
                        activityResultLauncher.launch(intent);
                    }

                }
                return true;
            }
        });

        setSupportActionBar(toolbar);
        toolbar.setTitle(albumName);

        adapter = new ListImageRecyclerViewAdapter(listImageByDate,this);
        albumImageRecyclerView.setAdapter(adapter);
        albumImageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.getImagesByAlbum().observe(this, new Observer<HashMap<String, ArrayList<Image>>>() {
            @Override
            public void onChanged(HashMap<String, ArrayList<Image>> stringArrayListHashMap) {
                try {
                    ArrayList<Image> albumImage = stringArrayListHashMap.get(albumName);
                    convertArrangeImageListByDate(albumImage);
                    adapter.setData(listImageByDate);
                    adapter.notifyDataSetChanged();
                }
                catch (Exception e){
                    listImageByDate = new HashMap<>();
                    adapter.setData(listImageByDate);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        myViewModel.getImagesChecked().observe(this, new Observer<ArrayList<Image>>() {
            @Override
            public void onChanged(ArrayList<Image> images) {
                adapter.notifyDataSetChanged();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    public void convertArrangeImageListByDate(ArrayList<Image> albumImages){
        if(albumImages.size() == 0 ){
            listImageByDate = new HashMap<>();
            return;
        }
        Collections.sort(albumImages, new Comparator<Image>() {
            @Override
            public int compare(Image image, Image t1) {
                return Integer.compare(image.getDate(), t1.getDate());
            }
        });
        String strDate = null;
        listImageByDate.clear();
        for(int i= 0;i<albumImages.size();i++){
            ArrayList<Image> tempImageList = new ArrayList<>();
            strDate = convertDateToString((long)albumImages.get(i).getDate());
            if(listImageByDate.containsKey(strDate)){
                tempImageList  = listImageByDate.get(strDate);
            }
            tempImageList.add(albumImages.get(i));
            listImageByDate.put(strDate,tempImageList);
        }
    }
    public String convertDateToString(long longDate){
        Date date = new Date(longDate*1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd 'Thg' MM, yyyy");
        String strDate = simpleDateFormat.format(date);
        return strDate;
    }
    private void deleteImages(ArrayList<Image> images) {
        try {
            for(int i=0;i<images.size();i++){
                getContentResolver().delete(images.get(i).getUri(),null,null);
            }
            for(int i=0;i<images.size();i++){
                myViewModel.removeNewSecretImage(images.get(i));
            }
            myViewModel.updateSecretAlbum();
            myViewModel.updateImageData();
        }
        catch (SecurityException e){
            Collection<Uri> uris = new HashSet<>();
            for(int i=0;i<images.size();i++){
                uris.add(images.get(i).getUri());
            }
            IntentSender intentSender;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.e("TAG", "deleteImage:30 " );
                intentSender = MediaStore.createDeleteRequest(getContentResolver(),uris).getIntentSender();
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                Log.e("TAG", "deleteImage:29 " );
                RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) e;
                intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
            }
            else{
                intentSender = null;
            }
            mIntentSenderLaucher.launch(new IntentSenderRequest.Builder(intentSender).build());

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void openActionMode() {
        mActionMode = startSupportActionMode(actionModeCallBack);
    }

    @Override
    public boolean isOnActionMode() {
        if(mActionMode!=null){
            return true;
        }
        return false;
    }
    @Override
    public void onResume() {
        myViewModel.updateSecretAlbum();
        adapter.notifyDataSetChanged();
        super.onResume();
    }
}