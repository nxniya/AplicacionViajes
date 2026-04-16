package com.example.aplicacionviajes;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.aplicacionviajes.entity.Trip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DetailActivityTest {

    private static final String TRIP_TITLE = "Aventura en Roma";
    private static final String TRIP_CITY  = "Roma";

    private ActivityScenario<DetailActivity> scenario;

    @Before
    public void setUp() {
        Trip trip = new Trip(
                TRIP_TITLE, "ROM001", TRIP_CITY, 450.0,
                System.currentTimeMillis(),
                "https://example.com/roma.jpg"
        );
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                DetailActivity.class
        );
        intent.putExtra("trip", trip);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void tripTitleIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.detailTitle))
                .check(ViewAssertions.matches(ViewMatchers.withText(TRIP_TITLE)));
    }

    @Test
    public void tripCityIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.detailCiudad))
                .check(ViewAssertions.matches(ViewMatchers.withText("Ciudad: " + TRIP_CITY)));
    }

    @Test
    public void tripPriceIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.detailPrecio))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void tripDateIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.detailFecha))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void favoriteCheckboxIsUncheckedByDefault() {
        Espresso.onView(ViewMatchers.withId(R.id.checkFavorite))
                .perform(TestUtils.nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isNotChecked()));
    }

    @Test
    public void checkingFavoriteCheckboxMarksAsFavorite() {
        Espresso.onView(ViewMatchers.withId(R.id.checkFavorite))
                .perform(TestUtils.nestedScrollTo(), ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.checkFavorite))
                .check(ViewAssertions.matches(ViewMatchers.isChecked()));
    }

    @Test
    public void uncheckingFavoriteCheckboxRemovesMark() {
        Espresso.onView(ViewMatchers.withId(R.id.checkFavorite))
                .perform(TestUtils.nestedScrollTo(), ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.checkFavorite))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.checkFavorite))
                .check(ViewAssertions.matches(ViewMatchers.isNotChecked()));
    }

    @Test
    public void buyButtonIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.btnBuy))
                .perform(TestUtils.nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void clickingBuyButtonShowsSnackbar() {
        Espresso.onView(ViewMatchers.withId(R.id.btnBuy))
                .perform(TestUtils.nestedScrollTo(), ViewActions.click());
        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("¡Que tengas un buen viaje en " + TRIP_CITY + "!")));
    }
}
