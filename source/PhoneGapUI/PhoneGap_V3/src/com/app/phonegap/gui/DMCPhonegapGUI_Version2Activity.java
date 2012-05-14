package com.app.phonegap.gui;

import com.phonegap.*;
import android.os.Bundle;

public class DMCPhonegapGUI_Version2Activity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
    }
}
