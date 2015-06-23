package com.eightblocksaway.android.practicepronunciation;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.view.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.internal.matchers.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private static final String PHRASE = "hello";

    @Before
    public void cleanup(){
        //clean all phrases
        mActivityRule.getActivity().getContentResolver().delete(PronunciationContract.PhraseEntry.CONTENT_URI, null, null);
    }

    @Test
    public void testInitialState() {
        // test input fragment is empty
        onView(withId(R.id.edit_text)).check(matches(withText("")));
        onView(withId(R.id.edit_text)).check(matches(withHint(R.string.pronounce_hint)));

        // check buttons are disabled
        onView(withId(R.id.listen_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.speak_button)).check(matches(not(isEnabled())));
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
        checkDetailView();
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
    public void testClickItem() throws InterruptedException {
        testAdd();

        //noinspection unchecked
        onView(withText(containsString(PHRASE))).perform(click());

        checkDetailView();
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

    private void checkDetailView() {
        // check buttons become enabled
        onView(withId(R.id.listen_button)).check(matches(isEnabled()));

        /**
         * No voice recognition support on the emulator
         */
        //onView(withId(R.id.speak_button)).check(matches(isEnabled()));

        // check add button
        onView(withId(R.id.add_button)).check(matches(isEnabled()));

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