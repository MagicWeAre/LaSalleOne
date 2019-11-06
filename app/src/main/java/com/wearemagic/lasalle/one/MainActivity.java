package com.wearemagic.lasalle.one;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.fragments.BalanceFragment;
import com.wearemagic.lasalle.one.fragments.ChargesFragment;
import com.wearemagic.lasalle.one.fragments.CreditsFragment;
import com.wearemagic.lasalle.one.fragments.GradesFragment;
import com.wearemagic.lasalle.one.fragments.HomeFragment;
import com.wearemagic.lasalle.one.fragments.ProfileFragment;
import com.wearemagic.lasalle.one.fragments.SubjectsFragment;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        SubjectsFragment.SubjectsListener, GradesFragment.GradesListener, ProfileFragment.ProfileListener,
        BalanceFragment.BalanceListener, CreditsFragment.CreditsListener, ChargesFragment.ChargesListener {
    private static final String TAG = "LaSalleOne";
    public String packageName = "com.wearemagic.lasalle.one";

    private PeriodsAsyncTaskRunner periodsTask = new PeriodsAsyncTaskRunner();
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
    private int nightMode;
    private boolean redoingLogin;

    private boolean subjectsFragmentActive;
    private boolean gradesFragmentActive;
    private boolean balanceFragmentActive;

    private int subjectsSpinnerPosition;
    private int gradesSpinnerPosition;
    private int balanceSpinnerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

        nightMode = sharedP.getInt("nightMode", -1);

        AppCompatDelegate.setDefaultNightMode(nightMode);

        super.onCreate(savedInstanceState);

        Bundle loginBundle = getIntent().getExtras();

        if(loginBundle == null){
           onRedoLogin();
        }

        else {
            sessionCookie = loginBundle.getString("sessionCookie");
            moodleCookie = loginBundle.getString("moodleCookie");
            salleId = loginBundle.getString("salleId");

            if (savedInstanceState != null) {
                if (sessionCookie.isEmpty()) {
                    sessionCookie = savedInstanceState.getString("sessionCookie");
                }
                if (moodleCookie.isEmpty()) {
                    moodleCookie = savedInstanceState.getString("moodleCookie");
                }
                if (salleId.isEmpty()) {
                    salleId = savedInstanceState.getString("salleId");
                }

                //Active Fragments
                subjectsFragmentActive = savedInstanceState.getBoolean("subjectsFragmentActive");
                gradesFragmentActive = savedInstanceState.getBoolean("gradesFragmentActive");
                balanceFragmentActive = savedInstanceState.getBoolean("balanceFragmentActive");

                //Period Information
                subjectsPeriodNames = savedInstanceState.getStringArrayList("subjectsPeriodNames");
                subjectsPeriodValues = savedInstanceState.getStringArrayList("subjectsPeriodValues");
                gradesPeriodNames = savedInstanceState.getStringArrayList("gradesPeriodNames");
                gradesPeriodValues = savedInstanceState.getStringArrayList("gradesPeriodValues");
                balancePeriodNames = savedInstanceState.getStringArrayList("balancePeriodNames");
                balancePeriodValues = savedInstanceState.getStringArrayList("balancePeriodValues");

                //Spinner Positions
                subjectsSpinnerPosition = savedInstanceState.getInt("subjectsSpinnerPosition");
                gradesSpinnerPosition = savedInstanceState.getInt("gradesSpinnerPosition");
                balanceSpinnerPosition = savedInstanceState.getInt("balanceSpinnerPosition");

                //Fragment Tags
                balanceTag = savedInstanceState.getString("balanceTag");
                creditsTag = savedInstanceState.getString("creditsTag");
                chargesTag = savedInstanceState.getString("chargesTag");

                // Redoing Login Boolean
                redoingLogin = savedInstanceState.getBoolean("redoingLogin");

            } else {
                periodsTask = new PeriodsAsyncTaskRunner();
                periodsTask.execute(sessionCookie);
            }

            final String[] fragmentCookies = {sessionCookie, moodleCookie, salleId};

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

            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                if (android.os.Build.VERSION.SDK_INT < 26){
                    getSupportActionBar().setDisplayUseLogoEnabled(true);
                    getSupportActionBar().setLogo(R.mipmap.ic_launcher);
                }
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle arguments = new Bundle();

            arguments.putString("sessionCookie" , fragmentCookies[0]);
            arguments.putString("moodleCookie" , fragmentCookies[1]);
            arguments.putString("salleId", fragmentCookies[2] );

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


            balanceSpinner.setVisibility(View.GONE);
            subjectsSpinner.setVisibility(View.GONE);
            gradesSpinner.setVisibility(View.GONE);

            if (savedInstanceState != null){
                setSpinners();
                sendPeriods();
            }

            frameLayout = findViewById(R.id.mainFrameLayout);

            mainTabLayout = findViewById(R.id.mainTabs);
            mainTabLayout.setupWithViewPager(mainViewPager);

            bottomNavigation = findViewById(R.id.mainBottomNavigation);
            navigationView = findViewById(R.id.mainDrawerNavigation);
            navigationView.setCheckedItem(R.id.mainDrawerHome);

            View headerLayout = navigationView.inflateHeaderView(R.layout.navigation_header);
            drawerID = headerLayout.findViewById(R.id.drawerID);
            if(drawerID != null) { drawerID.setText(salleId); }
            else { Log.e(TAG, "TextView null"); }


            if (subjectsFragmentActive){
                mainViewPager.setVisibility(View.GONE);
                mainTabLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);

                subjectsSpinner.setVisibility(View.VISIBLE);
                gradesSpinner.setVisibility(View.GONE);
                balanceSpinner.setVisibility(View.GONE);

                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

             else if (gradesFragmentActive){
                mainViewPager.setVisibility(View.GONE);
                mainTabLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);

                subjectsSpinner.setVisibility(View.GONE);
                gradesSpinner.setVisibility(View.VISIBLE);
                balanceSpinner.setVisibility(View.GONE);

                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

             else if (balanceFragmentActive){
                mainViewPager.setVisibility(View.VISIBLE);
                mainTabLayout.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);

                balanceSpinner.setVisibility(View.VISIBLE);
                gradesSpinner.setVisibility(View.GONE);
                subjectsSpinner.setVisibility(View.GONE);

                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            else {
                mainViewPager.setVisibility(View.GONE);
                mainTabLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);

                balanceSpinner.setVisibility(View.GONE);
                gradesSpinner.setVisibility(View.GONE);
                subjectsSpinner.setVisibility(View.GONE);

                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    drawerLayout.closeDrawers();

                    switch (menuItem.getItemId()){
                        case R.id.mainDrawerEnd: {
                            SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedP.edit();
                            editor.remove("accountUser");
                            editor.remove("accountPass");
                            editor.apply();

                            startActivity(new Intent( getApplicationContext(), LoginActivity.class));
                            finish();
                            return true;
                        }
                        case R.id.mainDrawerSettings: {
                            startActivity(new Intent( getApplicationContext(), SettingsActivity.class));
                            return true;
                        }

                        case R.id.mainDrawerHelp: {
                            startActivity(new Intent( getApplicationContext(), HelpActivity.class));
                            return true;
                        }

                        case R.id.mainDrawerDocuments: {
                            String url = "http://alumnos.ulsaoaxaca.edu.mx/";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }

                        case R.id.mainDrawerFIBU: {
                            String url = "http://fibu.ulsaoaxaca.edu.mx/";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }

                    }

                    return true;
                }
            });


            bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    subjectsFragmentActive = false;
                    gradesFragmentActive = false;
                    balanceFragmentActive = false;

                    mainViewPager.setVisibility(View.GONE);
                    mainTabLayout.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.VISIBLE);

                    balanceSpinner.setVisibility(View.GONE);
                    gradesSpinner.setVisibility(View.GONE);
                    subjectsSpinner.setVisibility(View.GONE);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);

                    switch (menuItem.getItemId()){
                        case R.id.mainMenuHome: {
                            setFragment(homeFragment);
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                            return true;
                        }
                        case R.id.mainMenuSubjects: {
                            setFragment(subjectsFragment);
                            subjectsFragmentActive = true;
                            subjectsSpinner.setVisibility(View.VISIBLE);
                            return true;
                        }
                        case R.id.mainMenuGrades: {
                            setFragment(gradesFragment);
                            gradesFragmentActive = true;
                            gradesSpinner.setVisibility(View.VISIBLE);
                            return true;
                        }
                        case R.id.mainMenuBalance: {
                            setFragment(null);
                            balanceFragmentActive = true;

                            mainViewPager.setVisibility(View.VISIBLE);
                            mainTabLayout.setVisibility(View.VISIBLE);
                            frameLayout.setVisibility(View.GONE);
                            mainViewPager.setCurrentItem(0);
                            balanceSpinner.setVisibility(View.VISIBLE);
                            return true;
                        }
                        case R.id.mainMenuProfile: {
                            setFragment(profileFragment);
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                            return true;
                        }

                        default:
                            return false;
                }}
            });

        }
    }

    protected void onResume(){
        navigationView = findViewById(R.id.mainDrawerNavigation);
        navigationView.setCheckedItem(R.id.mainDrawerHome);
        AppCompatDelegate.setDefaultNightMode(nightMode);
        super.onResume();
    }

    @Override
    public void onDestroy() {
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

        //Active Fragments
        outState.putBoolean("subjectsFragmentActive", subjectsFragmentActive);
        outState.putBoolean("gradesFragmentActive", gradesFragmentActive);
        outState.putBoolean("balanceFragmentActive", balanceFragmentActive);

        //Period Information
        outState.putStringArrayList("subjectsPeriodNames", subjectsPeriodNames);
        outState.putStringArrayList("subjectsPeriodValues", subjectsPeriodValues);
        outState.putStringArrayList("gradesPeriodNames", gradesPeriodNames);
        outState.putStringArrayList("gradesPeriodValues", gradesPeriodValues);
        outState.putStringArrayList("balancePeriodNames", balancePeriodNames);
        outState.putStringArrayList("balancePeriodValues", balancePeriodValues);

        //Spinner Positions
        outState.putInt("subjectsSpinnerPosition", subjectsSpinnerPosition);
        outState.putInt("gradesSpinnerPosition", gradesSpinnerPosition);
        outState.putInt("balanceSpinnerPosition", balanceSpinnerPosition);

        //Fragment Tags
        outState.putString("balanceTag", vpAdapter.getFragmentTag(0));
        outState.putString("creditsTag", vpAdapter.getFragmentTag(1));
        outState.putString("chargesTag", vpAdapter.getFragmentTag(2));

        outState.putBoolean("redoingLogin", redoingLogin);
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
        if (mainFragment != null){
        getSupportFragmentManager()
                .beginTransaction()
                .hide(this.homeFragment)
                .hide(this.gradesFragment)
                .hide(this.subjectsFragment)
                .hide(this.profileFragment)
                .show(mainFragment)
                .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(this.homeFragment)
                    .hide(this.gradesFragment)
                    .hide(this.subjectsFragment)
                    .hide(this.profileFragment)
                    .commit();
        }
    }

    public void sendPeriods() {
        balanceFragment.sharePeriodValues(balancePeriodValues);
        chargesFragment.sharePeriodValues(balancePeriodValues);
        creditsFragment.sharePeriodValues(balancePeriodValues);
        balanceFragment.onRefresh();
        chargesFragment.onRefresh();
        creditsFragment.onRefresh();

        gradesFragment.sharePeriodValues(gradesPeriodValues);
        gradesFragment.onRefresh();

        subjectsFragment.sharePeriodValues(subjectsPeriodValues);
        subjectsFragment.onRefresh();


        setSpinners();
    }


    @Override
    public void onSubjectInfoSent(ArrayList infoList) {
        homeFragment.onSubjectsNumberSent((String) infoList.get(0));
        homeFragment.onCurrentSubjectsSent((ArrayList<ScheduleSubject>) infoList.get(1));
        homeFragment.onNextSubjectsSent((ArrayList<ScheduleSubject>) infoList.get(2));
    }

    public void onSexSent(boolean sex){
        homeFragment.setSex(sex);
    }

    @Override
    public void onGradesInfoSent(ArrayList infoList) {
        homeFragment.onGradesHighestSent((String) infoList.get(0));
        homeFragment.onGradesLowestSent((String) infoList.get(1));
        homeFragment.onGradesAvgSent((String) infoList.get(2));
    }

    public void onRedoLogin(){
        if(!redoingLogin){
            redoingLogin = true;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private class PeriodsAsyncTaskRunner extends AsyncTask<String, String, ArrayList<ArrayList<ArrayList<String>>>> {

        @Override
        protected void onPreExecute() {
            //swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<ArrayList<ArrayList<String>>> doInBackground(String... params) {
            String sessionCookie = params[0];

            ArrayList<ArrayList<ArrayList<String>>> periodsList = new ArrayList<>();

            try {
                periodsList = getPeriods(sessionCookie);
            } catch (IOException e) {
                Log.e(TAG, "IOException on PeriodAsyncTask");
            } catch (NullPointerException np) {
                Log.d(TAG, np.getMessage());
            } catch (LoginTimeoutException lt) {
                Log.d(TAG, "LoginTimeoutException on PeriodAsyncTask");
                onRedoLogin();
            }

            return periodsList;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<ArrayList<String>>> periodsList) {

            if(periodsList.isEmpty()){
                Snackbar noInternetSB = Snackbar
                        .make(frameLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                periodsTask = new PeriodsAsyncTaskRunner();
                                periodsTask.execute(sessionCookie); }
                        });

                noInternetSB.show();
            } else {
                ArrayList<ArrayList<String>> subjectsPeriodList = periodsList.get(0);
                ArrayList<ArrayList<String>> gradesPeriodList = periodsList.get(1);
                ArrayList<ArrayList<String>> balancePeriodList = periodsList.get(2);

                subjectsPeriodNames = subjectsPeriodList.get(0);
                subjectsPeriodValues = subjectsPeriodList.get(1);
                gradesPeriodNames = gradesPeriodList.get(0);
                gradesPeriodValues = gradesPeriodList.get(1);
                balancePeriodNames = balancePeriodList.get(0);
                balancePeriodValues = balancePeriodList.get(1);

                sendPeriods();
            }
            //swipeRefreshLayout.setRefreshing(false);
        }
    }

    private ArrayList<ArrayList<ArrayList<String>>> getPeriods(String sessionCookie) throws IOException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", sessionCookie);
        ArrayList<ArrayList<ArrayList<String>>> returnList = new ArrayList<>();

        //Subjects Periods
        Document scheduleDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/ClassSchedule.aspx")
                .cookies(cookies)
                .get();

        if(scheduleDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }


        ArrayList<String> subjectsPeriodNames = new ArrayList<>();
        ArrayList<String> subjectsPeriodValues = new ArrayList<>();

        if(scheduleDocument.getElementsByClass("msgWarning").isEmpty()){
            Elements subjectsPeriods = scheduleDocument.getElementById("ctl00_pageOptionsZone_ddlbPeriods").getElementsByTag("option");

            for (Element subjectsPeriod : subjectsPeriods) {
                if (subjectsPeriod.attr("value").endsWith("|")) {
                    subjectsPeriodNames.add(subjectsPeriod.text());
                    subjectsPeriodValues.add(subjectsPeriod.attr("value"));
                }
            }
        } else {
            subjectsPeriodValues = null;
        }

        //Grades Periods
        Document gradesDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/GradeReport.aspx")
                .cookies(cookies)
                .get();

        ArrayList<String> gradesPeriodNames = new ArrayList<>();
        ArrayList<String> gradesPeriodValues = new ArrayList<>();

        if(gradesDocument.getElementsByClass("msgWarning").isEmpty()){
            Elements gradesPeriods = gradesDocument.getElementById("ctl00_pageOptionsZone_GradeReportOptions_PeriodDropDown").getElementsByTag("option");

            for (Element gradesPeriod : gradesPeriods) {
                gradesPeriodNames.add(gradesPeriod.text());
                gradesPeriodValues.add(gradesPeriod.attr("value"));
            }
        } else {
            gradesPeriodValues = null;
        }

        //Balance Periods
        Document financesDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Finances/Balance.aspx")
                .cookies(cookies)
                .get();

        ArrayList<String> balancePeriodNames = new ArrayList<>();
        ArrayList<String> balancePeriodValues = new ArrayList<>();

        if(financesDocument.getElementsByClass("msgWarning").isEmpty()){
            Elements balancePeriods = financesDocument.getElementById("ctl00_pageOptionsZone_ucFinancialPeriods_PeriodsDropDown_ddlbPeriods").getElementsByTag("option");

            for (Element balancePeriod : balancePeriods) {
                balancePeriodNames.add(balancePeriod.text());
                balancePeriodValues.add(balancePeriod.attr("value"));
            }
        } else {
            balancePeriodValues = null;
        }

        ArrayList<ArrayList<String>> subjectsPeriodList = new ArrayList<>();
        subjectsPeriodList.add(subjectsPeriodNames);
        subjectsPeriodList.add(subjectsPeriodValues);
        ArrayList<ArrayList<String>> gradesPeriodList = new ArrayList<>();
        gradesPeriodList.add(gradesPeriodNames);
        gradesPeriodList.add(gradesPeriodValues);
        ArrayList<ArrayList<String>> balancePeriodList = new ArrayList<>();
        balancePeriodList.add(balancePeriodNames);
        balancePeriodList.add(balancePeriodValues);

        returnList.add(subjectsPeriodList);
        returnList.add(gradesPeriodList);
        returnList.add(balancePeriodList);

        return returnList;
    }

    public void setSpinners(){
        ArrayAdapter<String> subjectsSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_style, subjectsPeriodNames);
        subjectsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsSpinnerAdapter);

        ArrayAdapter<String> gradesSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_style, gradesPeriodNames);
        gradesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradesSpinner.setAdapter(gradesSpinnerAdapter);

        ArrayAdapter<String> balanceSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_style, balancePeriodNames);
        balanceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        balanceSpinner.setAdapter(balanceSpinnerAdapter);
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

