package com.btrack.uhfscanner;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginResult;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.Logger;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

@CapacitorPlugin(name="UHFScannerPlugin")
public class UHFScannerPlugin extends Plugin implements KeyEventCallback {
    private static final String LOG_TAG = "UHFScanner";
    private PluginCall mCallbackContext = null;
    private boolean loopFlag = true;

    private TagThread tagThread = null;

    private CacheThread cacheThread = null;

    private RFIDWithUHFUART mReader;


    private final ConcurrentMap<String, Long> map = new ConcurrentHashMap<>();

    @PluginMethod()
    public String echo(String value) {
        return value;
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void execute( PluginCall callbackContext) {
        String action = callbackContext.getString("action");
        int power = callbackContext.getInt("power");
        if (action.equals("start")) {
            mReader.setPower(power);
            this.mCallbackContext = callbackContext;
            loopFlag = true;

            mReader.startInventoryTag();

            tagThread = new TagThread();
            tagThread.start();

            cacheThread = new CacheThread();
            cacheThread.start();
            callbackContext.setKeepAlive(true);
            JSObject obj = new JSObject();
            obj.put("result", "started");
            obj.put("value", "");
            PluginResult result = new PluginResult(obj);
            callbackContext.resolve(obj);
        } else if (action.equals("stop")) {
            loopFlag = false;
            mReader.stopInventory();
            PluginResult result = new PluginResult(new JSObject());
            result.put("result","stopped");
            callbackContext.setKeepAlive(false);
            callbackContext.successCallback(result);
        }
    }


    @Override
    public void load() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
            mReader.init();
            initSound();
        } catch (Exception ex) {
            return;
        }
    }


    public void onDestroy() {
        loopFlag = false;
        if (mReader != null) {
            mReader.free();
        }
    }

    public void onReset() {
        loopFlag = false;
        if (mReader != null) {
            mReader.free();
        }
    }

    private JSObject wrapObject(String epc) {
        JSObject obj = new JSObject();
        try {
            obj.put("result", "scanned");
            obj.put("value", epc);
            playSound(1);
        } catch (Exception e) {
            Logger.error(LOG_TAG, e.getMessage(), e);
        }
        return obj;
    }

    private void sendUpdate(JSObject info, boolean keepCallback) {
        if (this.mCallbackContext != null) {
            this.mCallbackContext.setKeepAlive(keepCallback);
            this.mCallbackContext.resolve(info);
        }
    }



    @Override
    public void onKeyDown(int keyCode) {
        if (keyCode == 1)
        {

        }
    }

    class TagThread extends Thread {
        public void run() {
            while (loopFlag) {
                UHFTAGInfo res = mReader.readTagFromBuffer();
                if (res != null) {
                    String strEpc = res.getEPC();

                    if (!map.containsKey(strEpc)) {
                        map.put(strEpc, System.currentTimeMillis());
                        sendUpdate(wrapObject(strEpc), true);
                    }
                }

                try {
                    Thread.sleep(30);
                } catch (Exception e) {
                }
            }
        }
    }

    class CacheThread extends Thread {
        private final int cacheTime = 5 * 1000;

        @Override
        public void run() {
            while (loopFlag) {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> entry = iterator.next();
                    Long time = entry.getValue();
                    if (now - time > cacheTime) {
                        iterator.remove();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }
    }


    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;
    public static final int barcodebeep=0x7f050000;
    public static final int serror=0x7f050001;
    private void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this.getActivity(), R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this.getActivity(), R.raw.serror, 1));
        am = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
    }

    public void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio,
                    volumnRatio,
                    1,
                    0,
                    1
            );
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

