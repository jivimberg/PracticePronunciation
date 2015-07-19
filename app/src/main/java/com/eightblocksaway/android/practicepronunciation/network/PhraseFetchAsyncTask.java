package com.eightblocksaway.android.practicepronunciation.network;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.model.Definition;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;
import com.eightblocksaway.android.practicepronunciation.view.PhraseListFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class PhraseFetchAsyncTask extends AsyncTask<String, Void, AsyncTaskResult<Phrase>>{

    private static final String LOG_TAG = "PronunciationAlphabetAsyncTask";
    private final PhraseFetchAsyncTask.Callback  callback;

    public PhraseFetchAsyncTask(@NotNull PhraseFetchAsyncTask.Callback callback){
        this.callback = callback;
    }

    @Override
    protected AsyncTaskResult<Phrase> doInBackground(String... params) {
        String phrase = params[0];
        try {
            String pronunciation = FetchPronunciation.create(phrase).fetchData();
            List<Definition> definitions = FetchDefinitions.create(phrase).fetchData();
            List<Syllable> hyphenation = FetchHyphenation.create(phrase).fetchData();

            return new AsyncTaskResult<>(Phrase.createNotPersisted(phrase, pronunciation, definitions, hyphenation));
        } catch (IOException | JSONException | FetchCommand.EmptyResponseException e) {
            return new AsyncTaskResult<>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<Phrase> result) {
        super.onPostExecute(result);

        callback.onPhraseFetch(result);
    }

    public interface Callback {
        void onPhraseFetch(AsyncTaskResult<Phrase> phrase);
    }
}
