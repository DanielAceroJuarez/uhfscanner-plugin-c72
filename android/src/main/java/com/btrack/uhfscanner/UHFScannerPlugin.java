package com.btrack.uhfscanner;

import android.util.Log;

public class UHFScannerPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
