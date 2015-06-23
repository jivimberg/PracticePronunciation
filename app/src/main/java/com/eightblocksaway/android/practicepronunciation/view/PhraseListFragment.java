package com.eightblocksaway.android.practicepronunciation.view;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.data.DataUtil;
import com.eightblocksaway.android.practicepronunciation.data.PhrasesCursorAdapter;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationProvider;
import com.eightblocksaway.android.practicepronunciation.data.SwipeToDeleteCursorWrapper;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhraseListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final int LOADER_ID = 1;
    private static final String LOG_TAG = "PhraseListFragment";

    private PhrasesCursorAdapter phrasesCursorAdapter;
    private PhraseSelectCallback callback;

    @InjectView(R.id.phrase_list) DynamicListView phraseList;
    @InjectView(R.id.empty_list_placeholder) View emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.phrase_list_fragment, container, false);
        ButterKnife.inject(this, rootView);

        phraseList.setEmptyView(emptyView);
        phraseList.enableSwipeToDismiss(
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            //To avoid flickering
                            Cursor cursor = phrasesCursorAdapter.getCursor();
                            SwipeToDeleteCursorWrapper cursorWrapper = new SwipeToDeleteCursorWrapper(cursor, position);
                            phrasesCursorAdapter.swapCursor(cursorWrapper);

                            cursor.moveToPosition(position);
                            String removingPhrase = cursor.getString(cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_TEXT));
                            getActivity().getContentResolver().delete(PronunciationContract.PhraseEntry.CONTENT_URI, PronunciationProvider.phraseByTextSelector, new String[]{removingPhrase});
                        }
                    }
                }
        );
        phrasesCursorAdapter = new PhrasesCursorAdapter(getActivity(), R.layout.phrase_list_item, null, 0);
        phraseList.setAdapter(phrasesCursorAdapter);

        // Needs to be after the phraseCursorAdapter creation
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @OnItemClick(R.id.phrase_list)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // CursorAdapter returns a cursor at the correct position for getItem(), or null
        // if it cannot seek to that position.
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        if (cursor != null) {
            Phrase phrase = DataUtil.fromCursor(cursor);
            Log.d(LOG_TAG, "Clicked item for phrase: " + phrase);
            callback.onPhraseSelected(phrase);
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = PronunciationContract.PhraseEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, null, null, null, PronunciationContract.PhraseEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        phrasesCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        phrasesCursorAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (PhraseSelectCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback");
        }
    }

}
