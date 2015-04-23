package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class FetchCommand<T> {
    private static final String LOG_TAG = "FetchCommand";
    protected static final String BASE_URI = "http://api.wordnik.com:80/v4/word.json/";
    private final Uri uri;
    private final String phrase;

    public FetchCommand(Uri uri, String phrase){
        this.uri = uri;
        this.phrase = phrase;
    }

    public T fetchData() throws IOException, JSONException, EmptyResponseException {
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        Log.i(LOG_TAG, "Executing search for normalized phrase " + phrase);

        try{
            URL url = new URL(uri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                throw new IOException("Couldn't get data. InputStream == null");
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
                throw new IOException("Couldn't get data. buffer is empty");
            }
            return parseResult(buffer.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't get data", e);
            throw e;
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
    }

    private T parseResult(@NotNull String json) throws JSONException, EmptyResponseException {
        JSONArray root = new JSONArray(json);
        if(root.length() <= 0){
            throw new EmptyResponseException();
        } else {
            return doParseResult(json);
        }
    }

    protected abstract T doParseResult(String json) throws JSONException;

    public static class EmptyResponseException extends Exception {}
}