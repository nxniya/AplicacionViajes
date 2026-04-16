package com.example.aplicacionviajes;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FilterActivityTest {

    @Rule
    public ActivityScenarioRule<FilterActivity> testRule =
            new ActivityScenarioRule<>(FilterActivity.class);

    @Test
    public void cityFilterEditTextIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.etCityFilter))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void typingCityUpdatesEditText() {
        Espresso.onView(ViewMatchers.withId(R.id.etCityFilter))
                .perform(ViewActions.typeText("Madrid"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.etCityFilter))
                .check(ViewAssertions.matches(ViewMatchers.withText("Madrid")));
    }

    @Test
    public void priceRangeTextViewIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.tvPriceRange))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void soloFavoritosCheckboxIsUncheckedByDefault() {
        Espresso.onView(ViewMatchers.withId(R.id.cbSoloFavoritos))
                .perform(TestUtils.nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isNotChecked()));
    }

    @Test
    public void checkingSoloFavoritosCheckboxTogglesIt() {
        Espresso.onView(ViewMatchers.withId(R.id.cbSoloFavoritos))
                .perform(TestUtils.nestedScrollTo(), ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.cbSoloFavoritos))
                .check(ViewAssertions.matches(ViewMatchers.isChecked()));
    }

    @Test
    public void applyFilterButtonIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.btnApplyFilter))
                .perform(TestUtils.nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void typingCityAndApplyingFilterClosesActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.etCityFilter))
                .perform(ViewActions.typeText("Barcelona"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.btnApplyFilter))
                .perform(TestUtils.nestedScrollTo(), ViewActions.click());
    }
}
