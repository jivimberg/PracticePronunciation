package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.data.DataUtil;
import com.eightblocksaway.android.practicepronunciation.model.Definition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchDefinitions extends FetchCommand<List<Definition>>{

    private static final String LOG_TAG = "FetchPronunciation";

    private FetchDefinitions(Uri uri, String phrase) {
        super(uri, phrase);
    }

    public static FetchDefinitions create(String phrase){
        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(normalizedPhrase)
                .appendPath("definitions")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("limit", "5")
                .appendQueryParameter("api_key", API_KEY)
                .build();
        return new FetchDefinitions(builtUri, phrase);
    }

    @Override
    protected List<Definition> parseResult(String json) throws JSONException, EmptyResponseException {
        JSONArray root = new JSONArray(json);
        if(root.length() <= 0){
            throw new EmptyResponseException();
        }

        List<Definition> result = new ArrayList<>();
        for (int i = 0; i < root.length(); i++) {
            JSONObject syllable = (JSONObject) root.get(i);
            String partOfSpeech = syllable.getString("partOfSpeech");
            String definition = syllable.getString("text");
            result.add(new Definition(definition, partOfSpeech));
        }

        Log.i(LOG_TAG, "Returning definitions " + result);
        return result;
    }
}
