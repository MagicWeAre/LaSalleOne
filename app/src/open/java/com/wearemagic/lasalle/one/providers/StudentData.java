package com.wearemagic.lasalle.one.providers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.ChargeCredit;
import com.wearemagic.lasalle.one.objects.GradeSubject;
import com.wearemagic.lasalle.one.objects.ParcialGrade;
import com.wearemagic.lasalle.one.objects.SchedulePiece;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;
import com.wearemagic.lasalle.one.objects.SummaryType;

import org.apache.commons.text.WordUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentData {

    private static final String TAG = "LaSalleOne";
    private static String baseURL = "https://miportal.ulsaoaxaca.edu.mx/ss/";

    // ScheduleSubjectDetailActivity
    public static Document getScheduleDocument(String serviceCookie, String periodCode, String numericCode) throws IOException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);
        String appendedURL =  "Records/ClassSchedule.aspx";

        Connection.Response schedulePageGet = Jsoup.connect(baseURL + appendedURL)
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute();

        Document scheduleDocument = schedulePageGet.parse();

        if(scheduleDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element viewState = scheduleDocument.select("input[name=__VIEWSTATE]").first();
        Element eventValidation = scheduleDocument.select("input[name=__EVENTVALIDATION]").first();

        Document scheduleDocumentPost = Jsoup.connect(baseURL + appendedURL)

                .data("ctl00$pageOptionsZone$ddlbPeriods", periodCode)
                .data("__EVENTARGUMENT", numericCode)
                .data("__EVENTTARGET", "ShowDetail")
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .cookies(cookies)
                .post();

        return scheduleDocumentPost;
    }

    public static HashMap<String, ArrayList<SchedulePiece>> getSchedulePieces(Document scheduleDocument){
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

    public static ArrayList<String> getScheduleAdditionalInfo(Document scheduleDocument) throws IOException {
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

    // GradeSubjectDetailActivity
    public static Document getParcialGradesDocument(String serviceCookie, String periodCode, String numericCode) throws IOException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        Document parcialGradesDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/ActivityGradesReport.aspx")
                .data("termperiod", periodCode)
                .data("SectionId", numericCode)
                .cookies(cookies)
                .get();

        return parcialGradesDocument;
    }

    public static ArrayList<String> getGradeAdditionalInfo(Document parcialGradeDocument) throws LoginTimeoutException {
        ArrayList<String> returnList = new ArrayList<>();

        if(parcialGradeDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Boolean dataAvailable = parcialGradeDocument.getElementsByClass("msgNoData").isEmpty();

        if (dataAvailable) {

            String subjectName = parcialGradeDocument.getElementsByTag("h2").first().ownText().substring(24);

            String instructorName = parcialGradeDocument.getElementsByAttributeValue("style", "font-size:0.6em;").first().text();

            returnList.add(subjectName);
            returnList.add(instructorName);
        }

        return returnList;
    }

    public static ArrayList<ParcialGrade> getParcialGrades(Document parcialGradeDocument) {
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

    // SubjectsFragment
    public static ArrayList<ScheduleSubject> getSubjects(String serviceCookie, String period, boolean firstGet) throws IOException, LoginTimeoutException {
        ArrayList<ScheduleSubject> returnList = new ArrayList<>();
        String appendedURL = "Records/ClassSchedule.aspx";

        Map<String, String> cookies = new HashMap<String, String>();
        cookies.put("SelfService", serviceCookie);

        Connection.Response subjectsPageGet = Jsoup.connect(baseURL + appendedURL)
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute();

        Document subjectsDocument = subjectsPageGet.parse();

        if(subjectsDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element viewState = subjectsDocument.select("input[name=__VIEWSTATE]").first();
        Element eventValidation = subjectsDocument.select("input[name=__EVENTVALIDATION]").first();

        if (firstGet) {
            Jsoup.connect(baseURL + appendedURL)
                    .data("ctl00$pageOptionsZone$ddlbPeriods", period)
                    .data("__EVENTTARGET", "ShowText")
                    .data("__VIEWSTATE", viewState.attr("value"))
                    .data("__EVENTVALIDATION", eventValidation.attr("value"))
                    .cookies(cookies)
                    .post();
        }
        Document subjectsDocumentPost = Jsoup.connect(baseURL + appendedURL)
                .data("ctl00$pageOptionsZone$ddlbPeriods", period)
                .data("__EVENTTARGET", "ctl00$pageOptionsZone$ddlbPeriods")
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .cookies(cookies)
                .post();

        Element subjectsTable = subjectsDocumentPost.getElementsByAttributeValue("style", "table-layout:fixed").first();
        Elements subjectNames = subjectsTable.getElementsByAttribute("title");
        Elements durations = subjectsTable.getElementsMatchingOwnText("Duration");
        Elements credits = subjectsTable.getElementsMatchingOwnText("Credits");
        Elements instructors = subjectsTable.getElementsMatchingOwnText("Instructors");
        Elements preScheduleTables = subjectsTable.getElementsByAttributeValue("align", "left");

        ArrayList<String> scheduleNameList = new ArrayList<>();
        ArrayList<String> scheduleNumericCodeList = new ArrayList<>();
        ArrayList<String> scheduleDurationList = new ArrayList<>();
        ArrayList<String> scheduleCreditList = new ArrayList<>();
        ArrayList<String> scheduleInstructorList = new ArrayList<>();
        ArrayList<ArrayList<SchedulePiece>> schedulePieceList = new ArrayList<>();

        for (Element subject : subjectNames) {
            if (!subject.text().isEmpty()) {
                scheduleNameList.add(subject.text());
                scheduleNumericCodeList.add(subject.attr("href").split("'")[3]);
            }
        }

        for (Element duration : durations) {
            String durationStr = duration.parent().ownText();
            if (!durationStr.isEmpty()) {
                scheduleDurationList.add(durationStr);
            }
        }

        for (Element credit : credits) {
            String creditStr = credit.parent().ownText();
            if (!creditStr.isEmpty()) {
                scheduleCreditList.add(creditStr);
            }
        }

        for (Element instructor : instructors) {
            String instructorStr = instructor.parent().ownText();
            scheduleInstructorList.add(instructorStr);
        }

        for (Element scheduleTable : preScheduleTables) {
            ArrayList<SchedulePiece> schedulePieces = new ArrayList<>();

            String scheduleStr = scheduleTable.nextElementSibling().html();

            if (!scheduleStr.isEmpty()) {

                String[] pieces = scheduleStr.split("<br>");

                for (String piece : pieces) {
                    if (!piece.isEmpty()) {
                        SchedulePiece newSchedulePiece = new SchedulePiece(piece.replace("&nbsp;", "").trim());
                        schedulePieces.add(newSchedulePiece);
                    }
                }
            }

            schedulePieceList.add(schedulePieces);
        }

        int rowNumber = 0;
        for (String subjectName : scheduleNameList) {
            String subjectNumericCode = scheduleNumericCodeList.get(rowNumber);
            String subjectDuration = scheduleDurationList.get(rowNumber);
            String subjectCredit = scheduleCreditList.get(rowNumber);
            String subjectInstructor = scheduleInstructorList.get(rowNumber);
            ArrayList<SchedulePiece> subjectSchedule = schedulePieceList.get(rowNumber);

            ScheduleSubject scheduleSubject = new ScheduleSubject(subjectName, subjectDuration,
                    subjectCredit, subjectInstructor, subjectNumericCode, period);

            scheduleSubject.setScheduleList(subjectSchedule);

            returnList.add(scheduleSubject);

            rowNumber++;
        }

        return returnList;
    }

    // ProfileFragment
    public static void savePicture(String moodleCookie, String userName, Context context) throws IOException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("MoodleSession", moodleCookie);

        Document profilePicDocument = Jsoup.connect("http://micurso.ulsaoaxaca.edu.mx/user/profile.php")
                .cookies(cookies)
                .get();

        Element profilePicture = profilePicDocument.getElementsByAttributeValueStarting("class", "userpicture").first();
        String pictureLink = profilePicture.attr("src");
        String linkTemplate = pictureLink.replace("f1", "f3");

        HttpURLConnection imageConnection = (HttpURLConnection) new URL(linkTemplate).openConnection();
        imageConnection.setRequestMethod("GET");
        imageConnection.addRequestProperty("Cookie","MoodleSession=" + moodleCookie);

        InputStream imageInput = imageConnection.getInputStream();
        Bitmap profileBitmap = BitmapFactory.decodeStream(imageInput);
        String filename = userName + ".jpg";

        File file = new File(context.getFilesDir(), filename);

        try {
            OutputStream stream = new FileOutputStream(file);

            profileBitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
            stream.flush();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getProfile(String sessionCookie) throws IOException, LoginTimeoutException {
        String idBase = "ctl00_mainContentZone_LoginInformationControl_LoginInfoFormView_";
        String addressBase = "ctl00_mainContentZone_ucEditAddress_";

        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", sessionCookie);

        Document perfilDocument = Jsoup.connect(baseURL + "Account/LoginInformation.aspx")
                .cookies(cookies)
                .get();

        if(perfilDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element userName = perfilDocument.getElementById(idBase + "UserNameField_UserNameFieldValueLabel");
        Element firstName = perfilDocument.getElementById(idBase + "FirstNameField_FirstNameFieldValueLabel");
        Element middleName = perfilDocument.getElementById(idBase + "MiddleNameField_MiddleNameFieldValueLabel");
        Element lastName = perfilDocument.getElementById(idBase + "LastNameField_LastNameFieldValueLabel");
        Element lastNamePrefix = perfilDocument.getElementById(idBase + "LastNamePrefixField_LastNamePrefixFieldValueLabel");
        Element email = perfilDocument.getElementById(idBase + "EmailField_EmailFieldValueLabel");

        Document phoneDocument = Jsoup.connect(baseURL + "Account/PhoneNumbers.aspx")
                .cookies(cookies)
                .get();

        Element mainPhoneTable = phoneDocument.getElementById("primaryphone");
        Element otherPhoneTable = phoneDocument.getElementById("otherphone");

        Map<String, String> phones = new HashMap<>();

        if (mainPhoneTable != null) {
            phones.put(mainPhoneTable.getElementsByTag("td").get(0).text(), mainPhoneTable.getElementsByTag("td").get(2).text()); }
        if (otherPhoneTable != null) {
            phones.put(otherPhoneTable.getElementsByTag("td").get(0).text(), otherPhoneTable.getElementsByTag("td").get(2).text()); }

        Document curpDocument = Jsoup.connect(baseURL + "Records/Transcripts.aspx")
                .cookies(cookies)
                .get();

        Element curpElement = curpDocument.getElementsByAttribute("colspan").first().nextElementSibling();
        String curp = curpElement.text().replace("ID:", "").trim();

        /*Document addressDocument = Jsoup.connect(baseURL + "Account/ChangeAddress.aspx")
                .cookies(cookies)
                .get();

        String addressLink = addressDocument.getElementById("ctl00_mainContentZone_ucChangeAddress_EditAddress_Hyperlink").attr("abs:href");

        Document editAddressDocument = Jsoup.connect(addressLink)
                .cookies(cookies)
                .get();

        String houseNumber = editAddressDocument.getElementById(addressBase + "AddressHouseNumberTextBox").attr("value");
        String addressLineOne = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox").attr("value");
        String addressLineTwo = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox2").attr("value");
        String addressLineThree = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox3").attr("value");
        String addressLineFour = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox4").attr("value");
        String city = editAddressDocument.getElementById(addressBase + "AddressCityTextBox").attr("value");
        String postalCode = editAddressDocument.getElementById(addressBase + "AddressZipCodeTextBox").attr("value");

        Element stateDropdown = editAddressDocument.getElementById("ctl00_mainContentZone_ucEditAddress_AddressStateDropDown");
        String state = stateDropdown.getElementsByAttribute("selected").first().text();

        Element countryDropdown = editAddressDocument.getElementById("ctl00_mainContentZone_ucEditAddress_AddressCountryDropDown");
        String country = countryDropdown.getElementsByAttribute("selected").first().text();*/


        Element[] elementArray = {userName, firstName, middleName, lastName, lastNamePrefix, email};
        //String[] addressArray = {houseNumber, addressLineOne, addressLineTwo, addressLineThree, addressLineFour, city, postalCode, state, country};

        //String[] returnArray = new String[elementArray.length + addressArray.length + 2];
        String[] returnArray = new String[elementArray.length + 2];

        for (int i = 0; i < elementArray.length; i++) {
            returnArray[i] = WordUtils.capitalizeFully(elementArray[i].text()); }

        returnArray[elementArray.length] = ((phones.get("Celular") == null) ? "" : phones.get("Celular"));
        returnArray[elementArray.length + 1 ] = ((phones.get("Casa") == null) ? "" : phones.get("Casa"));

        //System.arraycopy(addressArray, 0, returnArray, 8, addressArray.length);

        ArrayList<String> profileArrayList = new ArrayList<>();
        profileArrayList.addAll(Arrays.asList(returnArray));
        profileArrayList.add(curp);

        return profileArrayList;
    }

    // GradesFragment
    public static ArrayList<GradeSubject> getGradeSubjects(String serviceCookie, String period) throws IOException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        Connection.Response balancePageGet = Jsoup.connect(baseURL + "Records/GradeReport.aspx")
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute();

        Document gradesDocument = balancePageGet.parse();

        if(gradesDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element viewState = gradesDocument.select("input[name=__VIEWSTATE]").first();
        Element eventValidation = gradesDocument.select("input[name=__EVENTVALIDATION]").first();

        Document gradesDocumentPost = Jsoup.connect(baseURL + "Records/GradeReport.aspx")

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

    // CreditsFragment | ChargesFragment
    public static List getChargesCredits(String serviceCookie, String periodLocal, boolean credits) throws IOException, IllegalArgumentException, LoginTimeoutException {
        // [Credits] [TotalCredits]
        List returnList = new ArrayList<>();
        String creditsID = "ctl00_mainContentZone_ucAccountBalance_CreditDetailsUserControl_gvBalanceDetails";
        String chargesID = "ctl00_mainContentZone_ucAccountBalance_ChargeDetailsUserControl_gvBalanceDetails";

        String elementID = creditsID;

        if (!credits) {
            elementID = chargesID;
        }

        // Credits Table
        Element creditsTable = getBalanceDocument(serviceCookie, periodLocal, true).getElementById(elementID);
        String totalCredits = getTotalCC(creditsTable);
        List<ChargeCredit> creditsList = getChargesCreditsList(creditsTable, periodLocal);

        returnList.add(creditsList);
        returnList.add(totalCredits);

        return returnList;
    }

    private static Document getBalanceDocument(String serviceCookie, String periodLocal, boolean getCC) throws IOException, IllegalArgumentException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        String ccType = "ChargeCreditDetail";
        String summaryType = "Summary";
        String getType = summaryType;

        if (getCC) {
            getType = ccType;
        }

        Connection.Response balancePageGet = Jsoup.connect(baseURL + "Finances/Balance.aspx")
            .method(Connection.Method.GET)
            .cookies(cookies)
            .execute();

        Document balanceDocument = balancePageGet.parse();

        if(balanceDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element viewState = balanceDocument.select("input[name=__VIEWSTATE]").first();
        Element eventValidation = balanceDocument.select("input[name=__EVENTVALIDATION]").first();
        Element viewStateEncrypted = balanceDocument.select("input[name=__VIEWSTATEENCRYPTED]").first();

        Document balanceDocumentPost = Jsoup.connect(baseURL + "Finances/Balance.aspx")
            .data("ctl00$pageOptionsZone$btnSubmit", "Change")
            .data("ctl00$pageOptionsZone$ucBalanceOptions$ucbalanceViewOptions$ViewsButtonList", getType)
            .data("ctl00$pageOptionsZone$ucFinancialPeriods$PeriodsDropDown$ddlbPeriods", periodLocal)
            .data("__VIEWSTATE", viewState.attr("value"))
            .data("__EVENTVALIDATION", eventValidation.attr("value"))
            .data("__VIEWSTATEENCRYPTED", viewStateEncrypted.attr("value"))
            .cookies(cookies)
            .post();

        return balanceDocumentPost;
    }

    private static List<ChargeCredit> getChargesCreditsList(Element table, String periodLocal) {
        List<ChargeCredit> movementsList = new ArrayList<>();
        Elements tableRows = table.getElementsByTag("tr");
        tableRows.remove(tableRows.size() -1);

        for (Element row : tableRows) {
            if (!row.hasAttr("class")) {
                Elements cells = row.getElementsByTag("td");
                ChargeCredit chargeCredit = new ChargeCredit(row.getElementsByTag("span").first().text(),
                        cells.get(3).text(), cells.get(0).text(), periodLocal, cells.get(2).text());
                movementsList.add(chargeCredit);
            }
        }

        return movementsList;
    }

    private static String getTotalCC(Element table) {
        Elements ccTableRows = table.getElementsByTag("tr");
        Element finalRowCharges = ccTableRows.last();
        String total =  finalRowCharges.getElementsByTag("span").first().text();

        if(total.equals("No charges exist for the selected period.") || total.equals("No credits exist for the selected period.")){
            total = "$0.00";
        } else {
            total = total.split(":")[1].trim();
        }
        return total;
    }

    // BalanceFragment
    public static ArrayList<ArrayList> getSummary(String serviceCookie, String period) throws IOException, LoginTimeoutException {
        ArrayList<ArrayList> returnList = new ArrayList<>();
        Element summaryTable = getBalanceDocument(serviceCookie, period, false).getElementById("ctl00_mainContentZone_ucAccountBalance_gvSummaryTypeTotals");
        ArrayList<SummaryType> summaryTypeList = getSummaryList(summaryTable, period);
        ArrayList<String> totalsList = getTotalSL(summaryTable);

        returnList.add(summaryTypeList);
        returnList.add(totalsList);

        return returnList;

        //"Total: " + totalsList.get(0);
        //"From other periods: " + totalsList.get(1);
        //"Balance: " + totalsList.get(2);
    }

    // Lower rows
    private static ArrayList<SummaryType> getSummaryList(Element table, String period) {
        ArrayList<SummaryType> summaryList = new ArrayList<>();
        Elements tableRows = table.getElementsByTag("tr");
        for (Element row : tableRows) {
            if (!row.hasAttr("class")) {
                SummaryType summaryType = new SummaryType(row.getElementsByAttribute("align").first().text(), row.getElementsByTag("span").first().text(), period);
                summaryList.add(summaryType);
            }
        }
        return summaryList;
    }

    // Upper three data
    private static ArrayList<String> getTotalSL(Element table) {
        ArrayList<String> summaryList = new ArrayList<>();
        Elements labels = table.getElementsByAttributeValue("class", "label");

        summaryList.add(assignSign(labels.get(3).text(), false));
        summaryList.add(assignSign(labels.get(4).text(), false));
        summaryList.add(assignSign(labels.get(5).text(), true));

        return summaryList;
    }

    private static String assignSign(String amount, boolean onlyNegative) {
        String a;
        if (amount.startsWith("(")) {
            a = amount.replace("(", "").replace(")", "");
            if (!onlyNegative){
                a = "+ " + a;
            }
        } else {
            a = "- " + amount;
        }
        return a;
    }
}
