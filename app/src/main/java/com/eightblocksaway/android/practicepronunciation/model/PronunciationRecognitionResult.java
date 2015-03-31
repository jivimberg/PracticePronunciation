package com.eightblocksaway.android.practicepronunciation.model;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public enum PronunciationRecognitionResult {
    EXCELLENT,
    GOOD,
    TRY_AGAIN;

    public static final String LOG_TAG = "Speech Recognition";

    public static PronunciationRecognitionResult evaluate(@NotNull String phrase, @NotNull ArrayList<String> matches){
        Log.i(LOG_TAG, "Expected phrase: " + phrase);
        Log.i(LOG_TAG, "Recognized matches: " + matches);
        if(matches.get(0).equalsIgnoreCase(phrase)){
            return EXCELLENT;
        }

        for (String match : matches) {
            if(match.equalsIgnoreCase(phrase)) {
                return GOOD;
            }
        }

        return TRY_AGAIN;
    }
}
