package com.eightblocksaway.android.practicepronunciation.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhraseInputFragment extends Fragment implements TextToSpeech.OnInitListener
{

    private static final int TTS_CHECK_CODE = 1;
    private static final int SPEECH_RECOGNITION_CODE = 2;
    private static final String LOG_TAG = "PhraseInputFragment";
    private static final String RECORDING_URI = "RECORDING_URI";
    private static final String PREVIOUS_INPUT = "PREVIOUS_INPUT";
    private static final String PRONUNCIATION_LABEL_VALUE = "PRONUNCIATION_LABEL_VALUE";
    private static final String PHRASE_IS_SAVED = "PHRASE_IS_SAVED";
    private TextToSpeech mTts;

    @InjectView(R.id.listen_button) ImageButton listenButton;
    @InjectView(R.id.speak_button) ImageButton speakButton;
    @InjectView(R.id.play_button) ImageButton playButton;
    @InjectView(R.id.add_button) ImageButton addButton;
    @InjectView(R.id.remove_button) ImageButton removeButton;
    @InjectView(R.id.clear_edit_text) ImageButton clearEditText;
    @InjectView(R.id.edit_text) EditText editText;
    @InjectView(R.id.pronunciation_alphabet_label) TextView pronunciationAlphabetLabel;

    @NotNull private String previousInput = "";
    private boolean speechRecognitionInitialized = false;

    private boolean ttsInitialized = false;
    private boolean ignoreEvents = false;
    private PhraseDataHandler phraseDataHandler;
    private Phrase currentPhrase;
    private Callback callback;

    public static final long DELAY = 1000;
    private MediaPlayer mp;
    private boolean mpPrepared = false;
    private Uri recordingUri;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            String previousInput = savedInstanceState.getString(PREVIOUS_INPUT);
            if(previousInput != null){
                this.previousInput = previousInput;
                if(!previousInput.equals("")){
                    String pronunciation = savedInstanceState.getString(PRONUNCIATION_LABEL_VALUE);
                    pronunciationAlphabetLabel.setText(pronunciation);
                    pronunciationAlphabetLabel.setVisibility(View.VISIBLE);

                    boolean isPersisted = savedInstanceState.getBoolean(PHRASE_IS_SAVED);
                    setPersistedButtonState(isPersisted);

                    enableButtons();
                }
            }

            String uriString = savedInstanceState.getString(RECORDING_URI);
            if(uriString != null){
                recordingUri = Uri.parse(uriString);
                enablePlayRecording(recordingUri);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.phrase_input_fragment, container, false);
        ButterKnife.inject(this, rootView);

        phraseDataHandler = new PhraseDataHandler((PhraseFetchAsyncTask.Callback) getActivity());

        listenButton.setEnabled(false);
        speakButton.setEnabled(false);
        playButton.setEnabled(false);
        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setPressed(true);
                v.performClick();
                return true;
            }
        });
        addButton.setEnabled(false);

        enableTTS();
        enableSpeechRecognition();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PREVIOUS_INPUT, previousInput);
        outState.putString(PRONUNCIATION_LABEL_VALUE, pronunciationAlphabetLabel.getText().toString());
        outState.putBoolean(PHRASE_IS_SAVED, removeButton.getVisibility() == View.VISIBLE);

        if(recordingUri != null){
            outState.putString(RECORDING_URI, recordingUri.toString());
        }
    }

    @OnClick(R.id.clear_edit_text)
    void clearText() {
        editText.setText("");
    }

    @OnClick(R.id.remove_button)
    public void removePhrase() {
        final String phrase = getCurrentPhrase();
        getActivity().getContentResolver().delete(PronunciationContract.PhraseEntry.CONTENT_URI, PronunciationProvider.phraseByTextSelector, new String[]{phrase});
        editText.setText("");
    }

    @OnClick(R.id.add_button)
    void addCurrentPhrase() {
        if(currentPhrase != null){
            ContentValues phraseValues = DataUtil.toContentValues(currentPhrase);
            getActivity().getContentResolver().insert(PronunciationContract.PhraseEntry.CONTENT_URI, phraseValues);

            currentPhrase = Phrase.toPersisted(currentPhrase);
            callback.onPhraseAdded(currentPhrase);

            Toast.makeText(getActivity(), getString(R.string.phrase_saved_toast), Toast.LENGTH_SHORT).show();

            //hide soft keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            //change + button to -
            setPersistedButtonState(true);
        } else {
            Log.e(LOG_TAG, "Illegal state. Current phrase is null");
        }
    }

    @OnTextChanged(value = R.id.edit_text, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {
        final String currentInput = s.toString().trim();

        if(!previousInput.equals(currentInput)){
            previousInput = currentInput;

            pronunciationAlphabetLabel.setVisibility(View.INVISIBLE);
            pronunciationAlphabetLabel.setText("");
            currentPhrase = null;
            //change remove button back to +
            setPersistedButtonState(false);
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
                        setPersistedButtonState(true);

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

    public void setPhraseText(@NotNull String phrase, boolean ignoreEvents){
        if(!getCurrentPhrase().equals(phrase)){
            this.ignoreEvents = ignoreEvents;
            editText.setText(phrase);
        }
    }

    public void setPhrase(@NotNull Phrase phrase) {
        currentPhrase = phrase;
        if(isVisible()){
            //set pronunciation
            setPhraseText(phrase.getPhrase(), false);

            FragmentActivity ctx = getActivity();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
            String pronunciationDict = sharedPref.getString(ctx.getString(R.string.pronunciation_dictionary_key), ctx.getString(R.string.ahd_key));
            String pronunciation = "";
            if(pronunciationDict.equals(ctx.getString(R.string.ipa_key))){
                pronunciation = phrase.getIpaPronunciation();
            }

            if(TextUtils.isEmpty(pronunciation)) {
                pronunciation = phrase.getAhdPronunciation();
            }

            pronunciationAlphabetLabel.setText(Html.fromHtml(pronunciation));
            pronunciationAlphabetLabel.setVisibility(View.VISIBLE);

            //enable add button
            addButton.setEnabled(true);
        }

    }

    private String getCurrentPhrase() {
        return editText.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);

        if(mTts != null){
            Log.d(LOG_TAG, "Shutting down TTS Engine");
            mTts.shutdown();
        }

        if(mp != null){
            mp.release();
        }
    }

    private void setPersistedButtonState(boolean isPersisted) {
        if(isPersisted){
            addButton.setEnabled(false);
            addButton.setVisibility(View.GONE);
            removeButton.setEnabled(true);
            removeButton.setVisibility(View.VISIBLE);
        } else {
            addButton.setEnabled(true);
            addButton.setVisibility(View.VISIBLE);
            removeButton.setEnabled(false);
            removeButton.setVisibility(View.GONE);
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
        disablePlayRecording();
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
                    // secret parameters that when added provide audio url in the result
                    intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
                    intent.putExtra("android.speech.extra.GET_AUDIO", true);
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
        final FragmentActivity ctx = getActivity();
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(ctx, this);
                if(Build.VERSION.SDK_INT >= 15){
                    mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            ctx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listenButton.setPressed(true);
                                }
                            });
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            ctx.runOnUiThread(new Runnable() {
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
                    Toast.makeText(ctx, R.string.TTS_no_available, Toast.LENGTH_SHORT).show();
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

            // Enable play recording
            Uri audioUri = data.getData();
            enablePlayRecording(audioUri);

            //TODO this should be done in the background
            ContentValues values = new ContentValues();
            values.put(PronunciationContract.PhraseEntry.COLUMN_MASTERY_LEVEL, result.getScore());
            ctx.getContentResolver().update(PronunciationContract.PhraseEntry.CONTENT_URI,
                    values, PronunciationProvider.phraseByTextSelector, new String[]{phrase});

            @SuppressLint("InflateParams")
            View toastRoot = ctx.getLayoutInflater().inflate(R.layout.recognition_result_toast_layout, null);

            TextView resultTextView = ButterKnife.findById(toastRoot, R.id.result);
            resultTextView.setText(result.getDisplayText());

            ImageView resultIcon = ButterKnife.findById(toastRoot, R.id.result_icon);
            Drawable drawable = null;
            switch (result){
                case EXCELLENT:
                    drawable = ContextCompat.getDrawable(ctx, R.drawable.excellent);
                    break;
                case GOOD:
                    drawable = ContextCompat.getDrawable(ctx, R.drawable.good);
                    break;
                case TRY_AGAIN:
                    drawable = ContextCompat.getDrawable(ctx, R.drawable.try_again);
                    break;
            }

            resultIcon.setImageDrawable(drawable);

            Toast toast = new Toast(ctx);
            toast.setView(toastRoot);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 128);
            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void enablePlayRecording(@NotNull Uri audioUri) {
        final FragmentActivity ctx = getActivity();
        recordingUri = audioUri;
        try {
            mp = new MediaPlayer();
            mpPrepared = false;
            mp.setDataSource(ctx, audioUri);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer arg0) {
                    mpPrepared = true;
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setPressed(false);
                }
            });
            mp.prepareAsync();
            playButton.setEnabled(true);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not load audio uri", e);
        }
    }

    private void disablePlayRecording() {
        playButton.setEnabled(false);
        if(mp != null){
            mp.release();
            mp = null;
            mpPrepared = false;
            recordingUri = null;
        }
    }

    @OnClick(R.id.play_button)
    public void playRecording(ImageButton button){
        //TODO handle click while it's playing
        if(mp != null && mpPrepared) {
            playButton.setPressed(true);
            mp.start();
        }
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
        void onEmptyText();

        void onPhraseFromDB(@NotNull Phrase phrase);

        void onPhraseAdded(@NotNull Phrase phrase);
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
