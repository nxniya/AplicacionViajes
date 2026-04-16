package com.example.aplicacionviajes;

import android.graphics.Rect;
import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

public class TestUtils {

    public static ViewAction nestedScrollTo() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(NestedScrollView.class));
            }

            @Override
            public String getDescription() {
                return "scroll to view inside NestedScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                Rect rect = new Rect();
                view.getDrawingRect(rect);
                view.requestRectangleOnScreen(rect, true);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
