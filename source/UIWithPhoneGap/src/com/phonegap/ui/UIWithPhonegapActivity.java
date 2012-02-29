package com.phonegap.ui;

import com.phonegap.*;
import android.os.Bundle;

public class UIWithPhonegapActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/views/devices-view/devices-view.html");
    }
}
