package com.eightblocksaway.android.practicepronunciation;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.eightblocksaway.android.practicepronunciation.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.internal.matchers.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testInitialState() {
        // test input fragment is empty
        onView(withId(R.id.editText)).check(matches(withText("")));
        onView(withId(R.id.editText)).check(matches(withHint(R.string.pronounce_hint)));

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
        String phrase = "hello";
        onView(withId(R.id.editText)).perform(typeText(phrase)).check(matches(withText(phrase)));

        /**
         * Need to add this delay for the AsyncTask to commence :(
         */
        Thread.sleep(1200);

        // check that the fragment is actually displayed
        onView(withId(R.id.phraseText)).check(matches(isDisplayed()));
        onView(withId(R.id.phraseText)).check(matches(withText(phrase)));

        // check hyphenation
        onView(withId(R.id.hyphenationList)).check(matches(hasDescendant(withText("hel"))));
        onView(withId(R.id.hyphenationList)).check(matches(hasDescendant(withText("lo"))));

        // check definition
        String phraseDefinition = "Used to greet someone, answer the telephone, or express surprise.";
        onView(withId(R.id.definitionList)).check(matches(hasDescendant(withText(containsString(phraseDefinition)))));
    }

    @Test
    public void testBackAfterSearch() throws InterruptedException {
        testSearchNavigation();

        // Check that we are seeing the detail view
        onView(withId(R.id.phraseText)).check(matches(isDisplayed()));

        pressBack();

        onView(withId(R.id.phraseText)).check(doesNotExist());
        onView(withId(R.id.phrase_list_fragment)).check(matches(isDisplayed()));
    }

}