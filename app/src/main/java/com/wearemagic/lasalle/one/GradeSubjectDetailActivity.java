package com.wearemagic.lasalle.one;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wearemagic.lasalle.one.adapters.ParcialGradeAdapter;
import com.wearemagic.lasalle.one.adapters.SummaryTypeAdapter;
import com.wearemagic.lasalle.one.objects.ParcialGrade;
import com.wearemagic.lasalle.one.objects.SummaryType;

import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GradeSubjectDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "LaSalleOne";
    private ParcialAsyncTaskRunner parcialTask = new ParcialAsyncTaskRunner();
    private ArrayList<ParcialGrade> parcialGradeList = new ArrayList<>();
    private String subjectName;
    private String instructorName;

    private RecyclerView parcialGradeRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ParcialGradeAdapter pgAdapter;
    private ArrayList<ParcialGrade> pgList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private String sessionCookie;
    private String numericCode;
    private String periodCode;
    private boolean viewCreated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_subject_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle loginBundle = getIntent().getExtras();

        if (savedInstanceState != null) {
            sessionCookie = savedInstanceState.getString("sessionCookie");
            periodCode = savedInstanceState.getString("periodCode");
            numericCode = savedInstanceState.getString("numericCode");

            parcialGradeList = savedInstanceState.getParcelableArrayList("parcialGradeList");
            subjectName = savedInstanceState.getString("subjectName");
            instructorName = savedInstanceState.getString("instructorName");

        } else {

            if(loginBundle != null){
                sessionCookie = loginBundle.getString("sessionCookie");
                numericCode = loginBundle.getString("numericCode");
                periodCode = loginBundle.getString("periodCode");
            }

        }

        swipeRefreshLayout = findViewById(R.id.parcialRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        parcialGradeRecyclerView = findViewById(R.id.parcialRecyclerView);
        pgAdapter = new ParcialGradeAdapter(pgList, this);

        mLayoutManager = new LinearLayoutManager(this);

        parcialGradeRecyclerView.setLayoutManager(mLayoutManager);
        parcialGradeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        parcialGradeRecyclerView.setAdapter(pgAdapter);

        viewCreated = true;
    }

    @Override
    public void onDestroy() {
        if (parcialTask != null){
            parcialTask.cancel(true);
            parcialTask = null;}

        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}

        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (parcialGradeList.isEmpty()){
            onRefresh();
        } else {
            swipeRefreshLayout.setRefreshing(true);
            fillParcials(parcialGradeList);
            fillGradeData(subjectName, instructorName);
            swipeRefreshLayout.setRefreshing(false);
        }
        super.onResume();
    }

    @Override
    public void onRefresh() {
        if (viewCreated) {

            if (parcialTask != null) {
                parcialTask.cancel(true);
                parcialTask = null;
            }

            parcialTask = new ParcialAsyncTaskRunner();
            parcialTask.execute(sessionCookie, numericCode, periodCode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sessionCookie", sessionCookie);
        outState.putString("periodCode", periodCode);
        outState.putString("numericCode", numericCode);

        outState.putParcelableArrayList("parcialGradeList", parcialGradeList);
        outState.putString("subjectName", subjectName);
        outState.putString("instructorName", instructorName);

        super.onSaveInstanceState(outState);
    }

    private void fillParcials(ArrayList<ParcialGrade> parcialGradeList){
        pgList.clear();
        pgList.addAll(parcialGradeList);
        pgAdapter.notifyDataSetChanged();
    }

    private void fillGradeData(String subjectName, String instructorName){
        TextView subjectNameTextView = findViewById(R.id.subjectNameParcials);
        TextView instructorNameTextView = findViewById(R.id.instructorNameParcials);

        subjectNameTextView.setText(subjectName);
        instructorNameTextView.setText(instructorName);

        subjectNameTextView.setVisibility(View.VISIBLE);
        instructorNameTextView.setVisibility(View.VISIBLE);
    }

    private class ParcialAsyncTaskRunner extends AsyncTask<String, String, ArrayList> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList doInBackground(String... params) {
            String serviceCookie = params[0];
            String numericCode = params[1];
            String periodCode = params[2];

            ArrayList returnList = new ArrayList<>();
            boolean doBackgroundFinished = false;

            while (!doBackgroundFinished){
                if (isCancelled()) break;

                try {
                    Document parcialDocument = getParcialGradesDocument(serviceCookie, periodCode, numericCode);
                    ArrayList<String> additionalInfo = getAdditionalInfo(parcialDocument);
                    ArrayList<ParcialGrade> parcialGrades = getParcialGrades(parcialDocument);

                    if (!additionalInfo.isEmpty()) {
                        String subject = additionalInfo.get(0);
                        String instructor = additionalInfo.get(1);

                        returnList.add(subject);
                        returnList.add(instructor);
                        returnList.add(parcialGrades);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "IOException on ParcialAsyncTask");
                }

                doBackgroundFinished = true;
            }
            return returnList;
        }

        @Override
        protected void onPostExecute(ArrayList returnList) {
            if (returnList != null && !returnList.isEmpty()){

                subjectName = (String) returnList.get(0);
                String instructor = (String) returnList.get(1);

                instructor = instructor.replace(",", "");
                String[] instructorArray = instructor.split("\\s+");
                String secondName = "";

                if (instructorArray.length > 1){
                    if (instructorArray.length > 3){
                        secondName = instructorArray[3] + " "; }

                    instructorName = WordUtils.capitalizeFully(instructorArray[2] + " " +
                            secondName + instructorArray[0] + " " + instructorArray[1]);
                } else {
                    instructorName = instructorArray[0];
                }

                parcialGradeList = (ArrayList<ParcialGrade>) returnList.get(2);
                fillParcials(parcialGradeList);
                fillGradeData(subjectName, instructorName);
            } else {
                fillGradeData(getString(R.string.error_no_data), "");
            }
            swipeRefreshLayout.setRefreshing(false);

        }
    }

    private Document getParcialGradesDocument(String serviceCookie, String periodCode, String numericCode) throws IOException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        Document parcialGradesDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/ActivityGradesReport.aspx")
                .data("termperiod", periodCode)
                .data("SectionId", numericCode)
                .cookies(cookies)
                .get();

        return parcialGradesDocument;
    }

    private ArrayList<String> getAdditionalInfo(Document parcialGradeDocument) {
        ArrayList<String> returnList = new ArrayList<>();

        Boolean dataAvailable = parcialGradeDocument.getElementsByClass("msgNoData").isEmpty();

        if (dataAvailable) {

            String subjectName = parcialGradeDocument.getElementsByTag("h2").first().ownText().substring(24);

            String instructorName = parcialGradeDocument.getElementsByAttributeValue("style", "font-size:0.6em;").first().text();

            //Not-so inside joke
            if (instructorName.contains("FORTINO")) {
                instructorName = instructorName.replace("FORTINO", "FORTNITO");
            }

            returnList.add(subjectName);
            returnList.add(instructorName);
        }

        return returnList;
    }

    private ArrayList<ParcialGrade> getParcialGrades(Document parcialGradeDocument) {
        ArrayList<ParcialGrade> returnList = new ArrayList<>();

        try {
            //Rows for each subject (Parciales Y Semestral)
            Elements courseInfoRows = parcialGradeDocument.getElementById("resultsFinalByType3").getElementsByTag("tr");
            Elements finalCourseInfoRows = parcialGradeDocument.getElementById("resultsFinalByType4").getElementsByTag("tr");
            courseInfoRows.remove(0);
            finalCourseInfoRows.remove(0);

            courseInfoRows.addAll(finalCourseInfoRows);

            for (Element courseInfoRow : courseInfoRows) {
                Elements parcialData = courseInfoRow.getElementsByTag("td");

                String parcialName = parcialData.get(0).text();
                String dueDate = parcialData.get(2).text();
                String pointsEarned = parcialData.get(3).text();
                String maxPoints = parcialData.get(4).text();
                String percentaje = parcialData.get(5).text();
                String maxPercentaje = parcialData.get(6).text();
                String entryDate = parcialData.get(7).text();

                ParcialGrade parcialGrade = new ParcialGrade(parcialName, pointsEarned, maxPoints, percentaje, maxPercentaje);
                parcialGrade.setDueDate(dueDate);
                parcialGrade.setGradeEntryDate(entryDate);

                returnList.add(parcialGrade);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException on getParcialGrades");
        }

        return returnList;
    }
}