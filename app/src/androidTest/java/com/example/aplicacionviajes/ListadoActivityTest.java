package com.example.aplicacionviajes;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import com.example.aplicacionviajes.entity.Trip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class ListadoActivityTest {

    private ActivityScenario<ListadoActivity> scenario;

    @Before
    public void setUp() {
        ArrayList<Trip> trips = new ArrayList<>();
        trips.add(new Trip("Aventura en Roma", "ROM001", "Roma", 450.0,
                System.currentTimeMillis(), "https://example.com/img1.jpg"));
        trips.add(new Trip("Escapada a París", "PAR002", "París", 620.0,
                System.currentTimeMillis(), "https://example.com/img2.jpg"));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ListadoActivity.class);
        intent.putParcelableArrayListExtra("lista_viajes", trips);
        intent.putExtra("solo_favoritos", false);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void recyclerViewIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.recyclerViewViajes))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void filterButtonIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.btnOpenFilter))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void viewToggleButtonIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonCambiarVista))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void viewToggleButtonSwitchesLayoutMode() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonCambiarVista)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.recyclerViewViajes)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.buttonCambiarVista)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.recyclerViewViajes)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void clickingFilterButtonOpensFilterActivity() {
        Intents.init();
        try {
            Espresso.onView(ViewMatchers.withId(R.id.btnOpenFilter)).perform(ViewActions.click());
            intended(hasComponent(FilterActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }
}
