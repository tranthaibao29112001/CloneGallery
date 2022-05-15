package com.example.clonegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternLockActivity extends AppCompatActivity {
    private String currentPassword;
    private PatternLockView patternLockView;
    private TextView textView;
    private MyViewModel viewModel;
    private ArrayList<Image> secretImages = new ArrayList<>();
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(patternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            if(currentPassword.equals("")){
                currentPassword = PatternLockUtils.patternToString(patternLockView, pattern);
                SharedPreferences sharedPreferences = getSharedPreferences("SecretAlbum",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Password", currentPassword);
                editor.apply();
                textView.setText("Nhập mật khẩu để vào");
                patternLockView.clearPattern();
            }
            else{
                if(PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase(currentPassword)){
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    Toast.makeText(PatternLockActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PatternLockActivity.this,AlbumActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("album_name","Secret");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    MyViewModel.currentAlbum = "Secret";
                    finish();
                }
                else{
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(PatternLockActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_lock);

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewModel.updateSecretAlbum();

        textView = findViewById(R.id.textView);
        patternLockView =(PatternLockView) findViewById(R.id.pattern_lock_view);

        SharedPreferences sharedPreferences = getSharedPreferences("SecretAlbum",MODE_PRIVATE);
        currentPassword = sharedPreferences.getString("Password","");
        if(currentPassword.equals("")){
            textView.setText("Đăng nhập lần đầu, nhập mật khẩu mới của bạn");
        }
        else{
            textView.setText("Nhập nhật khẩu để vào");
        }

        patternLockView.addPatternLockListener(mPatternLockViewListener);

    }
}