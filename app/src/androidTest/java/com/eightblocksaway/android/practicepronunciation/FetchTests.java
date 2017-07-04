package com.eightblocksaway.android.practicepronunciation;

import com.eightblocksaway.android.practicepronunciation.network.FetchAHDPronunciation;
import com.eightblocksaway.android.practicepronunciation.network.FetchCommand;
import com.eightblocksaway.android.practicepronunciation.network.PronunciationTypeFormat;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class FetchTests {

    private static final String PHRASE = "pronunciation";

    @Test
    public void testFetchPronunciations() throws JSONException, FetchCommand.EmptyResponseException, IOException {
        Map<PronunciationTypeFormat, String> expected = new HashMap<>();
        expected.put(PronunciationTypeFormat.AHD, "(prə-nŭnˌsē-āˈshən)");
        expected.put(PronunciationTypeFormat.ARPABET, "P R OW0 N AH2 N S IY0 EY1 SH AH0 N");

        Map<PronunciationTypeFormat, String> actual = FetchAHDPronunciation.create(PHRASE).fetchData();
        assertEquals(expected, actual);
    }
}
