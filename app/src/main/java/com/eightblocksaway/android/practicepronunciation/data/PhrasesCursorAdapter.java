package com.eightblocksaway.android.practicepronunciation.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.PronunciationRecognitionResult;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PhrasesCursorAdapter extends ResourceCursorAdapter {

    @InjectView(R.id.phrase_text) TextView phraseText;
    @InjectView(R.id.phrase_pronunciation) TextView phrasePronunciation;
    @InjectView(R.id.mastery_level) TextView masteryLevelTextView;
    @InjectView(R.id.mastery_level_progress_bar) ProgressBar progressBar;

    public PhrasesCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ButterKnife.inject(this, view);
        view.setBackgroundColor(context.getResources().getColor(R.color.app_fg));

        int columnTextIndex = cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_TEXT);
        String text = cursor.getString(columnTextIndex);
        phraseText.setText(text);

        String pronunciation = cursor.getString(cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_PRONUNCIATION));
        phrasePronunciation.setText("");
        if(!TextUtils.isEmpty(pronunciation)){
            phrasePronunciation.setText(pronunciation);
        }

        int columnMasteryLevelIndex = cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_MASTERY_LEVEL);
        int masteryLevel = cursor.getInt(columnMasteryLevelIndex);

        int maxScore = context.getResources().getInteger(R.integer.max_points);
        masteryLevelTextView.setText(masteryLevel + "/" + maxScore);

        progressBar.setProgress(masteryLevel);
    }
}
