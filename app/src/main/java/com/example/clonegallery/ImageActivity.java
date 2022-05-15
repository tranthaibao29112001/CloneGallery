package com.example.clonegallery;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.RecoverableSecurityException;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class ImageActivity extends AppCompatActivity implements OnPageTouchListener {
    private ViewPager2 imageViewPager;
    private MyViewModel viewModel;
    private Toolbar toolbar;
    private Image image;
    private AppBarLayout appBarLayout;
    private BottomNavigationView bottomNavigationView;
    private ViewPagerImageAdapter imageViewPagerAdapter;
    private Uri newImageUri ;
    private ArrayList<Image> listImages = new ArrayList<>();
    private ActivityResultLauncher<IntentSenderRequest> mIntentSenderLaucher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == 0 ){
                return;
            }
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
                deleteImage(image);
            }
            if(viewModel.isFavoriteImage(image)){
                viewModel.removeFavoriteImage(image);
            }
            viewModel.removeNewSecretImage(image);
            viewModel.updateImageData();
            onBackPressed();
            return;
        }
    });
    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            newImageUri = result.getData().getData();
        }
    });
    float x = 0;
    float y = 0;
    float dx,dy;
    float firstX  = 0;
    float firstY = 0;


    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageViewPager = findViewById(R.id.imageViewPager2);
        CoordinatorLayout layout = findViewById(R.id.imageActivityLayout);
        toolbar = findViewById(R.id.imageToolBar);
        appBarLayout = findViewById(R.id.imageAppBar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.edit){
                    image = listImages.get(imageViewPager.getCurrentItem());
                    Intent dsPhotoEditorIntent = new Intent(ImageActivity.this, DsPhotoEditorActivity.class);
                    dsPhotoEditorIntent.setData(image.getUri());
                    dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "CloneGallery");
                    mActivityResultLauncher.launch(dsPhotoEditorIntent);
                }
                if(item.getItemId() == R.id.favorite){
                    Image temp = listImages.get(imageViewPager.getCurrentItem());
                    if(viewModel.isFavoriteImage(temp)){
                        int newIndex = listImages.indexOf(temp)-1;
                        if(MyViewModel.currentAlbum.equals("Favorite")){
                            if(newIndex>=0){
                                image = listImages.get(newIndex);
                            }
                            else{
                                if(listImages.size()>1){
                                    image = listImages.get(1);
                                }
                            }
                        }

                        viewModel.removeFavoriteImage(temp);
                        item.setIcon(R.drawable.ic_favorite);
                    }
                    else{
                        viewModel.addNewFavoriteImage(temp);
                        item.setIcon(R.drawable.ic_checked_favorite);
                    }
                }
                if(item.getItemId() == R.id.delete){
                    image = listImages.get(imageViewPager.getCurrentItem());
                    deleteImage(image);
                }
                if(item.getItemId() == R.id.share){
                    Log.e("TAG", "onNavigationItemSelected: "+image.getData() );
                }
                if(item.getItemId() == R.id.more){

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ImageActivity.this,R.style.BottomSheetDialogTheme);
                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_detail_info,(LinearLayout)findViewById(R.id.bottomSheetLayout));
                    bottomSheetView.findViewById(R.id.detailInfo).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ImageActivity.this,DetailInfoActivity.class);
                            Bundle bundle = new Bundle();
                            Image temp = listImages.get(imageViewPager.getCurrentItem());
                            bundle.putParcelable("image",temp);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });

                    bottomSheetView.findViewById(R.id.setScreenBackground).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WallpaperManager myWallpaperManager
                                    = WallpaperManager.getInstance(getApplicationContext());
                            try {
                                Image temp = listImages.get(imageViewPager.getCurrentItem());
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ImageActivity.this.getContentResolver(), temp.getUri());
                                myWallpaperManager.setBitmap(bitmap);
                                Toast.makeText(ImageActivity.this, "Set Wallpaper Success", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                }
                return true;
            }
        });

        imageViewPagerAdapter = new ViewPagerImageAdapter(listImages,this);
        imageViewPager.setAdapter(imageViewPagerAdapter);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            image = bundle.getParcelable("my_photo");
        }
        MyViewModel viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        viewModel.getImagesByAlbum().observe(this, new Observer<HashMap<String, ArrayList<Image>>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(HashMap<String, ArrayList<Image>> stringArrayListHashMap) {
                try{
                    listImages = stringArrayListHashMap.get(MyViewModel.currentAlbum);
                    if(MyViewModel.currentAlbum.equals("Tất cả")){
                        listImages = viewModel.getListAllImage().getValue();
                    }
                    listImages.sort(new Comparator<Image>() {
                        @Override
                        public int compare(Image image, Image t1) {
                            return Integer.compare(t1.getDate(),image.getDate());
                        }
                    });
                    imageViewPagerAdapter.setData(listImages);
                    if(newImageUri!=null){
                        if(listImages.get(0).getUri().equals(newImageUri)){
                            image = listImages.get(0);
                        }
                    }
                    int index =listImages.indexOf(image);
                    imageViewPager.setCurrentItem(index,false);
                    updateLayout(image);
                }
                catch (Exception e){
                    viewModel.updateSecretAlbum();
                    Log.e("TAG", "onChanged: "+e.toString() );
                }
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(convertDateToString(image.getDate()));
        getSupportActionBar().setSubtitle(convertDateToStringHour(image.getDate()));
        imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                try{
                    image = listImages.get(position);
                    updateLayout(image);
                }
                catch (Exception e){
                    onBackPressed();
                }
            }
        });


    }

    private void deleteImage(Image image) {
        try {
            getContentResolver().delete(image.getUri(),null,null);
            if(viewModel.isFavoriteImage(image)){
                viewModel.removeFavoriteImage(image);
            }
            viewModel.removeNewSecretImage(image);
            viewModel.updateImageData();
            onBackPressed();
            Log.e("TAG", "deleteImage:28 " );
        }
        catch (SecurityException e){
            Collection<Uri> uris = new HashSet<>();
            uris.add(image.getUri());
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

    public void updateLayout(Image image){
        getSupportActionBar().setTitle(convertDateToString(image.getDate()));
        getSupportActionBar().setSubtitle(convertDateToStringHour(image.getDate()));
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        if(viewModel.isFavoriteImage(image)){
            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_checked_favorite);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private String convertDateToStringHour(long date){
        Date myDate = new Date(date*1000L);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String strDate = dateFormat.format(myDate);
        return strDate;
    }
    private String convertDateToString(long date){
        Date myDate = new Date(date*1000L);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd 'Thg' MM, yyyy");
        String strDate = dateFormat.format(myDate);
        return strDate;
    }

    @Override
    public void onPageTouch() {
        if(appBarLayout.getVisibility() == View.VISIBLE){
            bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(200).start();
            appBarLayout.animate().translationY(-appBarLayout.getHeight()).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    appBarLayout.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).start();
        }
        else {
            bottomNavigationView.animate().translationY(0).setDuration(200).start();
            appBarLayout.animate().translationY(0).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    appBarLayout.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).start();
        }
    }

}