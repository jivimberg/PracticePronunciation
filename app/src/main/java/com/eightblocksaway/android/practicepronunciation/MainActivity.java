package com.eightblocksaway.android.practicepronunciation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eightblocksaway.android.practicepronunciation.data.PhrasesCursorAdapter;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationProvider;
import com.eightblocksaway.android.practicepronunciation.model.PronunciationRecognitionResult;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
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
        private ImageButton removeButton;
        private ImageButton clearEditText;
        private EditText editText;
        private ListView phraseList;
        private PhrasesCursorAdapter phrasesCursorAdapter;
        private boolean speechRecognitionInitialized = false;
        private boolean ttsInitialized = false;
        private TextView pronunciationAlphabetLabel;
        private Handler pronunciationAlphabetHandler;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

            pronunciationAlphabetLabel = (TextView) rootView.findViewById(R.id.pronunciation_alphabet_label);

            pronunciationAlphabetHandler = new PronunciationHandler(this, pronunciationAlphabetLabel);

            editText = (EditText) rootView.findViewById(R.id.editText);
            editText.addTextChangedListener(new TextWatcher() {
                private final long DELAY = 1000; // in ms
                public static final int TRIGGER_SEARCH = 1;
                public String previousPhrase = "";

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    final String phrase = s.toString().trim();

                    if(!previousPhrase.equals(phrase)){
                        pronunciationAlphabetLabel.setVisibility(View.INVISIBLE);
                        //change remove button back to +
                        removeButton.setVisibility(View.GONE);
                        addButton.setVisibility(View.VISIBLE);
                    }
                    previousPhrase = phrase;

                    if(phrase.length() == 0){
                        dissableButtons();
                    } else {
                        enableButtons();

                        Cursor cursor = null;
                        try{
                            cursor = getActivity().getContentResolver().query(PhraseEntry.CONTENT_URI,
                                    new String[]{PhraseEntry.COLUMN_PRONUNCIATION},
                                    PronunciationProvider.phraseByTextSelector,
                                    new String[]{phrase},
                                    null);

                            if(cursor.moveToFirst()){
                                //word from DB
                                String string = cursor.getString(0);
                                updatePronunciationLabel(string);

                                //change add button for remove button
                                addButton.setVisibility(View.GONE);
                                removeButton.setVisibility(View.VISIBLE);
                            } else {
                                //word not on DB
                                pronunciationAlphabetHandler.removeMessages(TRIGGER_SEARCH);
                                Message newMessage = Message.obtain();
                                newMessage.what = TRIGGER_SEARCH;
                                newMessage.obj = phrase;
                                pronunciationAlphabetHandler.sendMessageDelayed(newMessage, DELAY);
                            }
                        } finally {
                            if(cursor != null && !cursor.isClosed())
                                cursor.close();
                        }
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
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    editText.setText(cursor.getString(cursor.getColumnIndex(PhraseEntry.COLUMN_TEXT)));
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
                    ContentValues phraseValues = new ContentValues();
                    phraseValues.put(PhraseEntry.COLUMN_TEXT, getCurrentPhrase());
                    phraseValues.put(PhraseEntry.COLUMN_MASTERY_LEVEL, 0);
                    String pronunciation = pronunciationAlphabetLabel.getText().toString().trim();
                    if(!TextUtils.isEmpty(pronunciation)){
                        phraseValues.put(PhraseEntry.COLUMN_PRONUNCIATION, pronunciation);
                    }

                    getActivity().getContentResolver().insert(PhraseEntry.CONTENT_URI, phraseValues);

                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            });

            removeButton = (ImageButton) rootView.findViewById(R.id.remove_button);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String phrase = getCurrentPhrase();
                    getActivity().getContentResolver().delete(PhraseEntry.CONTENT_URI, PronunciationProvider.phraseByTextSelector, new String[]{phrase});
                    editText.setText("");
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

        private String getCurrentPhrase() {
            return editText.getText().toString().trim();
        }

        private void updatePronunciationLabel(String string) {
            if(!TextUtils.isEmpty(string)){
                pronunciationAlphabetLabel.setText(string);
                pronunciationAlphabetLabel.setVisibility(View.VISIBLE);
            }
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

                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say \"" + getCurrentPhrase() + "\"" );
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
                    if(Build.VERSION.SDK_INT >= 15){
                        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listenButton.setPressed(true);
                                    }
                                });
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listenButton.setPressed(false);
                                    }
                                });
                            }

                            @Override
                            public void onError(String utteranceId) {}
                        });
                    }

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

                View toastRoot = getActivity().getLayoutInflater().inflate(R.layout.recognition_result_toast_layout, null);

                TextView resultTextView = (TextView) toastRoot.findViewById(R.id.result);
                resultTextView.setText(result.getDisplayText());

                ImageView icon = (ImageView) toastRoot.findViewById(R.id.icon);
                Drawable drawable = null;
                switch (result){
                    case EXCELLENT:
                        drawable = getActivity().getResources().getDrawable(R.drawable.excellent);
                        break;
                    case GOOD:
                        drawable = getActivity().getResources().getDrawable(R.drawable.good);
                        break;
                    case TRY_AGAIN:
                        drawable = getActivity().getResources().getDrawable(R.drawable.try_again);
                        break;
                }

                icon.setImageDrawable(drawable);

                Toast toast = new Toast(getActivity());
                toast.setView(toastRoot);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 128);
                toast.show();
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
                    final String phrase = getCurrentPhrase();
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

        static class PronunciationHandler extends Handler {
            private final WeakReference<MainFragment> weakReference;
            private final TextView pronunciationAlphabetTextView;

            PronunciationHandler(@NotNull MainFragment fragment, @NotNull TextView pronunciationAlphabetTextView) {
                super(Looper.getMainLooper());
                weakReference = new WeakReference<>(fragment);
                this.pronunciationAlphabetTextView = pronunciationAlphabetTextView;
            }

            @Override
            public void handleMessage(Message msg)
            {
                MainFragment fragment = weakReference.get();
                if (fragment != null) {
                    if (msg.obj instanceof String) {
                        String phrase = (String) msg.obj;
                        if(!TextUtils.isEmpty(phrase))
                            new PronunciationAlphabetAsyncTask(pronunciationAlphabetTextView).execute(phrase);
                    }
                }
            }
        }
    }
}
