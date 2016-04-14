package com.sumod.bluetoothpoc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_second)
public class SecondActivity extends AppCompatActivity {

    @ViewById(R.id.device_name)
    TextView deviceName;

    @ViewById(R.id.device_address)
    TextView deviceAddress;

    @AfterViews
    protected void afterViews(){
        Intent i = getIntent();

        deviceName.setText(i.getStringExtra("deviceName"));
        deviceAddress.setText(i.getStringExtra("deviceAddress"));
    }

}
