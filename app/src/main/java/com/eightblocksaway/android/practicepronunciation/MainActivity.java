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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.eightblocksaway.android.practicepronunciation.data.PronunciationProvider;
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
        private static final String LOG_TAG = "MainFrame";
        private TextToSpeech mTts;

        private ImageButton listenButton;
        private ImageButton speakButton;
        private ImageButton addButton;
        private ImageButton clearEditText;
        private EditText editText;
        private ListView phraseList;
        private PhrasesCursorAdapter phrasesCursorAdapter;
        private boolean speechRecognitionInitialized = false;
        private boolean ttsInitialized = false;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

            editText = (EditText) rootView.findViewById(R.id.editText);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().trim().length() == 0){
                        dissableButtons();
                    } else {
                        enableButtons();
                    }
                }
            });

            clearEditText = (ImageButton) rootView.findViewById(R.id.clear_editText);
            clearEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setText("");
                }
            });

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
            addButton.setEnabled(false);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String phrase = editText.getText().toString().trim();

                    ContentValues phraseValues = new ContentValues();
                    phraseValues.put(PhraseEntry.COLUMN_TEXT, phrase);
                    phraseValues.put(PhraseEntry.COLUMN_MASTERY_LEVEL, 0);

                    getActivity().getContentResolver().insert(PhraseEntry.CONTENT_URI, phraseValues);

                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            });

            enableTTS();
            enableSpeechRecognition();

            //Starting from intent
            Intent receivedIntent = getActivity().getIntent();
            String receivedAction = receivedIntent.getAction();
            if(receivedAction.equals(Intent.ACTION_SEND)){
                String receivedType = receivedIntent.getType();
                if(receivedType.startsWith("text/plain")){
                    String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
                    editText.setText(receivedText);
                }
            }

            return rootView;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if(mTts != null){
                Log.d(LOG_TAG, "Shutting down TTS Engine");
                mTts.shutdown();
            }
        }


        private void enableButtons() {
            if(ttsInitialized)
                listenButton.setEnabled(true);
            if(speechRecognitionInitialized)
                speakButton.setEnabled(true);
            addButton.setEnabled(true);
            clearEditText.setEnabled(true);
            clearEditText.setVisibility(View.VISIBLE);
        }

        private void dissableButtons() {
            listenButton.setEnabled(false);
            speakButton.setEnabled(false);
            addButton.setEnabled(false);
            clearEditText.setEnabled(false);
            clearEditText.setVisibility(View.INVISIBLE);
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

                speechRecognitionInitialized = true;

                if(!editText.getText().toString().isEmpty()){
                    speakButton.setEnabled(true);
                }
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

                    if(!editText.getText().toString().isEmpty()){
                        listenButton.setEnabled(true);
                    }
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

                //persist result
                /*
                int today = Time.getJulianDay(System.currentTimeMillis(), new Time().gmtoff);
                ContentValues attemptValues = new ContentValues();
                attemptValues.put(AttemptEntry.COLUMN_DATE, today);
                attemptValues.put(AttemptEntry.COLUMN_RESULT_ID, result.name());
                getActivity().getContentResolver().insert(AttemptEntry.buildAttemptWithPhrase(phrase), attemptValues);
                 */

                //TODO this should be done in the background

                ContentValues attemptValues = new ContentValues();
                attemptValues.put(PhraseEntry.COLUMN_MASTERY_LEVEL, result.getScore());
                getActivity().getContentResolver().update(PhraseEntry.CONTENT_URI,
                        attemptValues, PronunciationProvider.phraseByTextSelector, new String[]{phrase});

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

            ttsInitialized = true;
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
