package com.wearemagic.lasalle.one;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.fragments.BalanceFragment;
import com.wearemagic.lasalle.one.fragments.ChargesFragment;
import com.wearemagic.lasalle.one.fragments.CreditsFragment;
import com.wearemagic.lasalle.one.fragments.GradesFragment;
import com.wearemagic.lasalle.one.fragments.HomeFragment;
import com.wearemagic.lasalle.one.fragments.ProfileFragment;
import com.wearemagic.lasalle.one.fragments.SubjectsFragment;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;
import com.wearemagic.lasalle.one.providers.Periods;
import com.wearemagic.lasalle.one.providers.WeAreMagic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        SubjectsFragment.SubjectsListener, GradesFragment.GradesListener, ProfileFragment.ProfileListener,
        BalanceFragment.BalanceListener, CreditsFragment.CreditsListener, ChargesFragment.ChargesListener {

    private static final String TAG = "LaSalleOne";
    public String packageName = "com.wearemagic.lasalle.one";

    String projectCode = "la_salle-one";
    int versionCode = BuildConfig.VERSION_CODE;
    String versionName = BuildConfig.VERSION_NAME;
    String variant = "PUBLIC_RELEASE_ONE";

    private PeriodsAsyncTaskRunner periodsTask = new PeriodsAsyncTaskRunner();
    boolean firstTime = false;
    private LogOpenAsyncTaskRunner logsTask = new LogOpenAsyncTaskRunner();

    private ArrayList<String> subjectsPeriodNames = new ArrayList<>();
    private ArrayList<String> subjectsPeriodValues = new ArrayList<>();
    private ArrayList<String> gradesPeriodNames = new ArrayList<>();
    private ArrayList<String> gradesPeriodValues = new ArrayList<>();
    private ArrayList<String> balancePeriodNames = new ArrayList<>();
    private ArrayList<String> balancePeriodValues = new ArrayList<>();

    private HomeFragment homeFragment;
    private GradesFragment gradesFragment;
    private SubjectsFragment subjectsFragment;
    private BalanceFragment balanceFragment;
    private ProfileFragment profileFragment;
    private ChargesFragment chargesFragment;
    private CreditsFragment creditsFragment;

    private BottomNavigationView bottomNavigation;
    private NavigationView navigationView;
    private Toolbar drawerToolbar;
    private TextView drawerID;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPagerAdapter vpAdapter;
    private FrameLayout frameLayout;
    private TabLayout mainTabLayout;
    private ViewPager mainViewPager;
    private Spinner subjectsSpinner;
    private Spinner gradesSpinner;
    private Spinner balanceSpinner;

    private String sessionCookie;
    private String moodleCookie;
    private String salleId;
    private String balanceTag;
    private String creditsTag;
    private String chargesTag;
    private LogNewAsyncTaskRunner logsNewTask = new LogNewAsyncTaskRunner();
    private String logEmail;
    private String logName;

    private int nightMode;
    private boolean redoingLogin;
    private String logSurname;
    private boolean retrievedPeriods = false;
    private boolean loggedOnServer = false;
    private boolean loggedNewServer = false;
    private boolean advDataCollection = false;
    private boolean accountStored = false;
    private boolean advDataReceived = false;

    // 0: none
    // 1: subjectsFragment
    // 2: gradesFragment
    // 3: balanceFragment
    private int activeFragment;

    private int subjectsSpinnerPosition;
    private int gradesSpinnerPosition;
    private int balanceSpinnerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve SharedPreferences NightMode
        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);
        nightMode = sharedP.getInt("nightMode", -1);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        // Retrieve Intent Bundle data for Login details
        Bundle loginBundle = getIntent().getExtras();

        if (loginBundle == null){
           onRedoLogin();
        }

        else {
            // As Intent data is not null, retrieve it
            // Two cookies, to access sites, the user's ID to display it on the sidebar, amongst others
            sessionCookie = loginBundle.getString("sessionCookie");
            moodleCookie = loginBundle.getString("moodleCookie");
            salleId = loginBundle.getString("salleId");
            accountStored = loginBundle.getBoolean("accountStored");

            // Check Saved State (in case activity is being resumed)
            if (savedInstanceState != null) {
                if (sessionCookie.isEmpty()) {
                    sessionCookie = savedInstanceState.getString("sessionCookie");
                } if (moodleCookie.isEmpty()) {
                    moodleCookie = savedInstanceState.getString("moodleCookie");
                } if (salleId.isEmpty()) {
                    salleId = savedInstanceState.getString("salleId");
                    accountStored = savedInstanceState.getBoolean("accountStored");
                }

                // Recover saved variables
                // Active Fragment
                activeFragment = savedInstanceState.getInt("activeFragment");

                // Period Information
                subjectsPeriodNames = savedInstanceState.getStringArrayList("subjectsPeriodNames");
                subjectsPeriodValues = savedInstanceState.getStringArrayList("subjectsPeriodValues");
                gradesPeriodNames = savedInstanceState.getStringArrayList("gradesPeriodNames");
                gradesPeriodValues = savedInstanceState.getStringArrayList("gradesPeriodValues");
                balancePeriodNames = savedInstanceState.getStringArrayList("balancePeriodNames");
                balancePeriodValues = savedInstanceState.getStringArrayList("balancePeriodValues");

                // Spinner Positions
                subjectsSpinnerPosition = savedInstanceState.getInt("subjectsSpinnerPosition");
                gradesSpinnerPosition = savedInstanceState.getInt("gradesSpinnerPosition");
                balanceSpinnerPosition = savedInstanceState.getInt("balanceSpinnerPosition");

                // Fragment Tags
                balanceTag = savedInstanceState.getString("balanceTag");
                creditsTag = savedInstanceState.getString("creditsTag");
                chargesTag = savedInstanceState.getString("chargesTag");

                // Redoing Login Boolean
                redoingLogin = savedInstanceState.getBoolean("redoingLogin");

                retrievedPeriods = savedInstanceState.getBoolean("retrievedPeriods");
                loggedOnServer = savedInstanceState.getBoolean("loggedOnServer");
                loggedNewServer = savedInstanceState.getBoolean("loggedNewServer");
                firstTime = savedInstanceState.getBoolean("firstTime");
                advDataCollection = savedInstanceState.getBoolean("advDataCollection");

                logEmail = savedInstanceState.getString("logEmail");
                logName = savedInstanceState.getString("logName");
                logSurname = savedInstanceState.getString("logSurname");
                advDataReceived = savedInstanceState.getBoolean("advDataReceived");

            } else {
                firstTime = sharedP.getBoolean("firstTime", true);
                String lastLogin = sharedP.getString("lastLogin", "");

                if (lastLogin.equals(salleId)) {
                    firstTime = false;
                }

                sharedP.edit().putString("lastLogin", salleId).apply();

            }

            if (!retrievedPeriods) {
                periodsTask = new PeriodsAsyncTaskRunner();
                periodsTask.execute(sessionCookie);
            }

            if (!firstTime && !loggedOnServer) {
                logsTask = new LogOpenAsyncTaskRunner();
                logsTask.execute();
            }


            setContentView(R.layout.activity_main);

            subjectsSpinner = findViewById(R.id.subjectsBarSpinner);
            gradesSpinner = findViewById(R.id.gradesBarSpinner);
            balanceSpinner = findViewById(R.id.balanceBarSpinner);

            subjectsSpinner.setOnItemSelectedListener(this);
            gradesSpinner.setOnItemSelectedListener(this);
            balanceSpinner.setOnItemSelectedListener(this);

            drawerToolbar = findViewById(R.id.navigationToolbarIncluded);
            drawerLayout = findViewById(R.id.mainDrawerLayout);
            drawerID = findViewById(R.id.drawerID);

            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);

            setSupportActionBar(drawerToolbar);

            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.setDrawerSlideAnimationEnabled(false);

            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                // Use logo only if API does not implement rounded icons
                if (android.os.Build.VERSION.SDK_INT < 26){
                    getSupportActionBar().setDisplayUseLogoEnabled(true);
                    getSupportActionBar().setLogo(R.mipmap.ic_launcher);
                }
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle arguments = new Bundle();

            arguments.putString("sessionCookie" , sessionCookie);
            arguments.putString("moodleCookie" , moodleCookie);
            arguments.putString("salleId", salleId);

            mainViewPager = findViewById(R.id.mainPager);

            if(savedInstanceState == null){
                homeFragment = new HomeFragment();
                gradesFragment = new GradesFragment();
                subjectsFragment = new SubjectsFragment();
                profileFragment = new ProfileFragment();

                homeFragment.setArguments(arguments);
                gradesFragment.setArguments(arguments);
                subjectsFragment.setArguments(arguments);
                profileFragment.setArguments(arguments);

                fragmentTransaction.add(R.id.mainFrameLayout, homeFragment, "hF");
                fragmentTransaction.add(R.id.mainFrameLayout, subjectsFragment, "sF");
                fragmentTransaction.add(R.id.mainFrameLayout, gradesFragment, "gF");
                fragmentTransaction.add(R.id.mainFrameLayout, profileFragment, "pF");

                fragmentTransaction
                        .hide(gradesFragment)
                        .hide(subjectsFragment)
                        .hide(profileFragment)
                        .commit();
                setFragment(homeFragment);


                //TabLayout
                chargesFragment = new ChargesFragment();
                creditsFragment = new CreditsFragment();
                balanceFragment = new BalanceFragment();

                chargesFragment.setArguments(arguments);
                creditsFragment.setArguments(arguments);
                balanceFragment.setArguments(arguments);
            }

            else {
                homeFragment = (HomeFragment) fragmentManager.findFragmentByTag("hF");
                subjectsFragment = (SubjectsFragment) fragmentManager.findFragmentByTag("sF");
                gradesFragment = (GradesFragment) fragmentManager.findFragmentByTag("gF");
                profileFragment = (ProfileFragment) fragmentManager.findFragmentByTag("pF");

                if (chargesTag != null) {
                    chargesFragment = (ChargesFragment) fragmentManager.findFragmentByTag(chargesTag);
                } else {
                    chargesFragment = new ChargesFragment();
                    chargesFragment.setArguments(arguments);
                }

                if (creditsTag != null) {
                    creditsFragment = (CreditsFragment) fragmentManager.findFragmentByTag(creditsTag);
                } else {
                    creditsFragment = new CreditsFragment();
                    creditsFragment.setArguments(arguments);
                }

                if (balanceTag != null){
                    balanceFragment = (BalanceFragment) fragmentManager.findFragmentByTag(balanceTag);

                } else {
                    balanceFragment = new BalanceFragment();
                    balanceFragment.setArguments(arguments);
                }
            }

            vpAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            vpAdapter.addFragment(balanceFragment, getString(R.string.summary_tab));
            vpAdapter.addFragment(creditsFragment, getString(R.string.credits_tab));
            vpAdapter.addFragment(chargesFragment, getString(R.string.charges_tab));
            mainViewPager.setAdapter(vpAdapter);

            if (savedInstanceState != null){
                setSpinners();
                sendPeriods();
            }

            frameLayout = findViewById(R.id.mainFrameLayout);
            mainTabLayout = findViewById(R.id.mainTabs);
            mainTabLayout.setupWithViewPager(mainViewPager);

            restoreViewElements(activeFragment);

            bottomNavigation = findViewById(R.id.mainBottomNavigation);
            navigationView = findViewById(R.id.mainDrawerNavigation);
            navigationView.setCheckedItem(R.id.mainDrawerHome);

            View headerLayout = navigationView.inflateHeaderView(R.layout.navigation_header);
            drawerID = headerLayout.findViewById(R.id.drawerID);

            if (drawerID != null) {
                drawerID.setText(salleId);
            }

            navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()){
                    case R.id.mainDrawerEnd: {
                        SharedPreferences.Editor editor = sharedP.edit();
                        // Login Data
                        editor.remove("accountUser");
                        editor.remove("accountPass");

                        // Data Collection
                        editor.remove("firstTime");
                        //editor.putString("lastLogin", salleId).apply();

                        editor.apply();

                        startActivity(new Intent( getApplicationContext(), LoginActivity.class));
                        finish();
                        break;
                    } case R.id.mainDrawerSettings: {
                        startActivity(new Intent( getApplicationContext(), SettingsActivity.class));
                        break;
                    } case R.id.mainDrawerHelp: {
                        startActivity(new Intent( getApplicationContext(), HelpActivity.class));
                        break;
                    } case R.id.mainDrawerDocuments: {
                        openWebSite("http://alumnos.ulsaoaxaca.edu.mx/");
                        break;
                    } case R.id.mainDrawerFIBU: {
                        openWebSite("http://fibu.ulsaoaxaca.edu.mx/");
                        break;
                    }
                }

                return true;
            });

            bottomNavigation.setOnNavigationItemSelectedListener((@NonNull MenuItem menuItem) -> {

                activeFragment = 0;

                switch (menuItem.getItemId()){
                    case R.id.mainMenuHome: {
                        setFragment(homeFragment);
                        break;
                    } case R.id.mainMenuSubjects: {
                        setFragment(subjectsFragment);
                        activeFragment = 1;
                        break;
                    } case R.id.mainMenuGrades: {
                        setFragment(gradesFragment);
                        activeFragment = 2;
                        break;
                    } case R.id.mainMenuBalance: {
                        setFragment(null);
                        activeFragment = 3;
                        break;
                    } case R.id.mainMenuProfile: {
                        setFragment(profileFragment);
                        break;
                    }
                    default:
                        return false;
                }

                restoreViewElements(activeFragment);
                return true;
            });
        }
    }

    protected void onResume(){

        checkFirstTime();

        navigationView = findViewById(R.id.mainDrawerNavigation);
        navigationView.setCheckedItem(R.id.mainDrawerHome);

        AppCompatDelegate.setDefaultNightMode(nightMode);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // Handle destruction of AsyncTask to avoid errors
        if (periodsTask != null){
            periodsTask.cancel(true);
            periodsTask = null;}

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("sessionCookie", sessionCookie);
        outState.putString("moodleCookie", moodleCookie);
        outState.putString("salleId", salleId);
        outState.putBoolean("accountStored", accountStored);

        // Active Fragments
        outState.putInt("activeFragment", activeFragment);

        // Period Information
        outState.putStringArrayList("subjectsPeriodNames", subjectsPeriodNames);
        outState.putStringArrayList("subjectsPeriodValues", subjectsPeriodValues);
        outState.putStringArrayList("gradesPeriodNames", gradesPeriodNames);
        outState.putStringArrayList("gradesPeriodValues", gradesPeriodValues);
        outState.putStringArrayList("balancePeriodNames", balancePeriodNames);
        outState.putStringArrayList("balancePeriodValues", balancePeriodValues);

        // Spinner Positions
        outState.putInt("subjectsSpinnerPosition", subjectsSpinnerPosition);
        outState.putInt("gradesSpinnerPosition", gradesSpinnerPosition);
        outState.putInt("balanceSpinnerPosition", balanceSpinnerPosition);

        // Fragment Tags
        outState.putString("balanceTag", vpAdapter.getFragmentTag(0));
        outState.putString("creditsTag", vpAdapter.getFragmentTag(1));
        outState.putString("chargesTag", vpAdapter.getFragmentTag(2));

        outState.putBoolean("redoingLogin", redoingLogin);
        outState.putBoolean("retrievedPeriods", retrievedPeriods);
        outState.putBoolean("loggedOnServer", loggedOnServer);
        outState.putBoolean("loggedNewServer", loggedNewServer);
        outState.putBoolean("firstTime", firstTime);
        outState.putBoolean("advDataCollection", advDataCollection);

        outState.putString("logEmail", logEmail);
        outState.putString("logName", logName);
        outState.putString("logSurname", logSurname);
        outState.getBoolean("advDataReceived", advDataReceived);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Spinner onItemSelected
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()){
            case R.id.subjectsBarSpinner:
                if(subjectsSpinnerPosition != pos) {
                    subjectsSpinnerPosition = pos;
                    subjectsFragment.shareSpinnerPosition(pos);
                    subjectsFragment.onRefresh();
                }

                break;

            case R.id.gradesBarSpinner:
                if(gradesSpinnerPosition != pos) {
                    gradesSpinnerPosition = pos;
                    gradesFragment.shareSpinnerPosition(pos);
                    gradesFragment.onRefresh();
                }

                break;

            case R.id.balanceBarSpinner:
                if(balanceSpinnerPosition != pos) {
                    balanceSpinnerPosition = pos;

                    chargesFragment.shareSpinnerPosition(pos);
                    creditsFragment.shareSpinnerPosition(pos);
                    balanceFragment.shareSpinnerPosition(pos);

                    chargesFragment.onRefresh();
                    creditsFragment.onRefresh();
                    balanceFragment.onRefresh();
                }

                break;
        }
    }

    // Spinner onNothingSelected
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void setFragment(Fragment mainFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(this.homeFragment)
                .hide(this.gradesFragment)
                .hide(this.subjectsFragment)
                .hide(this.profileFragment).commit();

        if (mainFragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(mainFragment)
                    .commit();
        }
    }

    private void restoreViewElements (int newActiveFragment) {
        // 0: none
        // 1: subjectsFragment
        // 2: gradesFragment
        // 3: balanceFragment

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mainViewPager.setVisibility(View.GONE);
        mainTabLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

        subjectsSpinner.setVisibility(View.GONE);
        gradesSpinner.setVisibility(View.GONE);
        balanceSpinner.setVisibility(View.GONE);

        switch (newActiveFragment) {
            case 1:
                subjectsSpinner.setVisibility(View.VISIBLE);
                break;
            case 2:
                gradesSpinner.setVisibility(View.VISIBLE);
                break;
            case 3:
                mainViewPager.setVisibility(View.VISIBLE);
                mainViewPager.setCurrentItem(0);

                mainTabLayout.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);

                balanceSpinner.setVisibility(View.VISIBLE);
                break;
            default:
                getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void openWebSite(String URL) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(URL));
        startActivity(i);
    }

    public void checkFirstTime() {
        if (firstTime) {
            AlertDialog.Builder newcomerBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
            View privacyDialog = inflater.inflate(R.layout.privacy_dialog, null);

            newcomerBuilder.setView(privacyDialog).setMessage(getString(R.string.eula_message))
                    .setTitle(getString(R.string.eula_title))
                    .setPositiveButton(getString(R.string.eula_accept), (DialogInterface dialog, int which) -> {

                            CheckBox advDataCheckbox = privacyDialog.findViewById(R.id.advDataCollection);
                            advDataCollection = advDataCheckbox.isChecked();

                            SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

                            if (accountStored) {
                                sharedP.edit().putBoolean("firstTime", false).apply();
                            }

                            firstTime = false;

                            if (!loggedOnServer) {
                                logsTask = new LogOpenAsyncTaskRunner();
                                logsTask.execute();
                            }

                            if (advDataCollection && advDataReceived && !loggedNewServer) {
                                // Launch new async task to send new user data
                                logsNewTask = new LogNewAsyncTaskRunner();
                                logsNewTask.execute();
                            }
                        })
                    .setNegativeButton(getString(R.string.eula_read), (DialogInterface dialog, int which) -> {
                    String url = getString(R.string.eula_link);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                });

            AlertDialog newcomerDialog = newcomerBuilder.create();
            newcomerBuilder.show();
        }
    }

    // Fragment Listener Info Sent
    @Override
    public void onSexSent(boolean sex){
        homeFragment.setSex(sex);
    }

    @Override
    public void onUserDataSent(String name, String surname, String email) {
        advDataReceived = true;
        logEmail = email;
        logName = name;
        logSurname = surname;

        if (!loggedNewServer && advDataCollection) {
            // Launch new async task to send new user data
            logsNewTask = new LogNewAsyncTaskRunner();
            logsNewTask.execute();
        }
    }

    @Override
    public void onSubjectInfoSent(ArrayList infoList) {
        homeFragment.onSubjectsNumberSent((String) infoList.get(0));
        homeFragment.onCurrentSubjectsSent((ArrayList<ScheduleSubject>) infoList.get(1));
        homeFragment.onNextSubjectsSent((ArrayList<ScheduleSubject>) infoList.get(2));
    }

    @Override
    public void onGradesInfoSent(ArrayList infoList) {
        homeFragment.onGradesHighestSent((String) infoList.get(0));
        homeFragment.onGradesLowestSent((String) infoList.get(1));
        homeFragment.onGradesAvgSent((String) infoList.get(2));
    }

    // Initiate Login Activity and terminate this one
    public void onRedoLogin(){
        if(!redoingLogin){
            redoingLogin = true;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void updatePeriods(ArrayList<ArrayList<ArrayList<String>>> periodsArrayList) {
        ArrayList<ArrayList<String>> subjectsPeriodList = periodsArrayList.get(0);
        ArrayList<ArrayList<String>> gradesPeriodList = periodsArrayList.get(1);
        ArrayList<ArrayList<String>> balancePeriodList = periodsArrayList.get(2);

        subjectsPeriodNames = subjectsPeriodList.get(0);
        subjectsPeriodValues = subjectsPeriodList.get(1);
        gradesPeriodNames = gradesPeriodList.get(0);
        gradesPeriodValues = gradesPeriodList.get(1);
        balancePeriodNames = balancePeriodList.get(0);
        balancePeriodValues = balancePeriodList.get(1);
    }

    public void sendPeriods() {
        gradesFragment.sharePeriodValues(gradesPeriodValues);
        gradesFragment.onRefresh();

        subjectsFragment.sharePeriodValues(subjectsPeriodValues);
        subjectsFragment.onRefresh();

        balanceFragment.sharePeriodValues(balancePeriodValues);
        chargesFragment.sharePeriodValues(balancePeriodValues);
        creditsFragment.sharePeriodValues(balancePeriodValues);
        balanceFragment.onRefresh();
        chargesFragment.onRefresh();
        creditsFragment.onRefresh();

        setSpinners();
    }

    // Default configuration for all spinners
    private ArrayAdapter configSpinner(ArrayList<String> periodNames) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, periodNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return spinnerAdapter;
    }

    // Assign each spinner an adapter which we configurate with another function
    public void setSpinners() {
        subjectsSpinner.setAdapter(configSpinner(subjectsPeriodNames));
        gradesSpinner.setAdapter(configSpinner(gradesPeriodNames));
        balanceSpinner.setAdapter(configSpinner(balancePeriodNames));
    }

    private void sendErrorCode(String errorCode) {
        switch (errorCode) {
            case "ERROR_NO_INTERNET_CONNECTION":
                Snackbar noInternetSB = Snackbar
                        .make(frameLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry_internet_connection), (View view) -> {
                            periodsTask = new PeriodsAsyncTaskRunner();
                            periodsTask.execute(sessionCookie);
                        });

                noInternetSB.show();
                break;
        }
    }

    // Fairly simple AsyncTask, retrieves period information
    private class PeriodsAsyncTaskRunner extends AsyncTask<String, String, ArrayList<ArrayList<ArrayList<String>>>> {

        @Override
        protected ArrayList<ArrayList<ArrayList<String>>> doInBackground(String... params) {
            String sessionCookie = params[0];

            ArrayList<ArrayList<ArrayList<String>>> periodsList = new ArrayList<>();

            try {
                periodsList = Periods.getPeriods(sessionCookie);
            } catch (IOException e) {
                // Connectivity issues
                Log.e(TAG, "IOException on PeriodAsyncTask");
            } catch (NullPointerException np) {
                // Not previously observed error, likely caused
                // by referencing a non existent object in the
                // retrieved document
                Log.e(TAG, np.getMessage());
            } catch (LoginTimeoutException lt) {
                // Custom made exception triggered when the site
                // redirects us to the Login page
                Log.d(TAG, "LoginTimeoutException on PeriodAsyncTask");
                onRedoLogin();
            }

            return periodsList;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<ArrayList<String>>> periodsList) {

            if(periodsList.isEmpty()){
                sendErrorCode("ERROR_NO_INTERNET_CONNECTION");
            } else {
                updatePeriods(periodsList);
                sendPeriods();
            }

            retrievedPeriods = true;
        }
    }

    // Fairly simple AsyncTask, sends information to own server every time App is opened
    private class LogOpenAsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String requestCode = "ERROR_NO_OPERATION";

            try {
                requestCode = WeAreMagic.sendOpenEvent(projectCode, String.valueOf(versionCode), variant, salleId);
            } catch (IOException e) {
                // Connectivity issues
                Log.e(TAG, "IOException on LogsAsyncTask");
                e.printStackTrace();
            }

            return requestCode;
        }

        @Override
        protected void onPostExecute(String operationCode) {
            loggedOnServer = true;
        }
    }

    // Fairly simple AsyncTask, sends information to own server every time App is opened
    private class LogNewAsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String requestCode = "ERROR_NO_OPERATION";

            try {
                requestCode = WeAreMagic.sendNewLoginEvent(projectCode, String.valueOf(versionCode),
                        variant, salleId, logEmail, logName, logSurname);
            } catch (IOException e) {
                // Connectivity issues
                Log.e(TAG, "IOException on LogNewAsyncTask");
            }

            return requestCode;
        }

        @Override
        protected void onPostExecute(String operationCode) {
            loggedNewServer = true;
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private String[] mTagArray = new String[3];

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // get the tags set by FragmentPagerAdapter
            switch (position) {
                case 0:
                    String firstTag = createdFragment.getTag();
                    mTagArray[0] = firstTag;
                    break;
                case 1:
                    String secondTag = createdFragment.getTag();
                    mTagArray[1] = secondTag;
                    break;
                case 2:
                    String thirdTag = createdFragment.getTag();
                    mTagArray[2] = thirdTag;
                    break;
            }
            // ... save the tags somewhere so you can reference them later
            return createdFragment;
        }

        public String getFragmentTag(int position) {
            return mTagArray[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

}