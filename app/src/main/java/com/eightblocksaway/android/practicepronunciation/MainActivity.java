package com.eightblocksaway.android.practicepronunciation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


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
    public static class MainFragment extends Fragment implements TextToSpeech.OnInitListener {

        private static final int TTS_CHECK_CODE = 1;
        private static final int SPEECH_RECOGNITION_CODE = 2;
        private TextToSpeech mTts;

        private ImageButton listenButton;
        private ImageButton speakButton;
        private EditText editText;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            listenButton = (ImageButton) rootView.findViewById(R.id.listen_button);
            listenButton.setEnabled(false);

            speakButton = (ImageButton) rootView.findViewById(R.id.speak_button);
            speakButton.setEnabled(false);

            editText = (EditText) rootView.findViewById(R.id.editText);

            //TODO later...
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
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
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

        public void onActivityResult(
                int requestCode, int resultCode, Intent data) {
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

            if (requestCode == SPEECH_RECOGNITION_CODE && resultCode == RESULT_OK)
            {
                // Populate the wordsList with the String values the recognition engine thought it heard
                ArrayList<String> matches = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                Log.i("Speech Recognition", matches.toString());
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
                    final String phrase = editText.getText().toString();
                    if(Build.VERSION.SDK_INT >= 21){
                        mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, Integer.toString(phrase.hashCode()));
                    } else {
                        mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });

            listenButton.setEnabled(true);
        }
    }
}
