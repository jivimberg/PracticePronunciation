package com.eightblocksaway.android.practicepronunciation.model;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public enum PronunciationRecognitionResult {
    EXCELLENT("Excellent!", 1),
    GOOD("Very good", 0),
    TRY_AGAIN("Try Again", -1);

    public static final String LOG_TAG = "Speech Recognition";
    private final String displayText;
    private int score;

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 10;

    private PronunciationRecognitionResult(String displayText, int score){
        this.score = score;
        this.displayText = displayText;
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

    public String getDisplayText() {
        return displayText;
    }
}
