package com.wearemagic.lasalle.one;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.wearemagic.lasalle.one.providers.Logins;

import java.io.File;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LaSalleOne";
    public String packageName = "com.wearemagic.lasalle.one";
    public String baseURL = "https://miportal.ulsaoaxaca.edu.mx/ss/";

    LoginAsyncTaskRunner loginTask = new LoginAsyncTaskRunner(LoginActivity.this);

    private LinearLayout loginLinearLayout;
    private ProgressBar loginLoading;

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

        loginLinearLayout = findViewById(R.id.loginLinearLayout);
        loginLoading = findViewById(R.id.loginLoading);
        loginRelativeLayout = findViewById(R.id.loginRelativeLayout);
        idEditText = findViewById(R.id.idField);
        passEditText = findViewById(R.id.passwordField);
        logInButton = findViewById(R.id.signInButton);

        if (accountStored){
            executeLogin(accountUser, accountPass, moodleLogin);
        }

        // EditText Length Listener (Ugh)
        idEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {}
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (idEditText.length() == 9){
                    logInButton.setEnabled(true);
                } else {
                    logInButton.setEnabled(false);
                }
            }
            public void afterTextChanged(Editable c) {}
        });

        // Button Click Listener
        logInButton.setOnClickListener((View v) -> {
            String idSalle = idEditText.getText().toString();
            String passwordSalle = passEditText.getText().toString();

            executeLogin(idSalle, passwordSalle, moodleLogin);
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

    public void executeLogin(String user, String pass, Boolean moodle){
        Bundle loginBundle = new Bundle();

        loginBundle.putString("idSalle", user);
        loginBundle.putString("passwordSalle", pass);

        loginBundle.putBoolean("moodleLogin", moodle);

        new LoginAsyncTaskRunner(LoginActivity.this).execute(loginBundle);
    }

    private void sendErrorCode(String errorCode) {
        switch (errorCode) {
            case "ERROR_NO_INTERNET_CONNECTION":
                Snackbar noInternetSB = Snackbar
                    .make(loginRelativeLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry_internet_connection), (View view) -> {

                        String user, password;
                        if (accountStored){
                            user = accountUser;
                            password = accountPass;
                        } else {
                            user = idEditText.getText().toString();
                            password = passEditText.getText().toString();
                        }

                        executeLogin(user, password, moodleLogin);
                    });

                noInternetSB.show();
                break;
            case "ERROR_INCORRECT_LOGIN":
                Toast.makeText(getApplicationContext(), getString(R.string.error_incorrect_login), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    protected boolean saveLogin(String id, String pass){
        boolean idSaveOp = false;
        boolean passSaveOp = false;

        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

        idSaveOp = sharedP.edit().putString("accountUser", id).commit();
        passSaveOp = sharedP.edit().putString("accountPass", pass).commit();

        return idSaveOp && passSaveOp;
    }

    private class LoginAsyncTaskRunner extends AsyncTask<Bundle, String, String[]> {

        private Activity activity;

        private LoginAsyncTaskRunner(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            loginLinearLayout.setVisibility(View.GONE);
            loginLoading.setVisibility(View.VISIBLE);
            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.hide();
            }
        }

        @Override
        protected String[] doInBackground(Bundle... params) {
            Bundle loginBundle = params[0];
            Boolean moodleLogin;

            String idS = loginBundle.getString("idSalle");
            String passwordS = loginBundle.getString("passwordSalle");
            moodleLogin = loginBundle.getBoolean("moodleLogin");

            String sessionCookie = "";
            String moodleCookie = "";

            try { sessionCookie = Logins.doLogin(idS, passwordS, baseURL); }
            catch (IOException e){ Log.e(TAG, "IOException on LoginAsyncTask (Portal Login)"); }

            if (moodleLogin) {
                try { moodleCookie = Logins.doMoodleLogin(idS, passwordS); }
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
            ActionBar actionBar = getSupportActionBar();

            if (sessionC != null && !sessionC.isEmpty()) {
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
                i.putExtra("accountStored", accountStored);
                startActivity(i);
                finish();
            }

            else {
                loginLinearLayout.setVisibility(View.VISIBLE);
                loginLoading.setVisibility(View.GONE);

                if (actionBar!= null) {
                    actionBar.show();
                }

                if (sessionC == null){
                    sendErrorCode("ERROR_INCORRECT_LOGIN");
                }

                else if(sessionC.isEmpty()){
                    sendErrorCode("ERROR_NO_INTERNET_CONNECTION");

                    if(accountStored){
                        idEditText.setText(accountUser);
                        passEditText.setText(accountPass);
                        logInButton.setEnabled(false);
                    }
                }
            }
        }
    }
}