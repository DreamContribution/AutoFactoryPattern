package com.frank.recyclerdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.frank.factory.CarFactory;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // CarFactory.build("A")
        CarFactory.build("A").say();
        CarFactory.build("B").say();

    }

}