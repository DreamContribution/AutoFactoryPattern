package com.frank.recyclerdemo;


import android.util.Log;

import com.frank.annotation.AutoFactory;
import com.frank.annotation.IAutoFactory;

@AutoFactory(factoryName = "CarFactory", id = "B", returnType = IAutoFactory.class)
public class B implements IAutoFactory {
    private static final String TAG = "B";
    public void sayFuck() {
        Log.d(TAG, "B sayFuck: ");
    }

    @Override
    public void say() {
        sayFuck();
    }
}
