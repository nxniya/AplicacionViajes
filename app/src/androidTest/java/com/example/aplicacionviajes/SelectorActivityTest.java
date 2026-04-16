package com.example.aplicacionviajes;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SelectorActivityTest {

    @Rule
    public ActivityScenarioRule<SelectorActivity> testRule =
            new ActivityScenarioRule<>(SelectorActivity.class);

    @Test
    public void menuListViewIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.lvMenu))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void firstMenuOptionIsVisible() {
        Espresso.onView(ViewMatchers.withText("Ver viajes disponibles"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void secondMenuOptionIsVisible() {
        Espresso.onView(ViewMatchers.withText("Ver viajes seleccionados"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void clickingViajesDisponiblesNavigatesToListadoActivity() {
        Intents.init();
        try {
            Espresso.onView(ViewMatchers.withText("Ver viajes disponibles")).perform(ViewActions.click());
            intended(hasComponent(ListadoActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void clickingViajesSeleccionadosNavigatesToListadoActivity() {
        Intents.init();
        try {
            Espresso.onView(ViewMatchers.withText("Ver viajes seleccionados")).perform(ViewActions.click());
            intended(hasComponent(ListadoActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }
}
