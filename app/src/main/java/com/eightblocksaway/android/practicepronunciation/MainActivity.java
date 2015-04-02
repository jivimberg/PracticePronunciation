package com.eightblocksaway.android.practicepronunciation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eightblocksaway.android.practicepronunciation.data.PhrasesCursorAdapter;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.model.PronunciationRecognitionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.*;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainFragment extends Fragment implements TextToSpeech.OnInitListener, LoaderManager.LoaderCallbacks<Cursor>
    {

        private static final int TTS_CHECK_CODE = 1;
        private static final int SPEECH_RECOGNITION_CODE = 2;
        private static final int LOADER_ID = 1;
        private TextToSpeech mTts;

        private ImageButton listenButton;
        private ImageButton speakButton;
        private ImageButton addButton;
        private EditText editText;
        private ListView phraseList;
        private PhrasesCursorAdapter phrasesCursorAdapter;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

            editText = (EditText) rootView.findViewById(R.id.editText);

            phraseList = (ListView) rootView.findViewById(R.id.phrase_list);
            phrasesCursorAdapter = new PhrasesCursorAdapter(getActivity(), R.layout.phrase_list_item, null, 0);
            phraseList.setAdapter(phrasesCursorAdapter);
            phraseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView phraseText = (TextView) view.findViewById(R.id.phrase_text);
                    editText.setText(phraseText.getText());
                }
            });

            listenButton = (ImageButton) rootView.findViewById(R.id.listen_button);
            listenButton.setEnabled(false);

            speakButton = (ImageButton) rootView.findViewById(R.id.speak_button);
            speakButton.setEnabled(false);

            addButton = (ImageButton) rootView.findViewById(R.id.add_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String phrase = editText.getText().toString().trim();

                    ContentValues phraseValues = new ContentValues();
                    phraseValues.put(PhraseEntry.COLUMN_TEXT, phrase);

                    getActivity().getContentResolver().insert(PhraseEntry.CONTENT_URI, phraseValues);

                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            });

            enableTTS();
            enableSpeechRecognition();

            return rootView;
        }

        private void enableSpeechRecognition() {
            // Disable button if no recognition service is present
            PackageManager pm = getActivity().getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(
                    new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() != 0)
            {
                speakButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String phrase = editText.getText().toString().trim();

                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say \"" + phrase + "\"" );
                        startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
                    }
                });

                speakButton.setEnabled(true);
            }
        }

        private void enableTTS() {
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, TTS_CHECK_CODE);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // Handle TTS Check
            if (requestCode == TTS_CHECK_CODE) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // success, create the TTS instance
                    mTts = new TextToSpeech(getActivity(), this);
                } else {
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
            }

            // Handle speech recognition result
            if (requestCode == SPEECH_RECOGNITION_CODE && resultCode == RESULT_OK)
            {
                final String phrase = editText.getText().toString().trim();
                // Populate the wordsList with the String values the recognition engine thought it heard
                ArrayList<String> matches = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);

                PronunciationRecognitionResult result = PronunciationRecognitionResult.evaluate(phrase, matches);
                Log.i(PronunciationRecognitionResult.LOG_TAG, "Pronunciation recognition result: " + result);
                int today = Time.getJulianDay(System.currentTimeMillis(), new Time().gmtoff);

                //persist result
                ContentValues attemptValues = new ContentValues();
                attemptValues.put(AttemptEntry.COLUMN_DATE, today);
                attemptValues.put(AttemptEntry.COLUMN_RESULT_ID, result.name());
                getActivity().getContentResolver().insert(AttemptEntry.buildAttemptWithPhrase(phrase), attemptValues);

                //TODO improve this
                Toast.makeText(getActivity(), result.name(), Toast.LENGTH_SHORT).show();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onInit(int status) {
            //hardcoded language
            mTts.setLanguage(Locale.US);

            listenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String phrase = editText.getText().toString().trim();
                    if(Build.VERSION.SDK_INT >= 21){
                        mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, Integer.toString(phrase.hashCode()));
                    } else {
                        mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });

            listenButton.setEnabled(true);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = PhraseEntry.CONTENT_URI;
            return new CursorLoader(getActivity(), uri, null, null, null, PhraseEntry._ID + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            phrasesCursorAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            phrasesCursorAdapter.swapCursor(null);
        }
    }
}
