package com.eightblocksaway.android.practicepronunciation.network;

import android.os.AsyncTask;

import com.eightblocksaway.android.practicepronunciation.data.ArpabetToIpaConverter;
import com.eightblocksaway.android.practicepronunciation.model.Definition;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
            Map<PronunciationTypeFormat, String> pronunciations = FetchAHDPronunciation.create(phrase).fetchData();
            String ahdPronunciation = pronunciations.get(PronunciationTypeFormat.AHD);
            String ipaPronunciation = getIPAPronunciation(phrase, pronunciations);
            List<Definition> definitions = FetchDefinitions.create(phrase).fetchData();
            List<Syllable> hyphenation = FetchHyphenation.create(phrase).fetchData();

            return new AsyncTaskResult<>(Phrase.createNotPersisted(phrase, ahdPronunciation, ipaPronunciation, definitions, hyphenation));
        } catch (IOException | JSONException | FetchCommand.EmptyResponseException e) {
            return new AsyncTaskResult<>(e);
        }
    }

    private String getIPAPronunciation(String phrase, Map<PronunciationTypeFormat, String> pronunciations) throws JSONException, FetchCommand.EmptyResponseException {
        String ipaPronunciation;
        try {
            ipaPronunciation = FetchPronunciations.create(phrase).fetchData();
        } catch (IOException e) {
            if(pronunciations.containsKey(PronunciationTypeFormat.ARPABET)) {
                ipaPronunciation = ArpabetToIpaConverter.convertToIpa(pronunciations.get(PronunciationTypeFormat.ARPABET));
            } else {
                ipaPronunciation = "";
            }
        }
        return ipaPronunciation;
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
