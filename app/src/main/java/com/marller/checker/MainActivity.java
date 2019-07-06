package com.marller.checker;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.marller.api.OnDenied;
import com.marller.api.OnNeed;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermission.onRequestPermissionResult(this, requestCode, grantResults);
    }

    @OnNeed(values = {Manifest.permission.CAMERA})
    public void showCamera() {

    }

    @OnDenied(values = {Manifest.permission.CAMERA})
    public void showCameraDenied() {

    }
}
