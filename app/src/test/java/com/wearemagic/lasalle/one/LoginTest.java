package com.wearemagic.lasalle.one;

import android.util.Log;

import com.wearemagic.lasalle.one.providers.Logins;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


public class LoginTest {
    private static final String TAG = "LaSalleOne";
    public String baseURL = "https://miportal.ulsaoaxaca.edu.mx/ss/";

    // Logins
    private String myID = "014403980";
    private String myPass = "xzJhcsL7J3";

    private String myFakePass = "xzJhcsL7J2";

    public static HashMap<String, String> generateLoginMap() {
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

    @Test
    public void testLogin_OwnLogin() {
        String loginCookie = "";

        try {
            loginCookie = Logins.doLogin(myID, myPass, baseURL);
        } catch (IOException io) {
            System.out.println("IOException on testLogin_OwnLogin");
            fail();
        }

        assertNotNull(loginCookie);
        assertFalse(loginCookie.isEmpty());
    }

    @Test
    public void testLogin_OwnMoodleLogin() {
        String loginCookie = "";

        try {
            loginCookie = Logins.doMoodleLogin(myID, myPass);
        } catch (IOException io) {
            System.out.println("IOException on testLogin_OwnMoodleLogin");
            fail();
        }

        assertNotNull(loginCookie);
        assertFalse(loginCookie.isEmpty());
    }

    @Test
    public void testLogin_OwnLoginInvalid() {
        String loginCookie = "";

        try {
            loginCookie = Logins.doLogin(myID, myFakePass, baseURL);
        } catch (IOException io) {
            System.out.println("IOException on testLogin_OwnLoginInvalid");
        }

        assertNull(loginCookie);
    }

    @Test
    public void testLogin_OwnMoodleLoginInvalid() {
        String loginCookie = "";

        try {
            loginCookie = Logins.doMoodleLogin(myID, myFakePass);
        } catch (IOException io) {
            Log.e(TAG, "IOException on testLogin_OwnMoodleLoginInvalid");
        }

        assertNull(loginCookie);
    }

    @Test
    public void testLogin_LoginMap() {
        HashMap<String, String> loginMap = generateLoginMap();

        for (HashMap.Entry<String, String> user : loginMap.entrySet()) {
            String userID = user.getKey();
            String userPass = user.getValue();

            String loginCookie = "";

            try {
                loginCookie = Logins.doLogin(userID, userPass, baseURL);
            } catch (IOException io) {
                System.out.println("IOException on testLogin_LoginMap");
            }

            try{
                assertNotNull(loginCookie);
                assertFalse(loginCookie.isEmpty());
            } catch(AssertionError e){
                System.out.println("User Failed is " + myID);
                throw e;
            }

            try {
                loginCookie = Logins.doLogin(userID, myFakePass, baseURL);
            } catch (IOException io) {
                System.out.println("IOException on testLogin_LoginMap");
            }

            try{
                assertNull(loginCookie);
            } catch(AssertionError e){
                System.out.println("User Failed is " + myID);
                throw e;
            }

        }
    }

    @Test
    public void testLogin_MoodleLoginMap() {
        HashMap<String, String> loginMap = generateLoginMap();

        for (HashMap.Entry<String, String> user : loginMap.entrySet()) {
            String userID = user.getKey();
            String userPass = user.getValue();

            String loginCookie = "";

            try {
                loginCookie = Logins.doMoodleLogin(userID, userPass);
            } catch (IOException io) {
                System.out.println("IOException on testLogin_LoginMap");
            }

            try{
                assertNotNull(loginCookie);
                assertFalse(loginCookie.isEmpty());
            } catch(AssertionError e){
                System.out.println("User Failed is " + myID);
                throw e;
            }

            try {
                loginCookie = Logins.doLogin(userID, myFakePass, baseURL);
            } catch (IOException io) {
                System.out.println("IOException on testLogin_LoginMap");
            }

            try{
                assertNull(loginCookie);
            } catch(AssertionError e){
                System.out.println("User Failed is " + myID);
                throw e;
            }

        }
    }
}
