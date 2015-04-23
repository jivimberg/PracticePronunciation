package com.eightblocksaway.android.practicepronunciation.view;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.network.AsyncTaskResult;
import com.eightblocksaway.android.practicepronunciation.network.FetchCommand;
import com.eightblocksaway.android.practicepronunciation.network.PhraseFetchAsyncTask;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements PhraseListFragment.Callback, PhraseFetchAsyncTask.Callback {

    public static final String LOG_TAG = "MainActivity";
    public static final String ERROR_NO_NETWORK_TAG = "ERROR_NO_NETWORK";
    private PhraseInputFragment phraseInputFragment;
    private int detailFragmentContainerId;
    private DetailFragment detailFragment;
    private boolean isPhone = true;
    private DetailErrorFragment noWifiErrorFragment;

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
                        .add(R.id.multi_fragment_container, new PhraseListFragment())
                        .commit();
            }
        } else {
            //tablet
            isPhone = false;
            detailFragmentContainerId = R.id.detail_fragment_container;
        }

        noWifiErrorFragment = DetailErrorFragment.newInstance(R.drawable.ic_device_signal_wifi_off);

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
                phraseInputFragment.setPhrase(receivedText);
            }
        }
    }

    @Override
    public void onPhraseSelected(String phrase) {
        phraseInputFragment.setPhrase(phrase);

        detailFragment = new DetailFragment();
        //TODO implement
        //fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(detailFragmentContainerId, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPhraseFetch(@NotNull AsyncTaskResult<Phrase> asyncTaskResult){

        if(asyncTaskResult.wasSuccessfull()) {
            Phrase phrase = asyncTaskResult.getResult();
            //set pronunciation
            phraseInputFragment.setPronunciation(phrase.getPronunciation());

            //set detail fragment
            if (detailFragment != null && detailFragment.isVisible()) {
                detailFragment.setPhrase(phrase);
            }
        } else {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            Exception e = asyncTaskResult.getError();

            if(e instanceof IOException) {
                // possible network error
                Log.i(LOG_TAG, "Network error");
                getSupportFragmentManager().beginTransaction()
                        .replace(detailFragmentContainerId, ErrorFragments.NO_WIFI.getFragmentInstance(), ErrorFragments.NO_WIFI.name())
                        .commit();
            } else if (e instanceof FetchCommand.EmptyResponseException) {
                // word not found
                Log.i(LOG_TAG, "Word not found");
                getSupportFragmentManager().beginTransaction()
                        .replace(detailFragmentContainerId, ErrorFragments.WORD_NOT_FOUND.getFragmentInstance(), ErrorFragments.WORD_NOT_FOUND.name())
                        .commit();
            } else {
                // parser exception and others
                Log.e(LOG_TAG, "Parser exception", e);
                getSupportFragmentManager().beginTransaction()
                        .replace(detailFragmentContainerId, ErrorFragments.WORD_NOT_FOUND.getFragmentInstance(), ErrorFragments.WORD_NOT_FOUND.name())
                        .commit();
            }
        }
    }
}
