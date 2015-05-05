package com.eightblocksaway.android.practicepronunciation.view;

import com.eightblocksaway.android.practicepronunciation.model.Phrase;

import org.jetbrains.annotations.NotNull;

/**
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */
public interface PhraseSelectCallback {
    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onPhraseSelected(@NotNull Phrase phrase);
}
