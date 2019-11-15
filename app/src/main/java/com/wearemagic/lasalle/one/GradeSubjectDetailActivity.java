package com.wearemagic.lasalle.one;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.wearemagic.lasalle.one.adapters.ParcialGradeAdapter;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.ParcialGrade;

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

    private boolean redoingLogin;


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

            redoingLogin = savedInstanceState.getBoolean("redoingLogin");

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
    public void onDestroy() {
        if (parcialTask != null){
            parcialTask.cancel(true);
            parcialTask = null;}

        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}

        super.onDestroy();
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

        outState.putBoolean("redoingLogin", redoingLogin);

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

                    if (returnList.isEmpty()) {
                        returnList = null;
                    }

                } catch (IOException e) {
                    Log.e(TAG, "IOException on ParcialAsyncTask");
                } catch (LoginTimeoutException lt) {
                    Log.d(TAG, "LoginTimeoutException on ParcialAsyncTask");
                    onRedoLogin();
                }

                doBackgroundFinished = true;
            }
            return returnList;
        }

        @Override
        protected void onPostExecute(ArrayList returnList) {
            if (returnList != null){
                if (!returnList.isEmpty()) {

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
                    if (viewCreated) {
                        Snackbar noInternetSB = Snackbar
                                .make(findViewById(R.id.gradeSubjectDetailLayout), getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onRefresh();
                                    }
                                });

                        noInternetSB.show();
                    }
                }

            } else {
                fillGradeData(getString(R.string.error_no_data), "");
            }

            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void onRedoLogin(){
        if(!redoingLogin){
            redoingLogin = true;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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

    private ArrayList<String> getAdditionalInfo(Document parcialGradeDocument) throws LoginTimeoutException {
        ArrayList<String> returnList = new ArrayList<>();

        if(parcialGradeDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Boolean dataAvailable = parcialGradeDocument.getElementsByClass("msgNoData").isEmpty();

        if (dataAvailable) {

            String subjectName = parcialGradeDocument.getElementsByTag("h2").first().ownText().substring(24);

            String instructorName = parcialGradeDocument.getElementsByAttributeValue("style", "font-size:0.6em;").first().text();

            // Not-so inside joke
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
            // Rows for each subject (Parciales Y Semestral)
            Elements courseInfoTables = parcialGradeDocument.getElementsByAttributeValueStarting("id", "resultsFinalByType");
            Elements courseInfoRows = new Elements();

            for (Element courseType : courseInfoTables) {
                Elements courseTypeRows = courseType.getElementsByTag("tr");
                courseTypeRows.remove(0);
                courseInfoRows.addAll(courseTypeRows);
            }
            // Elements courseInfoRows = parcialGradeDocument.getElementById("resultsFinalByType3").getElementsByTag("tr");
            // Elements finalCourseInfoRows = parcialGradeDocument.getElementById("resultsFinalByType4").getElementsByTag("tr");
            // courseInfoRows.remove(0);
            // finalCourseInfoRows.remove(0);

            // courseInfoRows.addAll(finalCourseInfoRows);

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
