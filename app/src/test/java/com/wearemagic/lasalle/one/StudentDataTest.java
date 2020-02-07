package com.wearemagic.lasalle.one;

import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.ChargeCredit;
import com.wearemagic.lasalle.one.objects.GradeSubject;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;
import com.wearemagic.lasalle.one.objects.SummaryType;
import com.wearemagic.lasalle.one.providers.Logins;
import com.wearemagic.lasalle.one.providers.Periods;
import com.wearemagic.lasalle.one.providers.StudentData;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class StudentDataTest {
    private static final String TAG = "LaSalleOne";
    public String baseURL = "https://miportal.ulsaoaxaca.edu.mx/ss/";

    private ArrayList<ArrayList<ArrayList<String>>> periodArray;
    private ArrayList<String> subjectsPeriodsNames;
    private ArrayList<String> subjectsPeriodsValues;

    private ArrayList<String> gradesPeriodsNames;
    private ArrayList<String> gradesPeriodsValues;

    private ArrayList<String> balancePeriodsNames;
    private ArrayList<String> balancePeriodsValues;

    private String sessionCookie = "";
    // Logins
    private String myID = "014403980";
    private String myPass = "xzJhcsL7J3";

    // doLogin
    @Before
    public void setUp(){
        // Login
        try {
            sessionCookie = Logins.doLogin(myID, myPass, baseURL);
        } catch (IOException io) {
            System.out.println(io.getMessage());
            fail();
        }

        assertNotNull(sessionCookie);
        assertFalse(sessionCookie.isEmpty());

        // Periods
        try {
            periodArray = Periods.getPeriods(sessionCookie);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }

        // - Subjects
        ArrayList<ArrayList<String>> subjectsPeriods = periodArray.get(0);
        assertNotNull(subjectsPeriods);

        subjectsPeriodsNames = subjectsPeriods.get(0);
        subjectsPeriodsValues = subjectsPeriods.get(1);
        assertEquals(subjectsPeriodsNames.size(), subjectsPeriodsValues.size());

        // - Grades
        ArrayList<ArrayList<String>> gradesPeriods = periodArray.get(1);
        assertNotNull(gradesPeriods);

        gradesPeriodsNames = gradesPeriods.get(0);
        gradesPeriodsValues = gradesPeriods.get(1);
        assertEquals(gradesPeriodsNames.size(), gradesPeriodsValues.size());

        // - Balance
        ArrayList<ArrayList<String>> balancePeriods = periodArray.get(2);
        assertNotNull(balancePeriods);

        balancePeriodsNames = balancePeriods.get(0);
        balancePeriodsValues = balancePeriods.get(1);
        assertEquals(balancePeriodsNames.size(), balancePeriodsValues.size());

        boolean allPeriodsValuesEmpty = subjectsPeriodsValues.isEmpty() &&
                gradesPeriodsValues.isEmpty() && balancePeriodsValues.isEmpty();

        assertFalse(allPeriodsValuesEmpty);
    }

    // Periods
    @Test(expected = LoginTimeoutException.class)
    public void testGetPeriods_InvalidCookie() throws LoginTimeoutException {
        try {
            Periods.getPeriods("");
        } catch (IOException io) {
            fail();
        }
    }

    public HashMap<String, String> generateLoginMap() {
        HashMap<String, String> loginMap = new HashMap<>();

        // 40 Different Logins
        loginMap.put("014403980", "xzJhcsL7J3");
        loginMap.put("014406496", "OD69KNkE");
        loginMap.put("014403932", "Qk1AnVTC");
        loginMap.put("014404010", "e1RxVJ1x");
        loginMap.put("014400132", "y2qdyWg7l");
        loginMap.put("014405942", "l41O6E1r");
        loginMap.put("014406477", "5IPik0K6");
        loginMap.put("014406243", "W4W7rQRO");
        loginMap.put("014210055", "319b4e298");
        loginMap.put("014210167", "55f920e34");
        // Next 10
        loginMap.put("014400202", "cHXgiX94");
        loginMap.put("014404388", "3y6iR1DSOS");
        loginMap.put("014403736", "1B7Ao7U4uj");
        loginMap.put("014404137", "W5EDqFDb");
        loginMap.put("014210007", "7c6e28290");
        loginMap.put("014406701", "Tc7zf07wc");
        loginMap.put("014400512", "ZklZ1XY0");
        loginMap.put("014401608", "xxWWoElu");
        loginMap.put("014210179", "17cb215f4");
        loginMap.put("014401593", "43T6w9H3");
        // Next 10
        loginMap.put("014400699", "6XVI243b");
        loginMap.put("014400348", "aJ38aDI0XQ");
        loginMap.put("014406509", "73jV2tMq");
        loginMap.put("014404516", "ATnA41SZ");
        loginMap.put("014407631", "rzgpL1T4");
        loginMap.put("014403738", "Vu4oG5g8");
        loginMap.put("014405914", "iF49ic06");
        loginMap.put("014403837", "87Rz5kvd");
        loginMap.put("014404648", "RTGfXt6h");
        loginMap.put("014404370", "iwq28cc8");
        // Next 10
        loginMap.put("014403890", "7SGQSOs3");
        loginMap.put("014401603", "195m385t");
        loginMap.put("014405700", "7R5h423W");
        loginMap.put("014404467", "l2eNj65ecU");
        loginMap.put("014404111", "kws5dcex");
        loginMap.put("014406498", "RY9o0J7ZT");
        loginMap.put("014406241", "c0X01Gqy");
        loginMap.put("014406556", "280821FGBS");
        loginMap.put("014404059", "OjYNUipK");
        loginMap.put("014401616", "RC9d4zg5");

        return loginMap;
    }

    // Profile
    @Test
    public void testStudentData_RetrieveProfile() {

        try {
            // Profile
            ArrayList<String> profileArray = StudentData.getProfile(sessionCookie);

            assertNotNull(profileArray);
            assertFalse(profileArray.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    // Subjects
    @Test
    public void testStudentData_RetrieveSubjectsLatestPeriod() throws IOException, LoginTimeoutException {
        try {
            ArrayList<ScheduleSubject> subjectArray = StudentData.getSubjects
                    (sessionCookie, subjectsPeriodsValues.get(0), true);

            assertNotNull(subjectArray);
            assertFalse(subjectArray.isEmpty());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }

    }

    @Test
    public void testStudentData_RetrieveSubjectsEarliestPeriod() throws IOException, LoginTimeoutException {
        try {
            int lastItem = subjectsPeriodsValues.size() - 1;

            ArrayList<ScheduleSubject> subjectArray = StudentData.getSubjects
                    (sessionCookie, subjectsPeriodsValues.get(lastItem), true);

            assertNotNull(subjectArray);
            assertFalse(subjectArray.isEmpty());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }

    }

    // Grades
    @Test
    public void testStudentData_RetrieveGradesLatestPeriod() throws IOException, LoginTimeoutException {
        try {
            ArrayList<GradeSubject> gradeArray =
                    StudentData.getGradeSubjects(sessionCookie, gradesPeriodsValues.get(0));

            assertNotNull(gradeArray);
            assertFalse(gradeArray.isEmpty());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test
    public void testStudentData_RetrieveGradesEarliestPeriod() throws IOException, LoginTimeoutException {
        try {
            int lastItem = gradesPeriodsValues.size() - 1;

            ArrayList<GradeSubject> gradeArray =
                    StudentData.getGradeSubjects(sessionCookie, gradesPeriodsValues.get(lastItem));

            assertNotNull(gradeArray);
            assertFalse(gradeArray.isEmpty());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    // Balance
    @Test
    public void testStudentData_RetrieveBalanceLatestPeriod() throws IOException, LoginTimeoutException {
        try {
            ArrayList<ArrayList> balanceArray =
                    StudentData.getSummary(sessionCookie, balancePeriodsValues.get(0));

            assertNotNull(balanceArray);
            assertFalse(balanceArray.isEmpty());

            ArrayList<SummaryType> summaryRows = balanceArray.get(0);
            ArrayList<String> totals = balanceArray.get(1);

            assertNotNull(summaryRows);
            assertNotNull(totals);

            assertFalse(summaryRows.isEmpty());
            assertFalse(totals.isEmpty());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test
    public void testStudentData_RetrieveBalanceEarliestPeriod() throws IOException, LoginTimeoutException {
        try {
            int lastItem = balancePeriodsValues.size() - 1;

            ArrayList<ArrayList> balanceArray =
                    StudentData.getSummary(sessionCookie, balancePeriodsValues.get(0));

            assertNotNull(balanceArray);
            assertFalse(balanceArray.isEmpty());

            ArrayList<SummaryType> summaryRows = balanceArray.get(0);
            ArrayList<String> totals = balanceArray.get(1);

            assertNotNull(summaryRows);
            assertNotNull(totals);

            assertFalse(summaryRows.isEmpty());
            assertFalse(totals.isEmpty());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    // Charges
    @Test
    public void testStudentData_RetrieveChargesLatestPeriod() throws IOException, LoginTimeoutException {
        try {
            List ccArray =
                    StudentData.getChargesCredits(sessionCookie, balancePeriodsValues.get(0), false);

            assertNotNull(ccArray);
            assertFalse(ccArray.isEmpty());

            List<ChargeCredit> ccList = (List<ChargeCredit>) ccArray.get(0);
            String total = (String) ccArray.get(1);

            assertNotNull(ccList);
            assertNotNull(total);

            assertFalse(ccList.isEmpty());
            assertFalse(total.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test
    public void testStudentData_RetrieveChargesMiddlePeriod() throws IOException, LoginTimeoutException {
        try {
            List ccArray =
                    StudentData.getChargesCredits(sessionCookie, balancePeriodsValues.get(1), false);

            assertNotNull(ccArray);
            assertFalse(ccArray.isEmpty());

            List<ChargeCredit> ccList = (List<ChargeCredit>) ccArray.get(0);
            String total = (String) ccArray.get(1);

            assertNotNull(ccList);
            assertNotNull(total);

            assertFalse(ccList.isEmpty());
            assertFalse(total.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test
    public void testStudentData_RetrieveChargesEarliestPeriod() throws IOException, LoginTimeoutException {
        try {
            int lastItem = balancePeriodsValues.size() - 1;

            List ccArray =
                    StudentData.getChargesCredits(sessionCookie, balancePeriodsValues.get(lastItem), false);

            assertNotNull(ccArray);
            assertFalse(ccArray.isEmpty());

            List<ChargeCredit> ccList = (List<ChargeCredit>) ccArray.get(0);
            String total = (String) ccArray.get(1);

            assertNotNull(ccList);
            assertNotNull(total);

            assertFalse(!ccList.isEmpty());
            assertFalse(total.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    // Credits
    @Test
    public void testStudentData_RetrieveCreditsLatestPeriod() throws IOException, LoginTimeoutException {
        try {
            List ccArray =
                    StudentData.getChargesCredits(sessionCookie, balancePeriodsValues.get(0), true);

            assertNotNull(ccArray);
            assertFalse(ccArray.isEmpty());

            List<ChargeCredit> ccList = (List<ChargeCredit>) ccArray.get(0);
            String total = (String) ccArray.get(1);

            assertNotNull(ccList);
            assertNotNull(total);

            assertFalse(!ccList.isEmpty());
            assertFalse(total.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test
    public void testStudentData_RetrieveCreditsMiddlePeriod() throws IOException, LoginTimeoutException {
        try {
            List ccArray =
                    StudentData.getChargesCredits(sessionCookie, balancePeriodsValues.get(1), true);

            assertNotNull(ccArray);
            assertFalse(ccArray.isEmpty());

            List<ChargeCredit> ccList = (List<ChargeCredit>) ccArray.get(0);
            String total = (String) ccArray.get(1);

            assertNotNull(ccList);
            assertNotNull(total);

            assertFalse(ccList.isEmpty());
            assertFalse(total.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Test
    public void testStudentData_RetrieveCreditsEarliestPeriod() throws IOException, LoginTimeoutException {
        try {
            int lastItem = balancePeriodsValues.size() - 1;

            List ccArray =
                    StudentData.getChargesCredits(sessionCookie, balancePeriodsValues.get(lastItem), true);

            assertNotNull(ccArray);
            assertFalse(ccArray.isEmpty());

            List<ChargeCredit> ccList = (List<ChargeCredit>) ccArray.get(0);
            String total = (String) ccArray.get(1);

            assertNotNull(ccList);
            assertNotNull(total);

            assertFalse(ccList.isEmpty());
            assertFalse(total.isEmpty());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}
