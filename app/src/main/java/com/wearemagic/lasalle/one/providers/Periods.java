package com.wearemagic.lasalle.one.providers;

import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Periods {
    private static String baseURL = "https://miportal.ulsaoaxaca.edu.mx/ss/";

    // Primary method to retrieve period information, reworked to reuse code
    public static ArrayList<ArrayList<ArrayList<String>>> getPeriods(String sessionCookie) throws IOException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", sessionCookie);

        ArrayList<ArrayList<ArrayList<String>>> returnList = new ArrayList<>();

        // Subjects Periods
        ArrayList<ArrayList<String>> subjectsPeriodList = getPeriod("Records/ClassSchedule.aspx",
                "ctl00_pageOptionsZone_ddlbPeriods",
                cookies, true, true);

        // Grades Periods
        ArrayList<ArrayList<String>> gradesPeriodList = getPeriod("Records/GradeReport.aspx",
                "ctl00_pageOptionsZone_GradeReportOptions_PeriodDropDown",
                cookies, false, false);

        // Balance Periods
        ArrayList<ArrayList<String>> balancePeriodList = getPeriod("Finances/Balance.aspx",
                "ctl00_pageOptionsZone_ucFinancialPeriods_PeriodsDropDown_ddlbPeriods",
                cookies, false, false);

        returnList.add(subjectsPeriodList);
        returnList.add(gradesPeriodList);
        returnList.add(balancePeriodList);

        return returnList;
    }

    private static ArrayList<ArrayList<String>> getPeriod(String periodURL, String parsedElementID,
                                                   Map<String, String> cookieMap, boolean checkLTException, boolean omitRepetitions) throws IOException, LoginTimeoutException {

        // We reuse the base string
        // exampleURL: "Records/ClassSchedule.aspx"
        Document periodDocument = Jsoup.connect(baseURL.concat(periodURL))
                .cookies(cookieMap)
                .get();

        // Only check for LTException once, obviously
        if(checkLTException && periodDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        ArrayList<String> periodNames = new ArrayList<>();
        ArrayList<String> periodValues = new ArrayList<>();

        if(periodDocument.getElementsByClass("msgWarning").isEmpty()){
            Elements periods = periodDocument.getElementById(parsedElementID).getElementsByTag("option");

            for (Element period : periods) {
                boolean periodNecessary = true;
                if (omitRepetitions) {
                    periodNecessary = period.attr("value").endsWith("|");
                }

                if (periodNecessary) {
                    periodNames.add(period.text());
                    periodValues.add(period.attr("value"));
                }
            }
        } else {
            periodValues = null;
        }

        ArrayList<ArrayList<String>> periodList = new ArrayList<>();
        periodList.add(periodNames);
        periodList.add(periodValues);

        return periodList;
    }

}
