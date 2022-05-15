package com.example.clonegallery;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MyViewModel extends AndroidViewModel {
    private static MutableLiveData<HashMap<String,ArrayList<Image>>> listImageByDate;
    private static MutableLiveData<HashMap<String,ArrayList<Image>>> listImageByAlbum;
    private static MutableLiveData<ArrayList<Image>> listSecretImage;
    private static MutableLiveData<ArrayList<Image>> listImageChecked;
    private static MutableLiveData<ArrayList<Image>> listImageFavorite;
    private static MutableLiveData<ArrayList<Image>> listAllImage;
    private static Set<String> secretStringSet= new HashSet<>();
    private static HashMap<String,ArrayList<Image>> tempImagesByDate = new HashMap<String,ArrayList<Image>>();
    private static HashMap<String,ArrayList<Image>> tempImagesByAlbum = new HashMap<String,ArrayList<Image>>();
    private ArrayList<Image> tempListImageChecked = new ArrayList<>();
    private static ArrayList<Image> tempListImageFavorite = new ArrayList<>();
    private static ArrayList<Image> tempListSecretImage = new ArrayList<>();
    public static String currentAlbum = "Tất cả";
    public static ArrayList<Image> tempListAllImage = new ArrayList<>();

    public MyViewModel(@NonNull Application application) {
        super(application);

        Handler handler = new Handler(Looper.getMainLooper());
        ContentObserver contentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                super.onChange(selfChange, uri);
                loadImage();
            }
        };
        getApplication().getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,true,contentObserver);
    }

    public LiveData<HashMap<String,ArrayList<Image>>> getImagesByDate() {
        if (listImageByDate == null) {
            listImageByDate = new MutableLiveData<HashMap<String,ArrayList<Image>>>();
            loadImage();
        }
        return listImageByDate;
    }
    public MutableLiveData<ArrayList<Image>> getListAllImage(){
        if (listAllImage == null) {
            listAllImage = new MutableLiveData<>();
            loadImage();
        }
        return listAllImage;
    }
    public void addNewCheckedImage(Image image){
        if(!tempListImageChecked.contains(image)){
            tempListImageChecked.add(image);
        }
        if(listImageChecked == null){
            listImageChecked = new MutableLiveData<>();
        }
        listImageChecked.setValue(tempListImageChecked);
    }
    public boolean isCheckedImage(Image image){
        return tempListImageChecked.contains(image);
    }
    public void clearAllChecked(){
        tempListImageChecked.clear();
        if(listImageChecked == null){
            listImageChecked = new MutableLiveData<>();
        }
        listImageChecked.setValue(tempListImageChecked);
    }
    public void checkedAllImage(){
        if(listImageChecked == null){
            listImageChecked = new MutableLiveData<>();
        }
        if(currentAlbum.equals("Tất cả")){
            tempListImageChecked = (ArrayList<Image>) tempListAllImage.clone();
        }
        else{
            for(int i =0;i<tempListAllImage.size();i++){
                if(tempListAllImage.get(i).getAlbum().equals(currentAlbum)){
                    tempListImageChecked.add(tempListAllImage.get(i));
                }
            }
        }
        listImageChecked.setValue(tempListImageChecked);
    }
    public void removeCheckedImage(Image image){
        if(tempListImageChecked.contains(image)){
            tempListImageChecked.remove(tempListImageChecked.indexOf(image));
        }
        if(listImageChecked == null){
            listImageChecked = new MutableLiveData<>();
        }
        listImageChecked.setValue(tempListImageChecked);
    }
    public void addNewFavoriteImage(Image image){
        tempListImageFavorite = listImageFavorite.getValue();
        if(!tempListImageFavorite.contains(image)){
            tempListImageFavorite.add(image);
        }
        if(listImageFavorite == null){
            listImageFavorite = new MutableLiveData<>();
        }
        addFavoriteAlbum(tempListImageFavorite);
        listImageFavorite.setValue(tempListImageFavorite);
    }
    public void removeFavoriteImage(Image image){
        tempListImageFavorite = listImageFavorite.getValue();
        if(tempListImageFavorite.contains(image)){
            tempListImageFavorite.remove(tempListImageFavorite.indexOf(image));
        }
        if(listImageFavorite == null){
            listImageFavorite = new MutableLiveData<>();
        }
        addFavoriteAlbum(tempListImageFavorite);
        listImageFavorite.setValue(tempListImageFavorite);
    }
    public void clearAllFavoriteImage(){
        tempListImageFavorite.clear();
        if(listImageFavorite == null){
            listImageFavorite = new MutableLiveData<>();
        }
        listImageFavorite.setValue(tempListImageFavorite);
    }
    public void addFavoriteAlbum(ArrayList<Image> images){
        tempListImageFavorite = listImageFavorite.getValue();
        if(listImageByAlbum !=null){
            tempImagesByAlbum = listImageByAlbum.getValue();
            Log.e("TAG", "addFavoriteAlbum: "+images );
            tempImagesByAlbum.put("Favorite",images);
            listImageByAlbum.setValue(tempImagesByAlbum);
        }
    }
    public boolean isFavoriteImage(Image image){
        return tempListImageFavorite.contains(image);
    }
    public LiveData<ArrayList<Image>> getListFavoriteImage(){
        if (listImageFavorite == null) {
            listImageFavorite = new MutableLiveData<>();
        }
        return listImageFavorite;
    }
    public void setSecretStringSet(Set<String> stringSet){
        secretStringSet = stringSet;
    }
    public void addNewSecretImage(Image image){
        if(!secretStringSet.contains(image.getData())){
            tempListSecretImage.add(image);
            secretStringSet.add(image.getData());
        }
    }
    public void removeNewSecretImage(Image image){
        Log.e("TAG", "removeNewSecretImage: "+image );
        if(secretStringSet.contains(image.getData())){
            tempListSecretImage.remove(image);
            secretStringSet.remove(image.getDate());
        }
    }
    public void updateSecretAlbum(){
        if(listSecretImage == null){
            listSecretImage = new MutableLiveData<>();
        }
        tempImagesByAlbum = listImageByAlbum.getValue();
        tempImagesByAlbum.put("Secret",tempListSecretImage);
        Log.e("TAG", "updateSecretAlbum: "+tempListSecretImage );
        listImageByAlbum.setValue(tempImagesByAlbum);
    }
    public LiveData<ArrayList<Image>> getListSecretImage(){
        if(listSecretImage == null){
            listSecretImage = new MutableLiveData<>();
        }
        return listSecretImage;
    }
    public LiveData<ArrayList<Image>> getImagesChecked(){
        if(listImageChecked ==null){
            listImageChecked = new MutableLiveData<>();
        }
        return listImageChecked;
    }
    public LiveData<HashMap<String,ArrayList<Image>>> getImagesByAlbum() {
        if (listImageByAlbum == null) {
            listImageByAlbum = new MutableLiveData<HashMap<String,ArrayList<Image>>>();
            loadImage();
        }
        return listImageByAlbum;
    }

    @Override
    protected void onCleared() {
        SharedPreferences sharedPreferencesFavorite = getApplication().getSharedPreferences("Favorite", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferencesSecret = getApplication().getSharedPreferences("Secret Album", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorFavorite = sharedPreferencesFavorite.edit();
        SharedPreferences.Editor editorSecret = sharedPreferencesSecret.edit();
        Set<String> favoriteSet = new HashSet<>();
        Set<String> secretSet = new HashSet<>();
        if(tempListImageFavorite !=null){
            for(int i=0;i<tempListImageFavorite.size();i++){
                favoriteSet.add(tempListImageFavorite.get(i).getName());
            }
        }
        if(tempListSecretImage !=null){
            for(int i=0;i<tempListSecretImage.size();i++){
                secretSet.add(tempListSecretImage.get(i).getData());
            }
        }
        editorFavorite.putStringSet("FavoriteSet",favoriteSet);
        editorFavorite.apply();
        editorSecret.putStringSet("Secret Set",secretSet);
        Log.e("TAG", "onCleared: "+secretSet );
        editorSecret.apply();
        super.onCleared();
    }

    public void updateImageData(){
        tempImagesByAlbum.clear();
        tempImagesByDate.clear();
        tempListAllImage.clear();
        addFavoriteAlbum(tempListImageFavorite);
        listImageFavorite.setValue(tempListImageFavorite);
        clearAllChecked();
    }

    public void loadImage() {
        LoadImageTask loadImageTask = new LoadImageTask();
        loadImageTask.execute();
    }
    private String convertDateToString(long date){
        Date myDate = new Date(date*1000L);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd 'Thg' MM, yyyy");
        String strDate = dateFormat.format(myDate);
        return strDate;
    }
    public class LoadImageTask extends AsyncTask<Void,HashMap<String,ArrayList<Image>>,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Uri collection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            String[] projection = new String[] {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.RESOLUTION,
                    MediaStore.Images.Media.RELATIVE_PATH,
                    MediaStore.Images.Media.DATA,

            };
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " ASC";

            try (Cursor cursor = getApplication().getContentResolver().query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder
            )) {
                // Cache column indices.
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dateColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                int dataAddedColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int bucketDisplayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int resolutionColumn  = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RESOLUTION);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH);
                int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    int date = cursor.getInt(dateColumn);
                    if(date == 0){
                        date =cursor.getInt(dataAddedColumn);
                    }
                    String strDate =convertDateToString((long)date);
                    int size = cursor.getInt(sizeColumn);
                    String resolution = cursor.getString(resolutionColumn);
                    String path = cursor.getString(pathColumn);
                    String bucketDisplayName = cursor.getString(bucketDisplayNameColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    String data =cursor.getString(dataColumn);
                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    Image newImage = new Image(contentUri, name, date, size,resolution,path,bucketDisplayName,data);

                    if(secretStringSet.contains(newImage.getData())){
                        if(!tempListSecretImage.contains(newImage)){
                            tempListSecretImage.add(newImage);
                        }
                        continue;
                    }
                    Log.e("TAG", "doInBackground: "+newImage );
                    if(!tempListAllImage.contains(newImage)){
                        tempListAllImage.add(newImage);
                    }
                    if(tempImagesByDate.containsKey(strDate)){
                        ArrayList<Image> tempList = new ArrayList<>();
                        tempList = tempImagesByDate.get(strDate);
                        if(tempList.contains(newImage)){
                            continue;
                        }
                        else{
                            Image.getAllNewImage().add(newImage);
                        }
                        tempList.add(newImage);
                        tempImagesByDate.put(strDate,tempList);
                    }
                    else{
                        ArrayList<Image> tempList = new ArrayList<>();
                        tempList.add(newImage);
                        tempImagesByDate.put(strDate,tempList);
                    }
                    if(tempImagesByAlbum.containsKey(bucketDisplayName)){
                        ArrayList<Image> tempList = new ArrayList<>();
                        tempList = tempImagesByAlbum.get(bucketDisplayName);
                        if(tempList.contains(newImage)){
                            continue;
                        }
                        tempList.add(newImage);
                        tempImagesByAlbum.put(bucketDisplayName,tempList);
                    }
                    else{
                        ArrayList<Image> tempList = new ArrayList<>();
                        tempList.add(newImage);
                        tempImagesByAlbum.put(bucketDisplayName,tempList);
                    }
                    publishProgress(tempImagesByDate,tempImagesByAlbum);
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(HashMap<String,ArrayList<Image>>... values) {
            super.onProgressUpdate(values);
            if(listImageByAlbum!=null){
                listImageByAlbum.setValue(values[1]);
            }
            if(listImageByDate !=null){
                listImageByDate.setValue(values[0]);
            }
            if(listAllImage !=null){
                listAllImage.setValue(tempListAllImage);
            }
        }
    }

}
