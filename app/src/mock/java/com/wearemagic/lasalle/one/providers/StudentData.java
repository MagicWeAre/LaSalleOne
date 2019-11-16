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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import static java.lang.Math.round;

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

            // Not-so inside joke
            if (instructorName.contains("FORTINO")) {
                instructorName = instructorName.replace("FORTINO", "FORTNITO");
            }

            returnList.add(subjectName);
            returnList.add(instructorName);
        }

        return returnList;
    }

    public static ArrayList<ParcialGrade> getParcialGrades(Document parcialGradeDocument) {
        ArrayList<ParcialGrade> returnList = new ArrayList<>();
        Random gradeRand = new Random();

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
                int randomGrade = gradeRand.nextInt(5) + 6;
                Elements parcialData = courseInfoRow.getElementsByTag("td");

                String parcialName = parcialData.get(0).text();
                String dueDate = parcialData.get(2).text();
                String pointsEarned = String.format(Locale.ENGLISH, "%d.00", randomGrade);
                String maxPoints = parcialData.get(4).text();
                String percentaje = round(randomGrade * 166.6) / 100 + "%";
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
        Random dateRand = new Random();

        String[] courseNames = new String[] {"/Teorico/FR99 - FRANCES II",
                "/Teorico/600 - ECOLOGIA Y MEDIO AMBIENTE",
                "/Teorico/600 - FORMACION VALORAL VI",
                "/Teorico/600 - FILOSOFIA",
                "/Teorico/SC99 - PRACTICAR EN EL CAMPO APLICANDO METODOS",
                "/Teorico/600 - METODOLOGIA DE LA INVESTIGACION"};

        String[] courseInstructors = new String[] {"VIGNON, PEREZ NORA IVONNE", "GONZALEZ, LIZET GUADALUPE",
                "ROSALES, TOVAR JAVIER", "HERNANDEZ, MARTINEZ RUBEN FERNANDO", "", "CORNELIO, CRUZ MIGUEL"};

        for (int i = 0; i < courseNames.length; i++) {
            String numeric = String.format(Locale.ENGLISH, "%03d", dateRand.nextInt(999) + 1);

            ScheduleSubject scheduleSubject = new ScheduleSubject(numeric + courseNames[i], "30/01/2020 - 17/06/2020",
                    "6.00 Credito Bachillerato", courseInstructors[i], numeric, period);

            returnList.add(scheduleSubject);
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

        String[] returnArray = new String[] {"014400000", "Jean-Baptiste", "", "De La", "Salle", "contacto@wearemagic.dev", "(951) 502 93 33", "01 800 502 93 33"};

        ArrayList<String> profileArrayList = new ArrayList<>();
        profileArrayList.addAll(Arrays.asList(returnArray));
        profileArrayList.add("JUANLASALLE");

        return profileArrayList;
    }

    // GradesFragment
    public static ArrayList<GradeSubject> getGradeSubjects(String serviceCookie, String period) throws IOException, LoginTimeoutException {
        ArrayList<GradeSubject> CoursesGradesList = new ArrayList<>();

        String[] courseNames = new String[] {"FRANCÉS II", "ECOLOGÍA Y MEDIO AMBIENTE", "FORMACIÓN VALORAL VI",
                "FILOSOFÍA", "PRACTICAR EN EL CAMPO APLICANDO METODOS", "METODOLOGIA DE LA INVESTIGACION"};
        String[] courseGrades = new String[] {"5.60", "9.0", "9.8", "10.0", "8.90", "5.9"};

        for (int i = 0; i < courseNames.length; i++) {

            GradeSubject gradeSubject = new GradeSubject("49894", period, courseNames[i], "030 Teorico", "600", "6.00");
            gradeSubject.setQualityPoints(courseGrades[i]);
            gradeSubject.setProjectedGrade("");
            gradeSubject.setFinalGrade(courseGrades[i]);

            CoursesGradesList.add(gradeSubject);
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
        Map<String, String> cookies = new HashMap<String, String>();
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

        Random dateRand = new Random();
        SimpleDateFormat isoSDF = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);

        for (Element row : tableRows) {
            int daysNumber = dateRand.nextInt(180) + 1;

            Calendar calendar = Calendar.getInstance(); // this would default to now
            calendar.add(Calendar.DAY_OF_YEAR, -daysNumber);

            Date randomDate = calendar.getTime();

            // Conversion
            String dateStr = isoSDF.format(randomDate);

            if (!row.hasAttr("class")) {
                Elements cells = row.getElementsByTag("td");
                ChargeCredit chargeCredit = new ChargeCredit(row.getElementsByTag("span").first().text(),
                        cells.get(3).text(), dateStr, periodLocal, cells.get(2).text());
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

    private static ArrayList<SummaryType> getSummaryList(Element table, String period) {
        ArrayList<SummaryType> summaryList = new ArrayList<>();

        summaryList.add(new SummaryType("($10,000.00)", "Becas", period));
        summaryList.add(new SummaryType("$10,000.00", "Colegiaturas", period));
        summaryList.add(new SummaryType("($400)", "Descuentos", period));
        summaryList.add(new SummaryType("$3,800.00", "Inscripción", period));
        summaryList.add(new SummaryType("($3,800.00)", "Pagos", period));

        return summaryList;
    }

    private static ArrayList<String> getTotalSL(Element table) {
        ArrayList<String> summaryList = new ArrayList<>();

        summaryList.add("+ $400.00");
        summaryList.add("- $200.00");
        summaryList.add("$200.00");

        return summaryList;
    }
}
