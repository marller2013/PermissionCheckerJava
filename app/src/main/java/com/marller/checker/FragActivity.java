package com.marller.checker;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.appcompat.app.AppCompatActivity;

public class FragActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_frag);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentRoot, BlankFragment.newInstance("", "")).commit();
    }
}
