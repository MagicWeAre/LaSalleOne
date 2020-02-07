package com.wearemagic.lasalle.one.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.ScheduleSubjectDetailActivity;
import com.wearemagic.lasalle.one.adapters.ScheduleSubjectAdapter;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.SchedulePiece;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;
import com.wearemagic.lasalle.one.providers.StudentData;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubjectsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ScheduleSubjectAdapter.OnGSListener{
    private static final String TAG = "LaSalleOne";
    private SubjectsListener listener;

    private SubjectsAsyncTaskRunner subjectsTask = new SubjectsAsyncTaskRunner();
    private ArrayList<String> periodValueList = new ArrayList<>();
    private ArrayList<ScheduleSubject> scheduleSubjectArrayList = new ArrayList<>();

    private ConnectivityManager mConnectivityManager;
    private RecyclerView subjectsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScheduleSubjectAdapter ssAdapter;
    private ArrayList<ScheduleSubject> ssList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    private TextView emptyMessage;

    private String sessionCookie = "";
    private int spinnerPosition = 0;
    private boolean viewCreated;
    private boolean firstGet = true;

    public SubjectsFragment() { }

    public interface SubjectsListener {
        void onSubjectInfoSent(ArrayList infoList);
        void onRedoLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            periodValueList = savedInstanceState.getStringArrayList("periodValueList");
            sessionCookie = savedInstanceState.getString("sessionCookie");
            spinnerPosition = savedInstanceState.getInt("spinnerPosition");
            scheduleSubjectArrayList = savedInstanceState.getParcelableArrayList("scheduleSubjectArrayList");
            firstGet = savedInstanceState.getBoolean("firstGet");
        }

        mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subjects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.subjectsRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        constraintLayout = getView().findViewById(R.id.gradesConstraintLayout);
        emptyMessage = getView().findViewById(R.id.subjectsEmpty);

        if(sessionCookie.isEmpty()){
            if (getArguments() != null){
                sessionCookie = getArguments().getString("sessionCookie");
            }
        }

        subjectsRecyclerView = getView().findViewById(R.id.subjectsRecyclerView);
        ssAdapter = new ScheduleSubjectAdapter(ssList, getContext(), this);

        mLayoutManager = new LinearLayoutManager(getContext());

        subjectsRecyclerView.setLayoutManager(mLayoutManager);
        subjectsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        subjectsRecyclerView.setAdapter(ssAdapter);

        viewCreated = true;
    }

    @Override
    public void onResume() {
        if (scheduleSubjectArrayList.isEmpty()){
            onRefresh();
        } else {
            swipeRefreshLayout.setRefreshing(true);
            fillSubjects(scheduleSubjectArrayList);
            swipeRefreshLayout.setRefreshing(false);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (subjectsTask != null){
            subjectsTask.cancel(true);
            subjectsTask = null;}

        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("sessionCookie", sessionCookie);
        outState.putInt("spinnerPosition", spinnerPosition);
        outState.putStringArrayList("periodValueList", periodValueList);
        outState.putParcelableArrayList("scheduleSubjectArrayList", scheduleSubjectArrayList);
        outState.putBoolean("firstGet", firstGet);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        if (periodValueList == null) {
            setEmptyMessage();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            if (viewCreated && !periodValueList.isEmpty()) {
                if (subjectsTask != null) {
                    subjectsTask.cancel(true);
                    subjectsTask = null;
                }

                subjectsTask = new SubjectsAsyncTaskRunner();
                subjectsTask.execute(sessionCookie);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SubjectsListener) {
            listener = (SubjectsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SubjectsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void onSubjectClick(int position){
        ScheduleSubject scheduleSubject = ssList.get(position);
        String numericCode = scheduleSubject.getNumericCode();
        String periodCode = scheduleSubject.getPeriodCode();

        //Intent
        Intent i = new Intent(getActivity(), ScheduleSubjectDetailActivity.class);
        i.putExtra("sessionCookie", sessionCookie);
        i.putExtra("numericCode", numericCode);
        i.putExtra("periodCode", periodCode);
        startActivity(i);
    }

    public void sharePeriodValues(List<String> periodList) {
        if (periodList != null){
            periodValueList.addAll(periodList);
        } else {
            periodValueList = null;
        }    }

    public void shareSpinnerPosition(int spinnerPos) {
        spinnerPosition = spinnerPos;
    }

    private void setEmptyMessage() {
        emptyMessage.setText(getString(R.string.error_no_data));
        emptyMessage.setVisibility(View.VISIBLE);
    }


    public void fillSubjects(ArrayList<ScheduleSubject> scheduleSubjectList) {
        emptyMessage.setVisibility(View.GONE);

        ssList.clear();
        ssList.addAll(scheduleSubjectList);
        ssAdapter.notifyDataSetChanged();
    }

    private ArrayList<ArrayList<ScheduleSubject>> getSubjectsByDate(ArrayList<ScheduleSubject> initialList) {

        ArrayList<ArrayList<ScheduleSubject>> returnList = new ArrayList<>();

        ArrayList<ScheduleSubject> currentSubjects = new ArrayList<>();
        ArrayList<ScheduleSubject> todaySubjects = new ArrayList<>();

        ArrayList<ScheduleSubject> pastSubjectsDay = new ArrayList<>();
        ArrayList<ScheduleSubject> futureSubjectsDay = new ArrayList<>();
        ArrayList<ScheduleSubject> nowSubjectsDay = new ArrayList<>();

        // Calendar PreProcessing
        Date today = new Date();
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(today);
        final int todayDayWeek = todayCal.get(Calendar.DAY_OF_WEEK);

        // Check for each subject if today falls inside their valid period
        for (ScheduleSubject scheduleSubject : initialList) {
            Date startDate = null;
            Date endDate = null;

            try {
                startDate = scheduleSubject.getStartDate();
                endDate = scheduleSubject.getEndDate();
            } catch (ParseException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }

            if (startDate != null && endDate != null) {
                if(today.after(startDate) && today.before(endDate)){
                    currentSubjects.add(scheduleSubject);
                }
            }
        }

        // Check for each subject if there are any classes today, divide into three categories
        for (ScheduleSubject currentSubject: currentSubjects) {
            System.out.println();
            ArrayList<Integer> weekIntList = currentSubject.getWeekDaysList();

            if(weekIntList.contains(todayDayWeek)){
                ArrayList<SchedulePiece> classesOfDay = currentSubject.getClassesOfDay(todayDayWeek);

                for (SchedulePiece classOfDay : classesOfDay) {

                    // Convert to objects and compare

                    SimpleDateFormat isoTime = new SimpleDateFormat("HH:mm", Locale.US);
                    String startTimeString =  classOfDay.getStartHour();
                    String endTimeString =  classOfDay.getEndHour();

                    Date startTime = new Date();
                    Date endTime = new Date();

                    try {
                        startTime = isoTime.parse(startTimeString);
                        endTime = isoTime.parse(endTimeString);
                    } catch (ParseException e){
                        Log.e(TAG, e.getMessage());
                    }

                    Date startDate = combineDates(today, startTime);
                    Date endDate = combineDates(today, endTime);

                    if(today.before(startDate)) {
                        futureSubjectsDay.add(currentSubject);
                    } else if (today.after(startDate)){
                        if(today.before(endDate)){
                            nowSubjectsDay.add(currentSubject);
                        } else {
                            pastSubjectsDay.add(currentSubject);
                        }
                    }
                }
                todaySubjects.add(currentSubject);
            }
            // So far we have a list of subjects with classes today
        }

        // Sort Future Subjects
        if(futureSubjectsDay.size() > 1){
            for (ScheduleSubject subject : futureSubjectsDay){
                ArrayList<SchedulePiece> schedulePiecesList = subject.getClassesAfterTime(today);
                if (!schedulePiecesList.isEmpty()){
                    schedulePiecesList.get(0);
                }
            }

            Collections.sort(futureSubjectsDay, new Comparator<ScheduleSubject>() {
                @Override public int compare(ScheduleSubject ss1, ScheduleSubject ss2) {
                    Date now = new Date();

                    ArrayList<SchedulePiece> ss1List = ss1.getClassesAfterTime(now);
                    ArrayList<SchedulePiece> ss2List = ss2.getClassesAfterTime(now);

                    SchedulePiece ss1Time, ss2Time;

                    ss1Time = ss1List.get(0);
                    ss2Time = ss2List.get(0);

                    return ss1Time.getStartDate().compareTo(ss2Time.getStartDate()); // Ascending
                }
            });
        }


        returnList.add(nowSubjectsDay);
        returnList.add(futureSubjectsDay);


        // By now we have three lists of subjects of the day, depending on whether they
        // have, haven't, or are happening right now

        return returnList;
    }

    private static Date combineDates(Date date, Date time) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);

        return cal.getTime();
    }

    private void sendEventCode(String code) {
        switch (code) {
            case "ERROR_NO_INTERNET_CONNECTION":
                if(viewCreated) {
                    Snackbar noInternetSB = Snackbar
                            .make(getActivity().findViewById(R.id.mainFrameLayout), getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.retry_internet_connection), (View view) -> {
                                onRefresh();
                            });

                    noInternetSB.show();
                }

                break;
            default:
                emptyMessage.setText(getString(R.string.error_parsing_data));
                emptyMessage.setVisibility(View.VISIBLE);
                break;
        }

    }

    private class SubjectsAsyncTaskRunner extends AsyncTask<String, String, ArrayList<ScheduleSubject>> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<ScheduleSubject> doInBackground(String... params) {
            String serviceCookie = params[0];

            ArrayList<ScheduleSubject> returnList = new ArrayList<>();
            boolean doBackgroundFinished = false;

            while (!doBackgroundFinished){
                if (isCancelled()) break;

                try {
                    String periodArgument = periodValueList.get(spinnerPosition);
                    returnList = StudentData.getSubjects(serviceCookie, periodArgument, firstGet);
                    firstGet = false;
                } catch (IOException e) {
                    Log.e(TAG, "IOException on SubjectsAsyncTask");
                } catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "IndexOutOfBoundsException on SubjectsAsyncTask");
                    Log.e(TAG, e.getMessage());
                    returnList = null;
                } catch (NullPointerException np) {
                    Log.d(TAG, "NullPointerException on SubjectsAsyncTask");
                    returnList = null;
                } catch (LoginTimeoutException lt) {
                    Log.d(TAG, "LoginTimeoutException on SubjectsAsyncTask");
                    if (getActivity() != null){
                        listener.onRedoLogin();
                    }
                }

                doBackgroundFinished = true;
            }
            return returnList;
        }

        @Override
        protected void onPostExecute(ArrayList<ScheduleSubject> returnList) {
            if (returnList != null){
                if (!returnList.isEmpty()){

                    ArrayList homeList = new ArrayList();
                    homeList.add(String.valueOf(returnList.size()));

                    ArrayList<ArrayList<ScheduleSubject>> homeDates = getSubjectsByDate(returnList);

                    homeList.add(homeDates.get(0));
                    homeList.add(homeDates.get(1));

                    listener.onSubjectInfoSent(homeList);

                    scheduleSubjectArrayList = returnList;
                    fillSubjects(returnList);
                } else {
                    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if (isConnected){
                        sendEventCode("ERROR_HTTP_IO_EXCEPTION");
                    } else {
                        sendEventCode("ERROR_NO_INTERNET_CONNECTION");
                    }
                }
            } else {
                sendEventCode("ERROR_HTTP_PARSE");
            }

            swipeRefreshLayout.setRefreshing(false);

        }
    }
}
