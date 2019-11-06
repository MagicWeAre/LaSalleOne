package com.wearemagic.lasalle.one;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LaSalleOne";

    public String packageName = "com.wearemagic.lasalle.one";
    LoginAsyncTaskRunner loginTask = new LoginAsyncTaskRunner(LoginActivity.this);

    private RelativeLayout loginRelativeLayout;
    private Button logInButton;
    private EditText idEditText;
    private EditText passEditText;

    private String accountUser;
    private String accountPass;
    private boolean accountStored;
    private boolean moodleLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){

            accountUser = savedInstanceState.getString("accountUser");
            accountPass = savedInstanceState.getString("accountPass");
            accountStored = savedInstanceState.getBoolean("accountStored");
            moodleLogin = savedInstanceState.getBoolean("moodleLogin");

        } else {

            SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

            accountUser = sharedP.getString("accountUser", "");
            accountPass = sharedP.getString("accountPass", "");

            accountStored = !accountUser.isEmpty() && !accountPass.isEmpty();

            File profilePic = new File(getFilesDir(), accountUser + ".jpg");
            moodleLogin = !profilePic.exists();
        }

        setContentView(R.layout.activity_login);

        if (accountStored){
            Bundle loginBundle = new Bundle();

            loginBundle.putString("idSalle", accountUser);
            loginBundle.putString("passwordSalle", accountPass);
            loginBundle.putBoolean("moodleLogin", moodleLogin);

            new LoginAsyncTaskRunner(LoginActivity.this).execute(loginBundle);
        }

        loginRelativeLayout = findViewById(R.id.loginRelativeLayout);
        idEditText = findViewById(R.id.idField);
        passEditText = findViewById(R.id.passwordField);
        logInButton = findViewById(R.id.signInButton);

        // EditText Length Listener (Ugh)
        idEditText.addTextChangedListener(new TextWatcher() {

            byte[] data = Base64.encode("o".getBytes(), 2);

            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (idEditText.length() == 9){
                    logInButton.setEnabled(true);
                } else { logInButton.setEnabled(false); }
            }
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {}
            public void afterTextChanged(Editable c) {}
        });

        // Button Click Listener
        logInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle loginBundle = new Bundle();

                String idSalle = idEditText.getText().toString();
                String passwordSalle = passEditText.getText().toString();

                loginBundle.putString("idSalle", idSalle);
                loginBundle.putString("passwordSalle", passwordSalle);
                loginBundle.putBoolean("moodleLogin", moodleLogin);

                new LoginAsyncTaskRunner(LoginActivity.this).execute(loginBundle);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("accountUser", accountUser);
        outState.putString("accountPass", accountPass);
        outState.putBoolean("accountStored", accountStored);
        outState.putBoolean("moodleLogin", moodleLogin);
        super.onSaveInstanceState(outState);
    }

    public void prepareLogin(){
        if (accountStored){
            Bundle loginBundle = new Bundle();

            loginBundle.putString("idSalle", accountUser);
            loginBundle.putString("passwordSalle", accountPass);
            loginBundle.putBoolean("moodleLogin", moodleLogin);

            new LoginAsyncTaskRunner(LoginActivity.this).execute(loginBundle);

        } else {
            Bundle loginBundle = new Bundle();

            String idSalle = idEditText.getText().toString();
            String passwordSalle = passEditText.getText().toString();

            loginBundle.putString("idSalle", idSalle);
            loginBundle.putString("passwordSalle", passwordSalle);
            loginBundle.putBoolean("moodleLogin", moodleLogin);

            new LoginAsyncTaskRunner(LoginActivity.this).execute(loginBundle);
        }
    }

    private class LoginAsyncTaskRunner extends AsyncTask<Bundle, String, String[]> {

        private Exception e;
        private Activity activity;

        private LoginAsyncTaskRunner(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String[] doInBackground(Bundle... params) {
            Bundle loginBundle = params[0];
            Boolean moodleLogin = false;

            String idS = loginBundle.getString("idSalle");
            String passwordS = loginBundle.getString("passwordSalle");
            moodleLogin = loginBundle.getBoolean("moodleLogin");

            String sessionCookie = "";
            String moodleCookie = "";

            try { sessionCookie = doLogin(idS, passwordS); }
            catch (IOException e){ Log.e(TAG, "IOException on LoginAsyncTask (Portal Login)"); }

            if (moodleLogin) {
                try { moodleCookie = doMoodleLogin(idS, passwordS); }
                catch (IOException e){ Log.e(TAG, "IOException on LoginAsyncTask (Moodle Login)"); }
            }

            String[] returnArray = {idS, passwordS, sessionCookie, moodleCookie};
            return returnArray;
        }


        @Override
        protected void onPostExecute(String[] returnArray) {

            String idS = returnArray[0];
            String passwordS= returnArray[1];
            String sessionC = returnArray[2];

            Switch passSwitch = findViewById(R.id.passwordSwitch);
            LinearLayout linearLayout = findViewById(R.id.loginLinearLayout);
            ProgressBar progressBar = findViewById(R.id.loginLoading);
            ActionBar actionBar = getSupportActionBar();

            if (sessionC == null){
                linearLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                actionBar.show();

                Toast.makeText(getApplicationContext(), getString(R.string.error_incorrect_login), Toast.LENGTH_SHORT).show();
            }

            else if(sessionC.isEmpty()){
                linearLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if(accountStored){
                    idEditText.setText(accountUser);
                    passEditText.setText(accountPass);
                    logInButton.setEnabled(false);
                }
                actionBar.show();

                Snackbar noInternetSB = Snackbar
                        .make(loginRelativeLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                prepareLogin();
                            }
                        });

                noInternetSB.show();

                //Toast.makeText(getApplicationContext(), getString(R.string.error_internet_failure), Toast.LENGTH_SHORT).show();
            }

            else {
                Log.d(TAG, sessionC);

                if (passSwitch.isChecked()){
                    saveLogin(idS, passwordS);}

                if (returnArray[3] == null){
                    returnArray[3] = "";
                }

                Intent i = new Intent(activity, MainActivity.class);
                i.putExtra("sessionCookie", sessionC);
                i.putExtra("moodleCookie", returnArray[3]);
                i.putExtra("salleId", idS);
                startActivity(i);
                finish();
            }
        }


        @Override
        protected void onPreExecute() {

            LinearLayout linearLayout = findViewById(R.id.loginLinearLayout);
            ProgressBar progressBar = findViewById(R.id.loginLoading);

            linearLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

        }


        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    public String doLogin(String id, String pass) throws IOException {

        Connection.Response loginPageGet = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Home.aspx")
                .method(Connection.Method.GET)
                .execute();

        Document loginDocument = loginPageGet.parse();

        Element eventValidation = loginDocument.select("input[name=__EVENTVALIDATION]").first();
        Element viewState = loginDocument.select("input[name=__VIEWSTATE]").first();

        Connection.Response loginPagePost = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Home.aspx")
                .method(Connection.Method.POST)
                .data("ctl00$ucUserLogin$lvLoginUser$ucLoginUser$lcLoginUser$UserName", id)
                .data("ctl00$ucUserLogin$lvLoginUser$ucLoginUser$lcLoginUser$Password", pass)
                .data("ctl00$ucUserLogin$lvLoginUser$ucLoginUser$lcLoginUser$LoginButton", "Log+In")
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .execute();

        String serviceCookie = loginPagePost.cookie("SelfService");

        return serviceCookie;
    }

    public String doMoodleLogin(String id, String pass) throws IOException {

        Connection.Response firstResponse = Jsoup.connect("http://micurso.ulsaoaxaca.edu.mx/login/index.php")
                .method(Connection.Method.GET)
                .execute();

        String firstMoodleCookie = firstResponse.cookie("MoodleSession");

        Map<String, String> firstCookieMap = new HashMap<String, String>();
        firstCookieMap.put("MoodleSession", firstMoodleCookie);

        Document firstDocument = firstResponse.parse();

        Element logintoken = firstDocument.select("input[name=logintoken]").first();

        Connection.Response loginPost = Jsoup.connect("http://micurso.ulsaoaxaca.edu.mx/login/index.php")
                .method(Connection.Method.POST)
                .cookies(firstCookieMap)
                .data("username", id)
                .data("password", pass)
                .data("logintoken", logintoken.attr("value"))
                .execute();

        String secondMoodleCookie = loginPost.cookie("MoodleSession");

        return secondMoodleCookie;
    }


    protected boolean saveLogin(String id, String pass){
        boolean idSaveOp = false;
        boolean passSaveOp = false;

        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

        idSaveOp = sharedP.edit().putString("accountUser", id).commit();
        passSaveOp = sharedP.edit().putString("accountPass", pass).commit();

        return idSaveOp && passSaveOp;
    }
}