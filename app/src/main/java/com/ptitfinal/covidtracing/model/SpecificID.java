package com.ptitfinal.covidtracing.model;

import android.content.Context;

public class SpecificID {
    public static final String genID(Context context){
        String macAddress =
                android.provider.Settings.Secure.getString(context.getApplicationContext().getContentResolver(), "android_id");
        return macAddress;
    }
}
