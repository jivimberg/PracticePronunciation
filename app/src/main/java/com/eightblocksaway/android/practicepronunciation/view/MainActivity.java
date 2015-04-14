package com.eightblocksaway.android.practicepronunciation.view;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.eightblocksaway.android.practicepronunciation.R;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PhraseInputFragment())
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        PhraseInputFragment phraseInputFragment = (PhraseInputFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        phraseInputFragment.populateViewFromIntent();
    }

}
