package com.eightblocksaway.android.practicepronunciation;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.view.MainActivity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {

    private final String IPA_PHRASE_PRONUNCIATION = "hɛˈləʊ";
    private final String AHD_PHRASE_PRONUNCIATION = "(hĕ-lōˈ, hə-)";
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private static final String PHRASE = "hello";

    @SuppressLint("CommitPrefEdits")
    @Before
    public void cleanup(){
        //clean all phrases
        MainActivity ctx = mActivityRule.getActivity();
        ctx.getContentResolver().delete(PronunciationContract.PhraseEntry.CONTENT_URI, null, null);

        //cleanup user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putString(ctx.getString(R.string.pronunciation_dictionary_key), ctx.getString(R.string.ahd_key)).commit();
    }

    @Test
    public void testInitialState() {
        // test input fragment is empty
        onView(withId(R.id.edit_text)).check(matches(withText("")));
        onView(withId(R.id.edit_text)).check(matches(withHint(R.string.pronounce_hint)));

        // check buttons are disabled
        onView(withId(R.id.listen_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.speak_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.play_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.add_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.remove_button)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // check the list is displayed
        onView(withId(R.id.phrase_list_fragment)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchNavigation() throws InterruptedException {
        onView(withId(R.id.edit_text)).perform(typeText(PHRASE));

        /**
         * Need to add this delay for the AsyncTask to commence :(
         */
        Thread.sleep(1200);
        checkDetailView(false, R.string.ahd);
    }

    @Test
    public void testSearchAndRotate() throws InterruptedException {
        onView(withId(R.id.edit_text)).perform(typeText(PHRASE));

        /**
         * Need to add this delay for the AsyncTask to commence :(
         */
        Thread.sleep(1200);
        checkDetailView(false, R.string.ahd);

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());

        /**
         * Need to add this delay for the AsyncTask to commence :(
         */
        Thread.sleep(1200);
        checkDetailView(false, R.string.ahd);
    }

    /**
     * Want to test that no ANR is thrown
     */
    @Test
    public void testSearchRotateWithoutWaiting() throws InterruptedException {
        onView(withId(R.id.edit_text)).perform(typeText(PHRASE));
        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());

        // detail view will be empty because we are not giving it time to load
        onView(withId(R.id.phrase_list_fragment)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchWordNotFound() throws InterruptedException {
        onView(withId(R.id.edit_text)).perform(typeText("lololo"));

        /**
         * Need to add this delay for the AsyncTask to commence :(
         */
        Thread.sleep(1200);

        onView(withId(R.id.error_icon)).check(matches(isDisplayed()));
        onView(withId(R.id.error_text)).check(matches(isDisplayed()));
        String wordNotFoundText = mActivityRule.getActivity().getResources().getString(R.string.word_not_found_text);
        onView(withId(R.id.error_text)).check(matches(withText(wordNotFoundText)));
    }

    @Test
    public void testClear() throws InterruptedException {
        testSearchNavigation();

        onView(withId(R.id.clear_edit_text)).perform(click());

        testInitialState();
    }

    @Test
    public void testAdd() throws InterruptedException {
        testSearchNavigation();

        onView(withId(R.id.add_button)).perform(click());

        // check + is hidden and - is visible
        onView(withId(R.id.add_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.remove_button)).check(matches(isDisplayed()));

        onView(withId(R.id.detail_points_label)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_points_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_points_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_points_text)).check(matches(isDisplayed()));
        int maxPoints = mActivityRule.getActivity().getResources().getInteger(R.integer.max_points);
        onView(withId(R.id.detail_points_text)).check(matches(withText(containsString("0/" + maxPoints))));

        // go back
        pressBack();

        // check list
        onView(withId(R.id.phrase_list)).check(matches(isDisplayed()));
        onView(withId(R.id.phrase_list)).check(matches(hasDescendant(withText(containsString(PHRASE)))));
    }

    @Test
    public void changePronunciationDict() throws InterruptedException {
        testAdd();

        changeDictionary(R.string.ipa);

        onView(withId(R.id.phrase_list)).check(matches(hasDescendant(withText(containsString(IPA_PHRASE_PRONUNCIATION)))));
    }

    @Test
    public void changePronunciationDictSearch() throws InterruptedException {
        changeDictionary(R.string.ipa);

        onView(withId(R.id.edit_text)).perform(typeText(PHRASE));

        /**
         * Need to add this delay for the AsyncTask to commence :(
         */
        Thread.sleep(1200);

        checkDetailView(false, R.string.ipa);
    }

    private void changeDictionary(int dictionary) {
        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.action_settings)).perform(click());
        onView(withText(R.string.pronunciation_preference_title)).perform(click());
        onView(withText(dictionary)).perform(click());

        // go back
        pressBack();
    }


    /**
     * Want to test that no ANR is thrown
     */
    @Test
    public void testAddSearchAndRotateWithoutWaiting() throws InterruptedException {
        testSearchNavigation();

        onView(withId(R.id.add_button)).perform(click());

        onView(withId(R.id.clear_edit_text)).perform(click());

        // search for the same phrase again
        onView(withId(R.id.edit_text)).perform(typeText(PHRASE));

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());
        checkDetailView(true, R.string.ahd);
    }


    @Test
    public void testClickItem() throws InterruptedException {
        testAdd();

        //noinspection unchecked
        onView(withText(containsString(PHRASE))).perform(click());

        checkDetailView(true, R.string.ahd);
    }

    @Test
    public void testRemove() throws InterruptedException {
        testAdd();

        //noinspection unchecked
        onView(withText(containsString(PHRASE))).perform(click());

        onView(withId(R.id.remove_button)).perform(click());

        // Check that we are in the list fragment and there is no item with PHRASE
        onView(withId(R.id.phrase_list)).check(matches(not(hasDescendant(withText(containsString(PHRASE))))));
    }

    @Test
    public void testSwipeToRemove() throws InterruptedException {
        testAdd();

        //noinspection unchecked
        onView(withText(containsString(PHRASE))).perform(swipeRight());

        // Check that we are in the list fragment and there is no item with PHRASE
        onView(withId(R.id.phrase_list)).check(matches(not(hasDescendant(withText(containsString(PHRASE))))));
    }

    @Test
    public void testBackAfterSearch() throws InterruptedException {
        testSearchNavigation();

        // Check that we are seeing the detail view
        onView(withId(R.id.detail_phrase_text)).check(matches(isDisplayed()));

        pressBack();

        onView(withId(R.id.detail_phrase_text)).check(doesNotExist());
        onView(withId(R.id.phrase_list_fragment)).check(matches(isDisplayed()));
    }

    private void checkDetailView(boolean phraseAdded, int dictionary) {
        // check buttons become enabled
        onView(withId(R.id.listen_button)).check(matches(isEnabled()));

        if(dictionary == R.string.ipa){
            onView(withId(R.id.pronunciation_alphabet_label)).check(matches(withText(IPA_PHRASE_PRONUNCIATION)));
        } else {
            onView(withId(R.id.pronunciation_alphabet_label)).check(matches(withText(AHD_PHRASE_PRONUNCIATION)));
        }

        onView(withId(R.id.edit_text)).check(matches(withText(PHRASE)));

        /**
         * No voice recognition support on the emulator
         */
        //onView(withId(R.id.speak_button)).check(matches(isEnabled()));

        // check add button
        if(phraseAdded){
            onView(withId(R.id.remove_button)).check(matches(isEnabled()));
            onView(withId(R.id.add_button)).check(matches(not(isDisplayed())));
        } else {
            onView(withId(R.id.add_button)).check(matches(isEnabled()));
            onView(withId(R.id.remove_button)).check(matches(not(isDisplayed())));
        }

        // check that the fragment is actually displayed
        onView(withId(R.id.detail_phrase_text)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_phrase_text)).check(matches(withText(PHRASE)));

        // check hyphenation
        onView(withId(R.id.hyphenation_list)).check(matches(hasDescendant(withText("hel"))));
        onView(withId(R.id.hyphenation_list)).check(matches(hasDescendant(withText("lo"))));

        // check definition
        String phraseDefinition = "Used to greet someone, answer the telephone, or express surprise.";
        onView(withId(R.id.definition_list)).check(matches(hasDescendant(withText(containsString(phraseDefinition)))));
    }
}