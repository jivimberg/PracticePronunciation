package com.eightblocksaway.android.practicepronunciation.view;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.network.PhraseFetchAsyncTask;

import org.jetbrains.annotations.NotNull;


public class MainActivity extends ActionBarActivity implements PhraseListFragment.Callback, PhraseFetchAsyncTask.Callback {

    private PhraseInputFragment phraseInputFragment;
    private int detailFragmentContainerId;
    private DetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phraseInputFragment = (PhraseInputFragment) getSupportFragmentManager().findFragmentById(R.id.phrase_input_fragment);

        if(findViewById(R.id.multi_fragment_container) != null){
            //phone
            detailFragmentContainerId = R.id.multi_fragment_container;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.multi_fragment_container, new PhraseListFragment())
                        .commit();
            }
        } else {
            //tablet
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
    public void onPhraseFetch(@NotNull Phrase phrase){
        //set pronunciation
        phraseInputFragment.setPronunciation(phrase.getPronunciation());

        //set detail fragment
        if(detailFragment != null && detailFragment.isVisible()){
            detailFragment.setPhrase(phrase);
        }
    }
}
