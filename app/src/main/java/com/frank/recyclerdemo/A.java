package com.frank.recyclerdemo;

import android.util.Log;

import com.frank.annotation.AutoFactory;
import com.frank.annotation.IAutoFactory;

@AutoFactory(factoryName = "CarFactory", id = "A", returnType = IAutoFactory.class)
public class A implements IAutoFactory {
    private static final String TAG = "A";
    public void sayHello(){
        Log.d(TAG, "A sayHello: ");
    }

    @Override
    public void say() {
        sayHello();
    }
}
