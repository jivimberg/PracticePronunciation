package com.eightblocksaway.android.practicepronunciation.network;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class PronunciationAlphabetAsyncTask extends AsyncTask<String, Void, String>{

    private static final String LOG_TAG = "PronunciationAlphabetAsyncTask";
    private final TextView textView;

    public PronunciationAlphabetAsyncTask(@NotNull TextView textView){
        this.textView = textView;
    }

    @Override
    protected String doInBackground(String... params) {
        return FetchPronunciation.create(params[0]).fetchData();
    }

    @Override
    protected void onPostExecute(String result) {
        //TODO we'll need to work out how to handle the result
        super.onPostExecute(result);
        textView.setText(result);
        textView.setVisibility(View.VISIBLE);
    }
}
