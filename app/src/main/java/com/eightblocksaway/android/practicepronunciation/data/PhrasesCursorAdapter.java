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

public class PhrasesCursorAdapter extends ResourceCursorAdapter {

    public PhrasesCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setBackgroundColor(context.getResources().getColor(R.color.list_item_color));
        TextView phraseText = (TextView) view.findViewById(R.id.phrase_text);
        int columnTextIndex = cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_TEXT);
        String text = cursor.getString(columnTextIndex);
        phraseText.setText(text);

        TextView phrasePronunciation = (TextView) view.findViewById(R.id.phrase_pronunciation);
        String pronunciation = cursor.getString(cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_PRONUNCIATION));
        if(!TextUtils.isEmpty(pronunciation)){
            phrasePronunciation.setText(pronunciation);
        }

        int columnMasteryLevelIndex = cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_MASTERY_LEVEL);
        int masteryLevel = cursor.getInt(columnMasteryLevelIndex);

        TextView masteryLevelTextView = (TextView) view.findViewById(R.id.mastery_level);
        masteryLevelTextView.setText(masteryLevel + "/" + PronunciationRecognitionResult.MAX_SCORE);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.mastery_level_progress_bar);
        progressBar.setProgress(masteryLevel);
    }
}
