package com.example.clonegallery;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DetailInfoActivity extends AppCompatActivity {
    private Image image;
    private Toolbar toolbar;
    private ActionBar mActionBar;
    private TextView dateTakenTextView, weekDayTextView, timeTakenTextView,
            imageNameTextView, sizeTextView, resolutionTextview, pathTextview;

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_info);

        toolbar = (Toolbar) findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        dateTakenTextView =findViewById(R.id.dateTaken);
        weekDayTextView = findViewById(R.id.weekDay);
        timeTakenTextView = findViewById(R.id.timeTaken);
        imageNameTextView = findViewById(R.id.imageName);
        sizeTextView = findViewById(R.id.size);
        resolutionTextview =findViewById(R.id.resolution);
        pathTextview = findViewById(R.id.path);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            image = bundle.getParcelable("image");
        }

        dateTakenTextView.setText(convertDateToString(image.getDate()));
        weekDayTextView.setText(getWeek(image.getDate()));
        timeTakenTextView.setText(getHour(image.getDate()));
        imageNameTextView.setText(image.getName());
        resolutionTextview.setText(image.getResolution());
        sizeTextView.setText(String.valueOf(image.getSize()/1000)+"KB");
        pathTextview.setText(image.getData());


    }
    private String getHour(long date){
        Date myDate = new Date(date*1000L);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String strDate = dateFormat.format(myDate);
        return strDate;
    }
    private String convertDateToString(long date){
        Date myDate = new Date(date*1000L);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd 'Tháng' MM, yyyy");
        String strDate = dateFormat.format(myDate);
        return strDate;
    }
    private String getWeek(long date){
        Date myDate = new Date(date*1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        switch (week){
            case 1:{
                return "Chủ nhật";
            }
            case 2:{
                return "Thứ hai";
            }
            case 3:{
                return "Thứ ba";
            }
            case 4:{
                return "Thứ tư";
            }
            case 5:{
                return "Thứ năm";
            }
            case 6:{
                return "Thứ sáu";
            }
            default:{
                return "Thứ bảy";
            }
        }
    }
}