package com.wearemagic.lasalle.one.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.ScheduleSubjectDetailActivity;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private BottomNavigationView bottomNavigation;

    private CardView numberCard, avgCard, highestCard, lowestCard, nextSubjectCard, currentSubjectCard;
    private TextView numberText, avgText, highestText, lowestText, nextSubject, currentSubject;
    ConstraintLayout numberCL, avgCL, highestCL, lowestCL, nextSubjectCL, currentSubjectCL;

    private RecyclerView cSubjectsList, nSubjectsList;
    private ImageView dropCSubjects, dropNSubjects;
    private TextView cSubjectsPlus, nSubjectsPlus;

    //private SwipeRefreshLayout swipeRefreshLayout;

    private TextView mainBannerText;

    private ProgressBar homeLoading;
    private LinearLayout llOne, llTwo, llThree;

    private boolean male;
    private boolean sex;
    private String number, avg, highest, lowest;
    private ArrayList<ScheduleSubject> nextSubjectAL, currentSubjectAL;

    private String sessionCookie = "";
    private String cNumericCode, cPeriodCode, nNumericCode, nPeriodCode;

    public HomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            sessionCookie = savedInstanceState.getString("sessionCookie");

            male = savedInstanceState.getBoolean("male");
            sex = savedInstanceState.getBoolean("sex");
            number = savedInstanceState.getString("number");
            avg = savedInstanceState.getString("avg");
            highest = savedInstanceState.getString("highest");
            lowest = savedInstanceState.getString("lowest");
            nextSubjectAL = savedInstanceState.getParcelableArrayList("nextSubjectAL");
            currentSubjectAL = savedInstanceState.getParcelableArrayList("currentSubjectAL");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

//        swipeRefreshLayout = view.findViewById(R.id.homeRefresh);
//        swipeRefreshLayout.setOnRefreshListener(this);
//        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        if(sessionCookie.isEmpty()){
            if (getArguments() != null){
                sessionCookie = getArguments().getString("sessionCookie");
            }
        }

        bottomNavigation = getActivity().findViewById(R.id.mainBottomNavigation);

        mainBannerText = getView().findViewById(R.id.homeBannerText);

        numberCard = getView().findViewById(R.id.subjectsNumberCard);
        avgCard = getView().findViewById(R.id.subjectsAvgCard);
        highestCard = getView().findViewById(R.id.subjectsHighestCard);
        lowestCard = getView().findViewById(R.id.subjectsLowestCard);
        nextSubjectCard = getView().findViewById(R.id.nextSubjectCard);
        currentSubjectCard = getView().findViewById(R.id.currentSubjectCard);

        numberText = getView().findViewById(R.id.subjectsNumber);
        avgText = getView().findViewById(R.id.subjectsAvg);
        highestText = getView().findViewById(R.id.subjectsHighest);
        lowestText = getView().findViewById(R.id.subjectsLowest);
        nextSubject = getView().findViewById(R.id.nextSubject);
        currentSubject = getView().findViewById(R.id.currentSubject);

        numberCL = getView().findViewById(R.id.homeNumberCL);
        avgCL = getView().findViewById(R.id.homeAvgCL);
        highestCL = getView().findViewById(R.id.homeHighestCL);
        lowestCL = getView().findViewById(R.id.homeLowestCL);
        nextSubjectCL = getView().findViewById(R.id.nextSubjectCL);
        currentSubjectCL = getView().findViewById(R.id.currentSubjectCL);

        llOne = getView().findViewById(R.id.statisticsOne);
        llTwo = getView().findViewById(R.id.statisticsTwo);
        llThree = getView().findViewById(R.id.scheduleStatistics);

        dropCSubjects = getView().findViewById(R.id.homeSubjectDropdownIcon);
        dropNSubjects = getView().findViewById(R.id.homeNSubDropdownIcon);

        cSubjectsPlus = getView().findViewById(R.id.currentSubjectPlus);
        nSubjectsPlus = getView().findViewById(R.id.nextSubjectPlus);

        homeLoading = getView().findViewById(R.id.homeLoading);

        if(sex){
            setSex(male);
        }

        if(number != null && !number.isEmpty()){
            onSubjectsNumberSent(number);
        }

        if(highest != null && !highest.isEmpty()){
            onGradesHighestSent(highest);
        }

        if(lowest != null && !lowest.isEmpty()){
            onGradesLowestSent(lowest);
        }

        if(avg != null && !avg.isEmpty()){
            onGradesAvgSent(avg);
        }

        if(nextSubjectAL != null && !nextSubjectAL.isEmpty()){
            onNextSubjectsSent(nextSubjectAL);
        }

        if(currentSubjectAL != null && !currentSubjectAL.isEmpty()){
            onCurrentSubjectsSent(currentSubjectAL);
        }

        numberCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigation.setSelectedItemId(R.id.mainMenuSubjects);
            }
        });

        avgCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigation.setSelectedItemId(R.id.mainMenuGrades);
            }
        });

        highestCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigation.setSelectedItemId(R.id.mainMenuGrades);
            }
        });

        lowestCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigation.setSelectedItemId(R.id.mainMenuGrades);
            }
        });

        currentSubjectCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Maybe redirect to DetailActivity? Sure, bitch!
                // Sorry for the profanity, it's just me falling into madness

                Intent i = new Intent(getActivity(), ScheduleSubjectDetailActivity.class);
                i.putExtra("sessionCookie", sessionCookie);
                i.putExtra("numericCode", cNumericCode);
                i.putExtra("periodCode", cPeriodCode);
                startActivity(i);
            }
        });

        nextSubjectCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ScheduleSubjectDetailActivity.class);
                i.putExtra("sessionCookie", sessionCookie);
                i.putExtra("numericCode", nNumericCode);
                i.putExtra("periodCode", nPeriodCode);
                startActivity(i);
            }
        });


        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("number", number);
        outState.putString("avg", avg);
        outState.putString("highest", highest);
        outState.putString("lowest", lowest);
        outState.putBoolean("male", male);
        outState.putBoolean("sex", sex);
        outState.putParcelableArrayList("nextSubjectAL", nextSubjectAL);
        outState.putParcelableArrayList("currentSubjectAL", currentSubjectAL);

        outState.putString("sessionCookie", sessionCookie);

        super.onSaveInstanceState(outState);
    }

    // Populate the three dot menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

//    @Override
//    public void onRefresh() {
//
//    }

    public void setSex(boolean localMale){

       /* Date nowDate = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(2020, 1, 18);

        Date endWelcomeDate = cal.getTime();*/

        if (!sex) {
            sex = true;
            male = localMale;
        }


        if (!localMale) {
            mainBannerText.setText(getString(R.string.welcome_message_w));
        } else {
            mainBannerText.setText(getString(R.string.welcome_message_m));
        }

        mainBannerText.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        if(sex){
            setSex(male);
        }
        super.onResume();
    }

    public void onSubjectsNumberSent(String localNumber) {
        number = localNumber;
        numberText.setText(number);
        homeLoading.setVisibility(View.GONE);
        llOne.setVisibility(View.VISIBLE);
        numberCard.setVisibility(View.VISIBLE);
    }

    public void onGradesAvgSent(String localAvg) {
        avg = localAvg;
        avgText.setText(avg);
        homeLoading.setVisibility(View.GONE);
        llOne.setVisibility(View.VISIBLE);
        avgCard.setVisibility(View.VISIBLE);
    }

    public void onGradesHighestSent(String localHighest) {
        highest = localHighest.substring(0,2);
        highestText.setText(highest);
        homeLoading.setVisibility(View.GONE);
        llTwo.setVisibility(View.VISIBLE);
        highestCard.setVisibility(View.VISIBLE);
    }

    public void onGradesLowestSent(String localLowest) {
        lowest = localLowest.substring(0,2);
        lowestText.setText(lowest);
        homeLoading.setVisibility(View.GONE);
        llTwo.setVisibility(View.VISIBLE);
        lowestCard.setVisibility(View.VISIBLE);
    }

    public void onCurrentSubjectsSent(ArrayList<ScheduleSubject> localCurrentSubjects) {
        currentSubjectAL = localCurrentSubjects;
        int listSize = localCurrentSubjects.size();

        if (listSize > 0) {
            currentSubject.setText(localCurrentSubjects.get(0).getSubjectName());

            if (listSize > 1) {
                cSubjectsPlus.setText(String.format(getString(R.string.schedule_class_plus), listSize - 1));
                cSubjectsPlus.setVisibility(View.VISIBLE);
            }

            cNumericCode = localCurrentSubjects.get(0).getNumericCode();
            cPeriodCode = localCurrentSubjects.get(0).getPeriodCode();

            homeLoading.setVisibility(View.GONE);
            llThree.setVisibility(View.VISIBLE);
            currentSubjectCard.setVisibility(View.VISIBLE);
        }
    }

    public void onNextSubjectsSent(ArrayList<ScheduleSubject> localNextSubjects) {
        nextSubjectAL = localNextSubjects;
        int listSize = localNextSubjects.size();

        if(listSize > 0){
            nextSubject.setText(localNextSubjects.get(0).getSubjectName());

            if (listSize > 1){
                nSubjectsPlus.setText(String.format(getString(R.string.schedule_class_plus), listSize - 1));
                nSubjectsPlus.setVisibility(View.VISIBLE);
            }

            nNumericCode = localNextSubjects.get(0).getNumericCode();
            nPeriodCode = localNextSubjects.get(0).getPeriodCode();

            homeLoading.setVisibility(View.GONE);
            llThree.setVisibility(View.VISIBLE);
            nextSubjectCard.setVisibility(View.VISIBLE);
        }
    }
}
