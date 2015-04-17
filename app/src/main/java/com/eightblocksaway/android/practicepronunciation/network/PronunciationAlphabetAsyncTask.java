package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PronunciationAlphabetAsyncTask extends AsyncTask<String, Void, String>{

    private static final String BASE_URI = "http://api.wordnik.com:80/v4/word.json/";
    private static final String LOG_TAG = "PronunciationAlphabetAsyncTask";
    private final TextView textView;

    public PronunciationAlphabetAsyncTask(@NotNull TextView textView){
        this.textView = textView;
    }

    @Override
    protected String doInBackground(String... params) {
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String phrase = params[0].toLowerCase();
        Log.i(LOG_TAG, "Executing search for phrase " + phrase);

        try{
            Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                    .appendPath(phrase)
                    .appendPath("pronunciations")
                    .appendQueryParameter("useCanonical", "false")
                    .appendQueryParameter("typeFormat", "ahd")
                    .appendQueryParameter("limit", "50")
                    .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5")
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return "";
            }
            return parseResult(buffer.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't get pronunciation", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        textView.setText(result);
        textView.setVisibility(View.VISIBLE);
    }

    private String parseResult(String json) {
        try{
            JSONArray root = new JSONArray(json);
            JSONObject jsonObject = (JSONObject) root.get(0);
            String result = jsonObject.getString("raw");
            Log.i(LOG_TAG, "Returning pronunciation " + result);
            return result;
        }catch (JSONException e){
            Log.e(LOG_TAG, "Couldn't parse response: " + json);
            return "";
        }
    }
}
