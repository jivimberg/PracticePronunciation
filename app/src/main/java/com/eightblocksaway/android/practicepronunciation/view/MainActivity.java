package com.eightblocksaway.android.practicepronunciation.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.network.AsyncTaskResult;
import com.eightblocksaway.android.practicepronunciation.network.FetchCommand;
import com.eightblocksaway.android.practicepronunciation.network.PhraseFetchAsyncTask;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity
        implements PhraseSelectCallback, PhraseFetchAsyncTask.Callback, PhraseInputFragment.Callback {

    public static final String LOG_TAG = "MainActivity";
    private PhraseInputFragment phraseInputFragment;
    private int detailFragmentContainerId;
    private DetailFragment detailFragment;
    private boolean isPhone;

    @InjectView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(savedInstanceState != null){
            phraseInputFragment = (PhraseInputFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, "PhraseInputFragment");
        } else {
            phraseInputFragment = (PhraseInputFragment) getSupportFragmentManager().findFragmentById(R.id.phrase_input_fragment);
        }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "PhraseInputFragment", phraseInputFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
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
                phraseInputFragment.setPhraseText(receivedText, false);
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
        // if the activity has already been destroyed do nothing
        if(getSupportFragmentManager().isDestroyed()){
            return;
        }

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
            onBackPressed();
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
                .commitAllowingStateLoss();
    }

    @Override
    public void onPhraseAdded(@NotNull Phrase phrase) {
        if(detailFragment != null){
            detailFragment.onPhraseAdded(phrase);
        }
    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        Log.d(LOG_TAG, "backStackEntryCount: " + backStackEntryCount);
        if(backStackEntryCount > 0){
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            super.onBackPressed();
        }

        phraseInputFragment.setPhraseText("", true);
    }
}
