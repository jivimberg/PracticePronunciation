package com.eightblocksaway.android.practicepronunciation.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.view.PhraseInputFragment;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class PhraseDataHandler extends Handler {
    private final WeakReference<PhraseInputFragment> weakReference;
    private final TextView pronunciationAlphabetTextView;
    public static final int FETCH_DATA = 1;

    public PhraseDataHandler(@NotNull PhraseInputFragment fragment, @NotNull TextView pronunciationAlphabetTextView) {
        super(Looper.getMainLooper());
        weakReference = new WeakReference<>(fragment);
        this.pronunciationAlphabetTextView = pronunciationAlphabetTextView;
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
        PhraseInputFragment fragment = weakReference.get();
        if (fragment != null) {
            if (msg.obj instanceof String) {
                String phrase = (String) msg.obj;
                if(!TextUtils.isEmpty(phrase))
                    new PronunciationAlphabetAsyncTask(pronunciationAlphabetTextView).execute(phrase);
            }
        }
    }
}