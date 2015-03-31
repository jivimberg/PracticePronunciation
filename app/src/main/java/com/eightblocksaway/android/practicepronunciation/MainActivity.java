package com.eightblocksaway.android.practicepronunciation;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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
        private TextToSpeech mTts;

        private ImageButton listenButton;
        private EditText editText;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            listenButton = (ImageButton) rootView.findViewById(R.id.listen_button);
            editText = (EditText) rootView.findViewById(R.id.editText);

            //TODO later...
            checkTTSAvailability();

            return rootView;
        }

        private void checkTTSAvailability() {
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
        }

        @Override
        public void onInit(int status) {
            //hardcoded language
            mTts.setLanguage(Locale.US);

            listenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String phrase = editText.getText().toString();
                    mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, Integer.toString(phrase.hashCode()));
                }
            });

            listenButton.setEnabled(true);
        }
    }
}
