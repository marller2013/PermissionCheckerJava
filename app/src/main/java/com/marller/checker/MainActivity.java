package com.marller.checker;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.marller.api.OnDenied;
import com.marller.api.OnNeed;
import com.marller.api.OnNeverAsk;


public class MainActivity extends AppCompatActivity {

    private Button showCamera;
    private Button showLocation;
    private Button showFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        showCamera = findViewById(R.id.showCamera);
        showCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityPermission.showCamera(MainActivity.this);
            }
        });
        showLocation = findViewById(R.id.showLocation);
        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivityPermission.showLocation(MainActivity.this);
            }
        });
        showFragment = findViewById(R.id.showFragment);
        showFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FragActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermission.onRequestPermissionResult(this, requestCode, grantResults);
    }

    @OnNeed(values = {Manifest.permission.CAMERA})
    public void showCamera() {
        Toast.makeText(this, "camera 权限申请成功", Toast.LENGTH_LONG).show();
    }

    @OnDenied(values = {Manifest.permission.CAMERA})
    public void showCameraDenied() {
        Toast.makeText(this, "camera 权限被拒绝", Toast.LENGTH_LONG).show();
    }

    @OnNeverAsk(values = {Manifest.permission.CAMERA})
    public void onNeverAskCarmera() {
        Toast.makeText(this, "camera 权限不再询问", Toast.LENGTH_LONG).show();
    }

//    @OnNeed(values ={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
//    public void showLocation(){
//        Toast.makeText(this,"定位权限申请成功",Toast.LENGTH_LONG).show();
//    }
//    @OnDenied(values = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
//    public void showLocationDenied(){
//        Toast.makeText(this,"定位权限被拒绝",Toast.LENGTH_LONG).show();
//    }
//
//    @OnNeverAsk(values = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
//    public void onNeverAskLocation(){
//        Toast.makeText(this,"定位权限不再询问",Toast.LENGTH_LONG).show();
//    }
}
