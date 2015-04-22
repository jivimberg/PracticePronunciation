package com.eightblocksaway.android.practicepronunciation.network;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.model.Definition;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;
import com.eightblocksaway.android.practicepronunciation.view.PhraseListFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhraseFetchAsyncTask extends AsyncTask<String, Void, Phrase>{

    private static final String LOG_TAG = "PronunciationAlphabetAsyncTask";
    private final PhraseFetchAsyncTask.Callback  callback;

    public PhraseFetchAsyncTask(@NotNull PhraseFetchAsyncTask.Callback callback){
        this.callback = callback;
    }

    @Override
    protected Phrase doInBackground(String... params) {
        String phrase = params[0];
        String pronunciation = FetchPronunciation.create(phrase).fetchData();
        List<Definition> definitions = FetchDefinitions.create(phrase).fetchData();
        List<Syllable> hyphenation = FetchHyphenation.create(phrase).fetchData();

        return new Phrase(phrase, pronunciation, definitions, hyphenation);
    }

    @Override
    protected void onPostExecute(Phrase result) {
        super.onPostExecute(result);

        callback.onPhraseFetch(result);
    }

    public interface Callback {
        public void onPhraseFetch(Phrase phrase);
    }
}
