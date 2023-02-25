package com.frank.recyclerdemo;

import android.util.Log;

import com.frank.annotation.AutoFactory;
import com.frank.annotation.IAutoFactory;

@AutoFactory(factoryName = "DD", id = "A", returnType = IAutoFactory.class)
public class D implements IAutoFactory {
    private static final String TAG = "A";
    public void sayHello(){
        Log.d(TAG, "A sayHello: ");
    }

    @Override
    public void say() {
        sayHello();
    }
}
