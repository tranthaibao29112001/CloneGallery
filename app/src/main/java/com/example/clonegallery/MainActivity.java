package com.example.clonegallery;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnActionModeListener {
    private static final String TAG = "Hello";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager2 viewPager2;
    private ViewPager2Adapter viewPager2Adapter;
    private BottomNavigationView bottomActionNavigation;
    private ImageView lockImage;
    private Animation bottomViewAnimation;
    private Animation lockImageAnimation;
    private MyViewModel myViewModel;
    private Toolbar toolbar;
    private ArrayList<Image> listImageChecked = new ArrayList<>();
    private Set<String> stringSet = new HashSet<>();
    private ActivityResultLauncher<IntentSenderRequest> mIntentSenderLaucher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == 0){
                myViewModel.clearAllChecked();
                return;
            }
            myViewModel.updateImageData();
            myViewModel.loadImage();
        }
    });
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent intent = result.getData();
            if(intent == null){
                return;
            }
            String path = intent.getStringExtra("path");
            String albumName = intent.getStringExtra("album_name");
            if(albumName.equals("Favorite")){
                for(int i=0;i<listImageChecked.size();i++){
                    myViewModel.addNewFavoriteImage(listImageChecked.get(i));
                }
                myViewModel.clearAllChecked();
                myViewModel.updateImageData();
                myViewModel.loadImage();
                Toast.makeText(MainActivity.this, "Add To Favorite Success", Toast.LENGTH_SHORT).show();
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
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this,R.style.BottomSheetDialogTheme);
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
                                e.printStackTrace();
                            }
                        }
                        if(isSuccess){
                            myViewModel.clearAllChecked();
                            Toast.makeText(MainActivity.this, "Copy Success", Toast.LENGTH_SHORT).show();
                        }
                        bottomSheetDialog.dismiss();

                    }
                });
                bottomSheetView.findViewById(R.id.move).setOnClickListener(new View.OnClickListener() {
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
                                e.printStackTrace();
                            }
                            if(!isSuccess){
                                listImageChecked.remove(i);
                                i--;
                            }
                        }
                        if(isSuccess){
                            deleteImages(listImageChecked);

                            Toast.makeText(MainActivity.this, "Move Success", Toast.LENGTH_SHORT).show();
                        }
                        myViewModel.clearAllChecked();
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
    private androidx.appcompat.view.ActionMode mActionMode;
    private androidx.appcompat.view.ActionMode.Callback actionModeCallBack = new androidx.appcompat.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            viewPager2.setUserInputEnabled(false);
            mode.getMenuInflater().inflate(R.menu.mutil_select_menu,menu);
            mode.setTitle("Chọn mục");
            toolbar.setVisibility(View.GONE);
            bottomActionNavigation.setVisibility(View.VISIBLE);
            bottomViewAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
            bottomActionNavigation.startAnimation(bottomViewAnimation);
            myViewModel.getImagesChecked().observe(MainActivity.this, new Observer<ArrayList<Image>>() {
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
            viewPager2.setUserInputEnabled(true);
            toolbar.setVisibility(View.VISIBLE);
            bottomViewAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
            bottomActionNavigation.startAnimation(bottomViewAnimation);
            bottomViewAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bottomActionNavigation.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myViewModel = new ViewModelProvider(MainActivity.this).get(MyViewModel.class);
        SharedPreferences sharedPreferences = getSharedPreferences("Secret Album", Context.MODE_PRIVATE);
        Set<String> secretStringSet = sharedPreferences.getStringSet("Secret Set",new HashSet<>());
        Log.e(TAG, "onCreate: "+secretStringSet );
        myViewModel.setSecretStringSet(secretStringSet);

        myViewModel.getListAllImage().observe(this, new Observer<ArrayList<Image>>() {
            @Override
            public void onChanged(ArrayList<Image> images) {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("Favorite",MODE_PRIVATE);
                    stringSet = sharedPreferences.getStringSet("FavoriteSet",new HashSet<>());
                    myViewModel.clearAllFavoriteImage();
                    for(int i=0;i<images.size();i++){
                        if(stringSet.contains(images.get(i).getName())){
                            myViewModel.addNewFavoriteImage(images.get(i));
                        }
                    }
                    myViewModel.addFavoriteAlbum(myViewModel.getListFavoriteImage().getValue());
                }
                catch (Exception e){
                    Log.e(TAG, "onChanged: "+e );
                }
            }
        });
        myViewModel.getImagesByAlbum();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);
        appBarLayout = findViewById(R.id.appBar);
        lockImage = findViewById(R.id.lockImage);
        appBarLayout.setExpanded(false);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.e(TAG, "onOffsetChanged: "+ verticalOffset );
                if(verticalOffset >= -20){
                    if(lockImage.getVisibility() == View.GONE){
                        lockImageAnimation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.lock_slide_down);
                        lockImage.startAnimation(lockImageAnimation);
                        lockImage.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    lockImage.setVisibility(View.GONE);
                }
            }
        });
        bottomActionNavigation = findViewById(R.id.bottomNavigationActionMode);
        bottomActionNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.action_delete){
                    if(listImageChecked.size() !=0){
                        deleteImages(listImageChecked);
                    }
                }
                if(item.getItemId()== R.id.add_to_album){
                    if(listImageChecked.size() !=0){
                        Intent intent = new Intent(MainActivity.this,AlbumPopupMenuActivity.class);
                        activityResultLauncher.launch(intent);
                    }
                }
                return true;
            }
        });

        viewPager2Adapter = new ViewPager2Adapter(this);
        viewPager2.setAdapter(viewPager2Adapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    MyViewModel.currentAlbum = "Tất cả";
                }
                if(position == 1){
                    ImageFragment.isOnCameraTab = false;
                }
                super.onPageSelected(position);
            }
        });
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText("Image");
                        break;
                    }
                    case 1:{
                        tab.setText("Album");
                        break;
                    }
                }
            }
        }).attach();
        requestStoragePermission();

    }
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
    private void deleteImages(ArrayList<Image> images) {
        try {
            for(int i=0;i<images.size();i++){
                getContentResolver().delete(images.get(i).getUri(),null,null);
            }
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
    // Xin quyền cho ứng dụng
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
            } else {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,  Manifest.permission.ACCESS_MEDIA_LOCATION};
                requestPermissions(permissions, STORAGE_PERMISSION_CODE);
            }
        }
    }
    public void touchTrigger(){
        if(lockImage.getVisibility() == View.VISIBLE){
            Intent intent = new Intent(this,PatternLockActivity.class);
            startActivity(intent);
        }
        lockImage.setVisibility(View.GONE);
        appBarLayout.setExpanded(false,true);

    }
    // Kiểm tra kết quả sau khi xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Thành công
                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.image_menu,menu);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position){
                    case 0:{
                        menu.clear();
                        getMenuInflater().inflate(R.menu.image_menu,menu);
                        break;
                    }
                    case 1:{
                        menu.clear();
                        getMenuInflater().inflate(R.menu.album_menu,menu);
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return true;
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
    protected void onResume() {
        super.onResume();
    }
}