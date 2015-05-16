package com.eightblocksaway.android.practicepronunciation.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.network.AsyncTaskResult;
import com.eightblocksaway.android.practicepronunciation.network.FetchCommand;
import com.eightblocksaway.android.practicepronunciation.network.PhraseFetchAsyncTask;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class MainActivity extends ActionBarActivity
        implements PhraseSelectCallback, PhraseFetchAsyncTask.Callback, PhraseInputFragment.Callback {

    public static final String LOG_TAG = "MainActivity";
    private PhraseInputFragment phraseInputFragment;
    private int detailFragmentContainerId;
    private DetailFragment detailFragment;
    private boolean isPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phraseInputFragment = (PhraseInputFragment) getSupportFragmentManager().findFragmentById(R.id.phrase_input_fragment);

        if(findViewById(R.id.multi_fragment_container) != null){
            //phone
            isPhone = true;
            detailFragmentContainerId = R.id.multi_fragment_container;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.multi_fragment_container, new PhraseListFragment())
                        .commit();
            }
        } else {
            //tablet
            isPhone = false;
            detailFragmentContainerId = R.id.detail_fragment_container;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String receivedAction = intent.getAction();
        if(receivedAction.equals(Intent.ACTION_SEND)){
            String receivedType = intent.getType();
            if(receivedType.startsWith("text/plain")){
                String receivedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                phraseInputFragment.setPhraseText(receivedText);
            }
        }
    }

    @Override
    public void onPhraseSelected(@NotNull Phrase phrase) {
        Log.i(LOG_TAG, "Phrase selected from list: " + phrase);
        phraseInputFragment.setPhrase(phrase);
    }

    @Override
    public void onPhraseFetch(@NotNull AsyncTaskResult<Phrase> asyncTaskResult){
        hideKeyboard();

        if(asyncTaskResult.wasSuccessfull()) {
            Phrase phrase = asyncTaskResult.getResult();
            Log.i(LOG_TAG, "Receiving successful fetch for phrase: " + phrase);
            phraseInputFragment.setPhrase(phrase);

            detailFragment = DetailFragment.newInstance(phrase);
            getSupportFragmentManager().beginTransaction()
                    .replace(detailFragmentContainerId, detailFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            Exception e = asyncTaskResult.getError();

            if(e instanceof IOException) {
                // possible network error
                Log.i(LOG_TAG, "Network error");
                getSupportFragmentManager().beginTransaction()
                        .replace(detailFragmentContainerId, ErrorFragments.NO_WIFI.getFragmentInstance(), ErrorFragments.NO_WIFI.name())
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            } else if (e instanceof FetchCommand.EmptyResponseException) {
                // word not found
                Log.i(LOG_TAG, "Word not found");
                getSupportFragmentManager().beginTransaction()
                        .replace(detailFragmentContainerId, ErrorFragments.WORD_NOT_FOUND.getFragmentInstance(), ErrorFragments.WORD_NOT_FOUND.name())
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            } else {
                // parser exception and others
                Log.e(LOG_TAG, "Parser exception", e);
                getSupportFragmentManager().beginTransaction()
                        .replace(detailFragmentContainerId, ErrorFragments.WORD_NOT_FOUND.getFragmentInstance(), ErrorFragments.WORD_NOT_FOUND.name())
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onEmptyText() {
        if(isPhone){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.multi_fragment_container, new PhraseListFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onPhraseFromDB(@NotNull Phrase phrase) {
        onPhraseSelected(phrase);

        //Add detail fragment
        detailFragment = DetailFragment.newInstance(phrase);
        getSupportFragmentManager().beginTransaction()
                .replace(detailFragmentContainerId, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        phraseInputFragment.setPhraseText("");
    }
}
