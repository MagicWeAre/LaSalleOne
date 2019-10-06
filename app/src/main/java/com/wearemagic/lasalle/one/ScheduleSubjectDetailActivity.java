package com.wearemagic.lasalle.one;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.wearemagic.lasalle.one.adapters.ExpandableScheduleAdapter;
import com.wearemagic.lasalle.one.objects.SchedulePiece;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ScheduleSubjectDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "LaSalleOne";
    private ScheduleAsyncTaskRunner scheduleTask = new ScheduleAsyncTaskRunner();
    private HashMap<String, ArrayList<SchedulePiece>> schedulePieceMap;
    private ArrayList<String> scheduleDayList = new ArrayList<>();
    private ArrayList<String> subjectData= new ArrayList<>();

    private ExpandableListView expandableListView;
    private ExpandableScheduleAdapter spAdapter;
    private HashMap<String, ArrayList<SchedulePiece>> spMap = new HashMap<>();
    private ArrayList<String> sdList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private String sessionCookie;
    private String numericCode;
    private String periodCode;
    private boolean viewCreated;

    private Toolbar scheduleToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_subject_detail);

        scheduleToolbar = findViewById(R.id.scheduleToolbarIncluded);
        setSupportActionBar(scheduleToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle loginBundle = getIntent().getExtras();

        if (savedInstanceState != null) {
            sessionCookie = savedInstanceState.getString("sessionCookie");
            periodCode = savedInstanceState.getString("periodCode");
            numericCode = savedInstanceState.getString("numericCode");

            schedulePieceMap = (HashMap<String, ArrayList<SchedulePiece>>) savedInstanceState.getSerializable("schedulePieceMap");
            scheduleDayList = savedInstanceState.getStringArrayList("scheduleDayList");
            subjectData = savedInstanceState.getStringArrayList("subjectData");

        } else {
            if(loginBundle != null){
                sessionCookie = loginBundle.getString("sessionCookie");
                numericCode = loginBundle.getString("numericCode");
                periodCode = loginBundle.getString("periodCode");
            }
        }

        swipeRefreshLayout = findViewById(R.id.scheduleRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setEnabled(false);

        expandableListView = findViewById(R.id.dayScheduleExpandableList);
        spAdapter = new ExpandableScheduleAdapter(this, sdList, spMap);
        expandableListView.setAdapter(spAdapter);

        viewCreated = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sessionCookie", sessionCookie);
        outState.putString("periodCode", periodCode);
        outState.putString("numericCode", numericCode);

        outState.putSerializable("schedulePieceMap", schedulePieceMap);
        outState.putStringArrayList("scheduleDayList", scheduleDayList);
        outState.putStringArrayList("subjectData", subjectData);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (scheduleTask != null){
            scheduleTask.cancel(true);
            scheduleTask = null;}

        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (scheduleDayList.isEmpty() || schedulePieceMap.isEmpty() || subjectData.isEmpty()){
            onRefresh();
        } else {
            swipeRefreshLayout.setRefreshing(true);
            fillExpandableList(scheduleDayList, schedulePieceMap);
            fillSubjectData(subjectData);
            swipeRefreshLayout.setRefreshing(false);
        }
        super.onResume();
    }

    @Override
    public void onRefresh() {
        if (viewCreated) {

            if (scheduleTask != null) {
                scheduleTask.cancel(true);
                scheduleTask = null;
            }

            scheduleTask = new ScheduleAsyncTaskRunner();
            scheduleTask.execute(sessionCookie, numericCode, periodCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.schedule_toolbar_items, menu);
        return true;
    }

    public String capitalizeSubjectTitle(String subjectTitle){
        String returnString;
        returnString = subjectTitle.replaceAll("\\bVi\\b","VI")
                .replaceAll("\\bIv\\b","IV")
                .replaceAll("\\bIii\\b","III")
                .replaceAll("\\bIi\\b","II")

                .replaceAll("\\bFm\\b","FM")
                .replaceAll("\\bQb\\b","QB")
                .replaceAll("\\bEa\\b","EA")
                .replaceAll("\\bHcs\\b","HCS");

        return returnString;
    }

    public String dateToISO(String imperialDate){
        String[] dateList = imperialDate.split("/");
        String[] isoDateList = {dateList[2].trim(), StringUtils.leftPad(dateList[0].trim(),2, "0"),
                StringUtils.leftPad(dateList[1].trim(),2, "0")};
        String isoDate = TextUtils.join("-", isoDateList);
        return isoDate;
    }

    public String orderName(String commaName) {
        String disorderedName = commaName.replace(",", "");
        String[] instructorArray = disorderedName.split("\\s+");
        String secondName = "";

        String instructorName = "";

        if(instructorArray.length > 1) {
            if (instructorArray.length > 3) {
                secondName = instructorArray[3] + " ";
            }

            instructorName = WordUtils.capitalizeFully(instructorArray[2] + " " +
                    secondName + instructorArray[0] + " " + instructorArray[1]);
        } else {
            instructorName = instructorArray[0];
        }

        return instructorName;
    }

    private void fillExpandableList(ArrayList<String> parentList, HashMap<String, ArrayList<SchedulePiece>> childMap){
        sdList.clear();
        spMap.clear();

        sdList.addAll(parentList);
        spMap.putAll(childMap);

        spAdapter.notifyDataSetChanged();
    }

    private void fillSubjectData(ArrayList<String> subjectDatum){

        //Order of data
        // [0] subjectName
        // [1] credits
        // [2] instructorName
        // [3] dateRange
        // [4] sectionCode
        // [5] courseCode

        TextView subjectNameTextView = findViewById(R.id.subjectNameSchedule);
        TextView creditsTextView = findViewById(R.id.creditsSchedule);
        TextView instructorTextView = findViewById(R.id.instructorNameSchedule);
        TextView dateRangeTextView = findViewById(R.id.datesSchedule);
        TextView sectionCodeTextView = findViewById(R.id.sectionSchedule);
        TextView courseCodeTextView = findViewById(R.id.codeSchedule);

        subjectNameTextView.setText(subjectDatum.get(0));
        creditsTextView.setText(getString(R.string.credits_placeholder_grades).concat(" ").concat(subjectDatum.get(1)));
        instructorTextView.setText(subjectDatum.get(2));
        dateRangeTextView.setText(subjectDatum.get(3));
        sectionCodeTextView.setText(subjectDatum.get(4));
        courseCodeTextView.setText(subjectDatum.get(5));

        subjectNameTextView.setVisibility(View.VISIBLE);
        creditsTextView.setVisibility(View.VISIBLE);
        instructorTextView.setVisibility(View.VISIBLE);
        dateRangeTextView.setVisibility(View.VISIBLE);
        sectionCodeTextView.setVisibility(View.VISIBLE);
        courseCodeTextView.setVisibility(View.VISIBLE);
    }

    private class ScheduleAsyncTaskRunner extends AsyncTask<String, String, ArrayList> {

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
                    Document scheduleDocument = getScheduleDocument(serviceCookie, periodCode, numericCode);

                    ArrayList<String> additionalInfo = getAdditionalInfo(scheduleDocument);
                    //AdditionalInfo
                    String subject = additionalInfo.get(0);
                    String credits = additionalInfo.get(1);
                    String instructor = additionalInfo.get(2);
                    String dateRange = additionalInfo.get(3);

                    HashMap<String, ArrayList<SchedulePiece>> schedulePieces = getSchedulePieces(scheduleDocument);

                    returnList.add(subject);
                    returnList.add(credits);
                    returnList.add(instructor);
                    returnList.add(dateRange);

                    returnList.add(schedulePieces);

                } catch (IOException e) {
                    Log.e(TAG, "IOException on ScheduleAsyncTask");
                }

                doBackgroundFinished = true;
            }
            return returnList;
        }

        @Override
        protected void onPostExecute(ArrayList returnList) {
            if (returnList != null && !returnList.isEmpty()){

                ArrayList<String> subjectData = new ArrayList<>();

                String[] subjectTitle = ((String) returnList.get(0)).split(" - ", 2);

                subjectData.add(capitalizeSubjectTitle(WordUtils.capitalizeFully(subjectTitle[1])).trim());

                String localCredits = (String) returnList.get(1);
                subjectData.add(localCredits.trim().replace("Credits", "").trim());

                subjectData.add(orderName((String) returnList.get(2)).trim());

                String[] dateRangeList = ((String) returnList.get(3)).split("-");

                subjectData.add(dateToISO(dateRangeList[0]).concat(" / ").concat(dateToISO(dateRangeList[1])).trim());

                String[] subjectCodes = subjectTitle[0].split("/", 3);

                subjectData.add(subjectCodes[2].trim());
                subjectData.add(subjectCodes[0].trim());

                schedulePieceMap = (HashMap<String, ArrayList<SchedulePiece>>) returnList.get(4);
                scheduleDayList.clear();
                scheduleDayList.addAll(schedulePieceMap.keySet());

                Collections.sort(scheduleDayList, new Comparator<String>() {
                    @Override public int compare(String s1, String s2) {

                        String[] days = new String[]{s1,s2};
                        int[] dayIntArray = new int[2];

                        int index = 0;
                        for (String weekDay: days){
                            int dayInt = 0;
                            switch (weekDay) {
                                case "Lunes": dayInt = 2;
                                    break;
                                case "Martes": dayInt = 3;
                                    break;
                                case "Miercoles": dayInt = 4;
                                    break;
                                case "Jueves": dayInt = 5;
                                    break;
                                case "Viernes": dayInt = 6;
                                    break;
                                case "Sabado": dayInt = 7;
                                    break;
                                case "Domingo": ;
                                    break;
                            }
                            dayIntArray[index] = dayInt;
                            index++;
                        }

                        return dayIntArray[0] - dayIntArray[1]; // Ascending
                    }
                });

                fillExpandableList(scheduleDayList, schedulePieceMap);
                fillSubjectData(subjectData);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private Document getScheduleDocument(String serviceCookie, String periodCode, String numericCode) throws IOException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        Connection.Response schedulePageGet = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/ClassSchedule.aspx")
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute();

        Document scheduleDocument = schedulePageGet.parse();

        Element viewState = scheduleDocument.select("input[name=__VIEWSTATE]").first();
        Element eventValidation = scheduleDocument.select("input[name=__EVENTVALIDATION]").first();

        Document scheduleDocumentPost = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/ClassSchedule.aspx")

                .data("ctl00$pageOptionsZone$ddlbPeriods", periodCode)
                .data("__EVENTARGUMENT", numericCode)
                .data("__EVENTTARGET", "ShowDetail")
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .cookies(cookies)
                .post();

        return scheduleDocumentPost;
    }

    private HashMap<String, ArrayList<SchedulePiece>> getSchedulePieces(Document scheduleDocument){
        HashMap<String, ArrayList<SchedulePiece>> returnHashMap = new HashMap<>();
        ArrayList<SchedulePiece> schedulePieces = new ArrayList<>();
        String schedule = scheduleDocument.getElementById("ctl00_mainContentZone_ucSectionDetail_lblSchedule").parent().parent().getElementsByTag("td").get(1).html();

        String[] pieces = schedule.split("<br>");

        for (String piece: pieces) {
            if(!piece.isEmpty()){
                SchedulePiece newSchedulePiece = new SchedulePiece(piece.replace("&nbsp;", "").trim());
                schedulePieces.add(newSchedulePiece);
            }
        }

        for (SchedulePiece schedulePiece: schedulePieces){
            if(!returnHashMap.containsKey(schedulePiece.getWeekDay())){
                ArrayList<SchedulePiece> daySchedulePieceList = new ArrayList<>();
                returnHashMap.put(schedulePiece.getWeekDay(), daySchedulePieceList);
            }

            returnHashMap.get(schedulePiece.getWeekDay()).add(schedulePiece);
        }

        return returnHashMap;
    }

    private ArrayList<String> getAdditionalInfo(Document scheduleDocument) throws IOException {
        ArrayList<String> returnList = new ArrayList<>();

        Element subjectName = scheduleDocument.getElementsByClass("leveloneheader").first();
        String credits = subjectName.parent().ownText();
        String instructor = scheduleDocument.getElementById("ctl00_mainContentZone_ucSectionDetail_lblInstructors").parent().parent().getElementsByTag("td").get(1).ownText();
        String dateRange = scheduleDocument.getElementById("ctl00_mainContentZone_ucSectionDetail_lblDuration").parent().parent().getElementsByTag("td").get(1).ownText();

        returnList.add(subjectName.ownText());
        returnList.add(credits.replace("|", "").trim());
        returnList.add(instructor);
        returnList.add(dateRange);

        return returnList;
    }
}