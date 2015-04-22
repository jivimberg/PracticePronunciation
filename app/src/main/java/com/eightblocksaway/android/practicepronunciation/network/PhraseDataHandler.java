package com.eightblocksaway.android.practicepronunciation.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class PhraseDataHandler extends Handler {
    private final WeakReference<PhraseFetchAsyncTask.Callback> weakReference;
    public static final int FETCH_DATA = 1;

    public PhraseDataHandler(@NotNull PhraseFetchAsyncTask.Callback callback) {
        super(Looper.getMainLooper());
        weakReference = new WeakReference<>(callback);
    }

    public void triggerFetch(String phrase, long delay){
        removeMessages(FETCH_DATA);
        Message newMessage = Message.obtain();
        newMessage.what = FETCH_DATA;
        newMessage.obj = phrase;
        sendMessageDelayed(newMessage, delay);
    }

    @Override
    public void handleMessage(Message msg)
    {
        PhraseFetchAsyncTask.Callback callback = weakReference.get();
        if (callback != null) {
            if (msg.obj instanceof String) {
                String phrase = (String) msg.obj;
                if(!TextUtils.isEmpty(phrase))
                    new PhraseFetchAsyncTask(callback).execute(phrase);
            }
        }
    }
}