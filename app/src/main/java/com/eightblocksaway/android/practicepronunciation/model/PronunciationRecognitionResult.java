package com.eightblocksaway.android.practicepronunciation.model;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public enum PronunciationRecognitionResult {
    EXCELLENT(1),
    GOOD(0),
    TRY_AGAIN(-1);

    public static final String LOG_TAG = "Speech Recognition";
    private int score;

    private PronunciationRecognitionResult(int score){
        this.score = score;
    }

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

    public int getScore() {
        return score;
    }
}
