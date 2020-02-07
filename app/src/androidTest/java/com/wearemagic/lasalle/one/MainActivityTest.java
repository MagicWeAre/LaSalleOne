package com.wearemagic.lasalle.one;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void mainActivityTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.idField),
                        childAtPosition(
                                allOf(withId(R.id.loginLinearLayout),
                                        childAtPosition(
                                                withId(R.id.loginRelativeLayout),
                                                0)),
                                1)));
        appCompatEditText.perform(scrollTo(), replaceText("014403980"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.passwordField),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("xzJhcsL7J3"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.signInButton), withText("Sign In"),
                        childAtPosition(
                                allOf(withId(R.id.loginLinearLayout),
                                        childAtPosition(
                                                withId(R.id.loginRelativeLayout),
                                                0)),
                                5)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.mainMenuProfile), withContentDescription("Profile"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainBottomNavigation),
                                        0),
                                4),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.profileName), withText("Jacob"),
                        childAtPosition(
                                allOf(withId(R.id.profileLayout),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                                0)),
                                1),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.profileId), withText("014403980"),
                        childAtPosition(
                                allOf(withId(R.id.profileLayout),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                                0)),
                                3),
                        isDisplayed()));
        textView2.check(matches(isDisplayed()));

        ViewInteraction bottomNavigationItemView2 = onView(
                allOf(withId(R.id.mainMenuBalance), withContentDescription("Finances"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainBottomNavigation),
                                        0),
                                3),
                        isDisplayed()));
        bottomNavigationItemView2.perform(click());

        ViewInteraction textView3 = onView(withId(R.id.coursesTotalAmount));
        textView3.check(matches(isDisplayed()));

        // Enter Credits Tab
        ViewInteraction tabView = onView(
                allOf(childAtPosition(childAtPosition(withId(R.id.mainTabs), 0), 1),
                        isDisplayed()));
        tabView.perform(click());

        // Enter Charges Tab
        ViewInteraction tabView2 = onView(
                allOf(childAtPosition(childAtPosition(withId(R.id.mainTabs), 0), 2),
                        isDisplayed()));
        tabView2.perform(click());

        // Back to Summary
        ViewInteraction tabView3 = onView(
                allOf(childAtPosition(childAtPosition(withId(R.id.mainTabs), 0), 0),
                        isDisplayed()));
        tabView3.perform(click());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.balanceBarSpinner),
                        childAtPosition(
                                allOf(withId(R.id.navigationToolbarIncluded),
                                        childAtPosition(
                                                withId(R.id.mainAppBarLayout),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatSpinner.perform(click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(7);
        appCompatCheckedTextView.perform(click());

        ViewInteraction tabView4 = onView(
                allOf(withContentDescription("Deposits"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainTabs),
                                        0),
                                1),
                        isDisplayed()));
        tabView4.perform(click());

        ViewInteraction tabView5 = onView(
                allOf(withContentDescription("Charges"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainTabs),
                                        0),
                                2),
                        isDisplayed()));
        tabView5.perform(click());

        ViewInteraction tabView6 = onView(
                allOf(withContentDescription("Summary"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainTabs),
                                        0),
                                0),
                        isDisplayed()));
        tabView6.perform(click());

        ViewInteraction bottomNavigationItemView3 = onView(
                allOf(withId(R.id.mainMenuGrades), withContentDescription("Grades"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainBottomNavigation),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView3.perform(click());

        ViewInteraction cardView = onView(
                allOf(withId(R.id.cardViewGradeSubject),
                        childAtPosition(
                                allOf(withId(R.id.gradesRecyclerView),
                                        childAtPosition(
                                                withId(R.id.gradesConstraintLayout),
                                                1)),
                                0),
                        isDisplayed()));
        cardView.perform(click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatSpinner2 = onView(
                allOf(withId(R.id.gradesBarSpinner),
                        childAtPosition(
                                allOf(withId(R.id.navigationToolbarIncluded),
                                        childAtPosition(
                                                withId(R.id.mainAppBarLayout),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatSpinner2.perform(click());

        DataInteraction appCompatCheckedTextView2 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(4);
        appCompatCheckedTextView2.perform(click());

        ViewInteraction cardView2 = onView(
                allOf(withId(R.id.cardViewGradeSubject),
                        childAtPosition(
                                allOf(withId(R.id.gradesRecyclerView),
                                        childAtPosition(
                                                withId(R.id.gradesConstraintLayout),
                                                1)),
                                0),
                        isDisplayed()));
        cardView2.perform(click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction bottomNavigationItemView4 = onView(
                allOf(withId(R.id.mainMenuSubjects), withContentDescription("Subjects"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainBottomNavigation),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView4.perform(click());

        ViewInteraction cardView3 = onView(
                allOf(withId(R.id.cardViewScheduleSubject),
                        childAtPosition(
                                allOf(withId(R.id.subjectsRecyclerView),
                                        childAtPosition(
                                                withId(R.id.subjectsConstraintLayout),
                                                1)),
                                0),
                        isDisplayed()));
        cardView3.perform(click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.scheduleToolbarIncluded),
                                        childAtPosition(
                                                withId(R.id.scheduleAppBarLayout),
                                                0)),
                                1)));
        appCompatImageButton3.perform(scrollTo(), click());

        ViewInteraction appCompatSpinner3 = onView(
                allOf(withId(R.id.subjectsBarSpinner),
                        childAtPosition(
                                allOf(withId(R.id.navigationToolbarIncluded),
                                        childAtPosition(
                                                withId(R.id.mainAppBarLayout),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatSpinner3.perform(click());

        DataInteraction appCompatCheckedTextView3 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(6);
        appCompatCheckedTextView3.perform(click());

        ViewInteraction cardView4 = onView(
                allOf(withId(R.id.cardViewScheduleSubject),
                        childAtPosition(
                                allOf(withId(R.id.subjectsRecyclerView),
                                        childAtPosition(
                                                withId(R.id.subjectsConstraintLayout),
                                                1)),
                                0),
                        isDisplayed()));
        cardView4.perform(click());

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.scheduleToolbarIncluded),
                                        childAtPosition(
                                                withId(R.id.scheduleAppBarLayout),
                                                0)),
                                1)));
        appCompatImageButton4.perform(scrollTo(), click());

        ViewInteraction bottomNavigationItemView5 = onView(
                allOf(withId(R.id.mainMenuHome), withContentDescription("Home"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.mainBottomNavigation),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView5.perform(click());

        ViewInteraction appCompatImageButton5 = onView(
                allOf(withContentDescription("Open drawer"),
                        childAtPosition(
                                allOf(withId(R.id.navigationToolbarIncluded),
                                        childAtPosition(
                                                withId(R.id.mainAppBarLayout),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatImageButton5.perform(click());

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.mainDrawerNavigation),
                                        0)),
                        5),
                        isDisplayed()));
        navigationMenuItemView.perform(click());
    }
}
