package com.phonegap.gui;

import com.phonegap.*;
import android.os.Bundle;

public class TryTryTryActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
    }
}
