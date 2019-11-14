package com.wearemagic.lasalle.one.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wearemagic.lasalle.one.GradeSubjectDetailActivity;
import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.adapters.GradeSubjectAdapter;
import com.wearemagic.lasalle.one.common.CommonStrings;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.GradeSubject;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, GradeSubjectAdapter.OnGSListener {
    private static final String TAG = "LaSalleOne";
    private GradesListener listener;

    private GradesAsyncTaskRunner gradesTask = new GradesAsyncTaskRunner();
    private ArrayList<String> periodValueList = new ArrayList<>();
    private ArrayList<GradeSubject> gradeSubjectArrayList = new ArrayList<>();

    private RecyclerView gradesRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private GradeSubjectAdapter gsAdapter;
    private ArrayList<GradeSubject> gsList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    private TextView emptyMessage;

    private String sessionCookie = "";
    private int spinnerPosition = 0;
    private boolean viewCreated;

    public GradesFragment() {  }

    public interface GradesListener {
        void onGradesInfoSent(ArrayList infoList);
        void onRedoLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            sessionCookie = savedInstanceState.getString("sessionCookie");
            spinnerPosition = savedInstanceState.getInt("spinnerPosition");
            periodValueList = savedInstanceState.getStringArrayList("periodValueList");
            gradeSubjectArrayList = savedInstanceState.getParcelableArrayList("gradeSubjectArrayList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grades, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.gradesRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        constraintLayout = getView().findViewById(R.id.gradesConstraintLayout);
        emptyMessage = getView().findViewById(R.id.gradesEmpty);

        if(sessionCookie.isEmpty()){
            if (getArguments() != null){
                sessionCookie = getArguments().getString("sessionCookie");
            }
        }

        gradesRecyclerView = getView().findViewById(R.id.gradesRecyclerView);
        gsAdapter = new GradeSubjectAdapter(gsList, getContext(), this);

        mLayoutManager = new LinearLayoutManager(getContext());

        gradesRecyclerView.setLayoutManager(mLayoutManager);
        gradesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        gradesRecyclerView.setAdapter(gsAdapter);

        viewCreated = true;
    }

    @Override
    public void onResume() {
        if (gradeSubjectArrayList.isEmpty()){
            onRefresh();
        } else {
            swipeRefreshLayout.setRefreshing(true);
            fillGrades(gradeSubjectArrayList);
            swipeRefreshLayout.setRefreshing(false);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (gradesTask != null){
            gradesTask.cancel(true);
            gradesTask = null;}

        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("sessionCookie", sessionCookie);
        outState.putInt("spinnerPosition", spinnerPosition);
        outState.putStringArrayList("periodValueList", periodValueList);
        outState.putParcelableArrayList("gradeSubjectArrayList", gradeSubjectArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        if (periodValueList == null) {
            setEmptyMessage();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            if (viewCreated && !periodValueList.isEmpty()) {

                if (gradesTask != null) {
                    gradesTask.cancel(true);
                    gradesTask = null;
                }

                gradesTask = new GradesAsyncTaskRunner();
                gradesTask.execute(sessionCookie);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GradesListener) {
            listener = (GradesListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GradesListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void onSubjectClick(int position){
        GradeSubject gradeSubject = gsList.get(position);
        String numericCode = gradeSubject.getNumericCode();
        String periodCode = gradeSubject.getPeriodCode();

        //Intent
        Intent i = new Intent(getActivity(), GradeSubjectDetailActivity.class);
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
        }
    }

    public void shareSpinnerPosition(int spinnerPos) {
        spinnerPosition = spinnerPos;
    }

    private class GradesAsyncTaskRunner extends AsyncTask<String, String, ArrayList<GradeSubject>> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<GradeSubject> doInBackground(String... params) {
            String serviceCookie = params[0];

            ArrayList<GradeSubject> returnList = new ArrayList<>();

            boolean doBackgroundFinished = false;

            while (!doBackgroundFinished) {
                if (isCancelled()) break;

                try {
                    String periodArgument = periodValueList.get(spinnerPosition);
                    returnList = getGradeSubjects(serviceCookie, periodArgument);
                } catch (IOException e) {
                    Log.e(TAG, "IOException on GradesAsyncTask");
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "IndexOutOfBoundsException on GradesAsyncTask");
                } catch (NullPointerException np) {
                    Log.d(TAG, "NullPointerException on GradesAsyncTask");
                    returnList = null;
                } catch (LoginTimeoutException lt) {
                    Log.d(TAG, "LoginTimeoutException on GradesAsyncTask");
                    if (getActivity() != null){
                        listener.onRedoLogin();
                    }
                }

                doBackgroundFinished = true;
            }

            return returnList;
        }

        @Override
        protected void onPostExecute(ArrayList<GradeSubject> returnList) {
            if (returnList != null){
                if(!returnList.isEmpty()){
                    Collections.sort(returnList, new Comparator<GradeSubject>() {
                        @Override public int compare(GradeSubject gs1, GradeSubject gs2) {
                            return gs2.getCurrentDoubleGrade() - gs1.getCurrentDoubleGrade(); // Ascending
                        }

                    });

                    gradeSubjectArrayList = returnList;
                    ArrayList homeList = new ArrayList();
                    homeList.add(String.valueOf(returnList.get(0).getCurrentGrade()));
                    homeList.add(String.valueOf(returnList.get(returnList.size() - 1).getCurrentGrade()));

                    //Get AVG
                    ArrayList<Integer> avgList = new ArrayList();
                    for (GradeSubject grade: returnList){
                        avgList.add(grade.getCurrentDoubleGrade());
                    }

                    int averageGrade = 0;

                    for (int points: avgList){
                        averageGrade = averageGrade + points;
                    }

                    int average = averageGrade / avgList.size();

                    homeList.add(String.valueOf(average));

                    listener.onGradesInfoSent(homeList);
                    fillGrades(gradeSubjectArrayList);
                } else{
                    if (constraintLayout != null){
                        Snackbar noInternetSB = Snackbar
                                .make(constraintLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onRefresh(); }
                                });

                        noInternetSB.show();
                    }
                }
            } else {
                emptyMessage.setText(getString(R.string.error_parsing_data));
                emptyMessage.setVisibility(View.VISIBLE);
            }

            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fillGrades(ArrayList<GradeSubject> gradeSubjectList) {
        emptyMessage.setVisibility(View.GONE);

        gsList.clear();
        gsList.addAll(gradeSubjectList);
        gsAdapter.notifyDataSetChanged();
    }

    private void setEmptyMessage() {
        emptyMessage.setText(getString(R.string.error_no_data));
        emptyMessage.setVisibility(View.VISIBLE);
    }

    private ArrayList<GradeSubject> getGradeSubjects(String serviceCookie, String period) throws IOException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        Connection.Response balancePageGet = Jsoup.connect(CommonStrings.baseURL + "Records/GradeReport.aspx")
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute();

        Document gradesDocument = balancePageGet.parse();

        if(gradesDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element viewState = gradesDocument.select("input[name=__VIEWSTATE]").first();
        Element eventValidation = gradesDocument.select("input[name=__EVENTVALIDATION]").first();

        Document gradesDocumentPost = Jsoup.connect(CommonStrings.baseURL + "Records/GradeReport.aspx")

                .data("ctl00$pageOptionsZone$GradeReportOptions$SubmitButton", "Submit")
                .data("ctl00$pageOptionsZone$GradeReportOptions$PeriodDropDown", period)
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .cookies(cookies)
                .post();

        Element gradesTable = gradesDocumentPost.getElementsByAttributeValue("summary", "A list of courses on the schedule and the grade for each course.").first();
        Elements tableRows = gradesTable.getElementsByAttribute("id");

        Elements linkDivs = gradesDocumentPost.getElementsByAttributeValue("class", "newMenuNoImg");
        ArrayList<GradeSubject> CoursesGradesList = new ArrayList<>();

        int rowNumber = 0;

        for (Element tableRow: tableRows) {
            Elements previewData = tableRow.getElementsByTag("td");

            //Assign info from subject row
            String courseInfo = previewData.get(1).text();
            String courseName = previewData.get(2).getElementsByTag("a").first().text();
            String numericCode = previewData.get(2).getElementsByTag("a").first().attr("onclick")
                    .split(" ", 3)[1].replace(");","");
            String sectionInfo = previewData.get(3).text();
            String creditsInfo = previewData.get(4).text();
            String qualityPoints = previewData.get(5).text();
            //Index 6 is without parenthesis
            String projectedGrade = previewData.get(6).text();
            String finalGrade = previewData.get(8).text();

            //Assign GradeSubject
            GradeSubject gradeSubject = new GradeSubject(numericCode, period, courseName, courseInfo, sectionInfo, creditsInfo);
            gradeSubject.setQualityPoints(qualityPoints);
            gradeSubject.setProjectedGrade(projectedGrade);
            gradeSubject.setFinalGrade(finalGrade);

            CoursesGradesList.add(gradeSubject);
            rowNumber ++;
        }

        return CoursesGradesList;
    }
}
