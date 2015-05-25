package com.eightblocksaway.android.practicepronunciation.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.data.DataUtil;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationProvider;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.PronunciationRecognitionResult;
import com.eightblocksaway.android.practicepronunciation.network.PhraseDataHandler;
import com.eightblocksaway.android.practicepronunciation.network.PhraseFetchAsyncTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhraseInputFragment extends Fragment implements TextToSpeech.OnInitListener
{

    private static final int TTS_CHECK_CODE = 1;
    private static final int SPEECH_RECOGNITION_CODE = 2;
    private static final String LOG_TAG = "PhraseInputFragment";
    private TextToSpeech mTts;

    private ImageButton listenButton;
    private ImageButton speakButton;
    private ImageButton addButton;
    private ImageButton removeButton;
    private ImageButton clearEditText;
    private EditText editText;
    private String previousInput = "";

    private boolean speechRecognitionInitialized = false;
    private boolean ttsInitialized = false;
    private boolean ignoreEvents = false;
    private TextView pronunciationAlphabetLabel;
    private PhraseDataHandler phraseDataHandler;
    private Phrase currentPhrase;
    private Callback callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.phrase_input_fragment, container, false);

        pronunciationAlphabetLabel = (TextView) rootView.findViewById(R.id.pronunciation_alphabet_label);
        editText = (EditText) rootView.findViewById(R.id.editText);

        phraseDataHandler = new PhraseDataHandler((PhraseFetchAsyncTask.Callback) getActivity());

        editText = (EditText) rootView.findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            private final long DELAY = 1000; // in ms

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                final String currentInput = s.toString().trim();

                if(!previousInput.equals(currentInput)){
                    previousInput = currentInput;

                    pronunciationAlphabetLabel.setVisibility(View.INVISIBLE);
                    pronunciationAlphabetLabel.setText("");
                    currentPhrase = null;
                    //change remove button back to +
                    removeButton.setVisibility(View.GONE);
                    addButton.setVisibility(View.VISIBLE);
                    dissableButtons();

                    if(currentInput.length() == 0){
                        if(!ignoreEvents) callback.onEmptyText();
                    } else {
                        enableButtons();

                        //TODO this should be moved out of here
                        Cursor cursor = null;
                        try{
                            cursor = getActivity().getContentResolver().query(PronunciationContract.PhraseEntry.CONTENT_URI,
                                    null,
                                    PronunciationProvider.phraseByTextSelector,
                                    new String[]{currentInput},
                                    null);

                            if(cursor.moveToFirst()){
                                //word from DB
                                Phrase phraseFromDB = DataUtil.fromCursor(cursor);

                                //change add button for remove button
                                addButton.setVisibility(View.GONE);
                                removeButton.setVisibility(View.VISIBLE);

                                //Cancel delayed lookups
                                if(!ignoreEvents) {
                                    phraseDataHandler.removeMessages();
                                    callback.onPhraseFromDB(phraseFromDB);
                                }
                            } else {
                                //word not on DB
                                if(!ignoreEvents) phraseDataHandler.triggerFetch(currentInput, DELAY);
                            }
                        } finally {
                            if(cursor != null && !cursor.isClosed())
                                cursor.close();
                        }
                    }
                }

                if(ignoreEvents){
                    ignoreEvents = false;
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

        listenButton = (ImageButton) rootView.findViewById(R.id.listen_button);
        listenButton.setEnabled(false);

        speakButton = (ImageButton) rootView.findViewById(R.id.speak_button);
        speakButton.setEnabled(false);

        addButton = (ImageButton) rootView.findViewById(R.id.add_button);
        addButton.setEnabled(false);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPhrase != null){
                    ContentValues phraseValues = DataUtil.toContentValues(currentPhrase);
                    getActivity().getContentResolver().insert(PronunciationContract.PhraseEntry.CONTENT_URI, phraseValues);

                    Toast.makeText(getActivity(), getString(R.string.phrase_saved_toast), Toast.LENGTH_SHORT).show();

                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    //change add button to -
                    addButton.setVisibility(View.GONE);
                    removeButton.setVisibility(View.VISIBLE);
                } else {
                    Log.e(LOG_TAG, "Illegal state. Current phrase is null");
                }
            }
        });

        removeButton = (ImageButton) rootView.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phrase = getCurrentPhrase();
                getActivity().getContentResolver().delete(PronunciationContract.PhraseEntry.CONTENT_URI, PronunciationProvider.phraseByTextSelector, new String[]{phrase});
                editText.setText("");
            }
        });

        enableTTS();
        enableSpeechRecognition();

        return rootView;
    }

    public void setPhraseText(@NotNull String phrase, boolean ignoreEvents){
        if(!getCurrentPhrase().equals(phrase)){
            this.ignoreEvents = ignoreEvents;
            editText.setText(phrase);
        }
    }

    public void setPhrase(@NotNull Phrase phrase) {
        currentPhrase = phrase;
        //set pronunciation
        setPhraseText(phrase.getPhrase(), false);
        pronunciationAlphabetLabel.setText(Html.fromHtml(phrase.getPronunciation()));
        pronunciationAlphabetLabel.setVisibility(View.VISIBLE);

        //enable add button
        addButton.setEnabled(true);
    }

    private String getCurrentPhrase() {
        return editText.getText().toString().trim();
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
        try{
            startActivityForResult(checkIntent, TTS_CHECK_CODE);
        } catch (ActivityNotFoundException e){
            Log.d(LOG_TAG, "No TTS engine on this device", e);
            Toast.makeText(getActivity(), R.string.TTS_no_available, Toast.LENGTH_SHORT).show();
        }
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
                        public void onError(String utteranceId) {
                        }
                    });

                    if(!editText.getText().toString().isEmpty()){
                        listenButton.setEnabled(true);
                    }
                }

            } else {
                try{
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                } catch (ActivityNotFoundException e){
                    Log.d(LOG_TAG, "No TTS engine on this device", e);
                    Toast.makeText(getActivity(), R.string.TTS_no_available, Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Handle speech recognition result
        if (requestCode == SPEECH_RECOGNITION_CODE && resultCode == Activity.RESULT_OK)
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
                ContentValues values = new ContentValues();
                values.put(AttemptEntry.COLUMN_DATE, today);
                values.put(AttemptEntry.COLUMN_RESULT_ID, result.name());
                getActivity().getContentResolver().insert(AttemptEntry.buildAttemptWithPhrase(phrase), values);
                 */

            //TODO this should be done in the background

            ContentValues values = new ContentValues();
            values.put(PronunciationContract.PhraseEntry.COLUMN_MASTERY_LEVEL, result.getScore());
            String pronunciation = pronunciationAlphabetLabel.getText().toString().trim();
            if(!TextUtils.isEmpty(pronunciation)){
                values.put(PronunciationContract.PhraseEntry.COLUMN_PRONUNCIATION, pronunciation);
            }
            getActivity().getContentResolver().update(PronunciationContract.PhraseEntry.CONTENT_URI,
                    values, PronunciationProvider.phraseByTextSelector, new String[]{phrase});

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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onEmptyText();

        void onPhraseFromDB(@NotNull Phrase phrase);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback");
        }
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
}
