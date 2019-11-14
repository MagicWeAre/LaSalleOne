package com.wearemagic.lasalle.one.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.adapters.SummaryTypeAdapter;
import com.wearemagic.lasalle.one.common.CommonStrings;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.SummaryType;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private BalanceListener listener;

    private BalanceAsyncTaskRunner balanceTask = new BalanceAsyncTaskRunner();
    private ArrayList<String> periodValueList = new ArrayList<>();
    private ArrayList<SummaryType> summaryList = new ArrayList<>();
    private ArrayList<String> totalsList = new ArrayList<>();

    private RecyclerView summaryRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SummaryTypeAdapter stAdapter;
    private ArrayList<SummaryType> stList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout linearLayout;

    private TextView emptyMessage;

    private String sessionCookie = "";
    private int spinnerPosition = 0;
    private boolean viewCreated;

    public BalanceFragment() {}

    public interface BalanceListener {
        void onRedoLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            periodValueList = savedInstanceState.getStringArrayList("periodValueList");
            summaryList = savedInstanceState.getParcelableArrayList("summaryList");
            totalsList = savedInstanceState.getStringArrayList("totalsList");
            sessionCookie = savedInstanceState.getString("sessionCookie");
            spinnerPosition = savedInstanceState.getInt("spinnerPosition");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_balance, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.balanceRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        linearLayout = getView().findViewById(R.id.balanceLinearLayout);

        if(sessionCookie.isEmpty()){
            if (getArguments() != null){
                sessionCookie = getArguments().getString("sessionCookie");}}

        summaryRecyclerView = getView().findViewById(R.id.summaryRecyclerView);
        stAdapter = new SummaryTypeAdapter(stList, getContext());

        mLayoutManager = new LinearLayoutManager(getContext());

        emptyMessage = getView().findViewById(R.id.balanceEmpty);

        summaryRecyclerView.setLayoutManager(mLayoutManager);
        summaryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        summaryRecyclerView.setAdapter(stAdapter);
        viewCreated = true;
    }

    @Override
    public void onResume() {
        if (summaryList.isEmpty() && totalsList.isEmpty()){
            onRefresh();
        } else {
            fillSummary(summaryList, totalsList);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (balanceTask != null){
            balanceTask.cancel(true);
            balanceTask = null;}

        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}

        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SubjectsFragment.SubjectsListener) {
            listener = (BalanceFragment.BalanceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BalanceListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("spinnerPosition", spinnerPosition);
        outState.putStringArrayList("periodValueList", periodValueList);

        outState.putParcelableArrayList("summaryList", summaryList);
        outState.putStringArrayList("totalsList", totalsList);

        outState.putString("sessionCookie", sessionCookie);

    }

    @Override
    public void onRefresh() {
        if (periodValueList == null) {
            setEmptyMessage();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            if (viewCreated && !periodValueList.isEmpty()) {

                if (balanceTask != null) {
                    balanceTask.cancel(true);
                    balanceTask = null;
                }

                balanceTask = new BalanceAsyncTaskRunner();
                balanceTask.execute(sessionCookie);
            }
        }
    }

    public void sharePeriodValues(List<String> periodList) {
        if (periodList != null){
            periodValueList.addAll(periodList);
        } else {
            periodValueList = null;
        }    }

    public void shareSpinnerPosition(int spinnerPos) {
        spinnerPosition = spinnerPos;
    }

    private class BalanceAsyncTaskRunner extends AsyncTask<String, String, ArrayList<ArrayList>> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<ArrayList> doInBackground(String... params) {
            String serviceCookie = params[0];

            ArrayList<ArrayList> returnList = new ArrayList<>();
            boolean doBackgroundFinished = false;

            while (!doBackgroundFinished){
                if (isCancelled()) break;

                try {
                    String periodArgument = periodValueList.get(spinnerPosition);
                    returnList = getSummary(serviceCookie, periodArgument);
                } catch (IOException e) {
                    Log.e(TAG, "IOException on BalanceAsyncTask");
                } catch (NullPointerException np) {
                    Log.d(TAG, "NullPointerException on BalanceAsyncTask");
                    returnList = null;
                } catch (LoginTimeoutException lt) {
                    Log.d(TAG, "LoginTimeoutException on BalanceAsyncTask");
                    if (getActivity() != null){
                        listener.onRedoLogin();
                    }
                }

                doBackgroundFinished = true;
            }
            return returnList;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList> returnList) {
            if (returnList != null){
                if (!returnList.isEmpty()){

                    summaryList = returnList.get(0);
                    totalsList = returnList.get(1);

                    fillSummary(summaryList, totalsList);
                } else {
                    if (viewCreated){
                        Snackbar noInternetSB = Snackbar
                                .make(linearLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onRefresh(); }
                                });

                        noInternetSB.show();
                    }
                }
            } else {
                emptyMessage.setText(getString(R.string.error_parsing_data));
                emptyMessage.setVisibility(View.VISIBLE);
            }

            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setEmptyMessage() {
        emptyMessage.setText(getString(R.string.error_no_data));
        emptyMessage.setVisibility(View.VISIBLE);
    }


    private void fillSummary(ArrayList<SummaryType> summaryTypeList, ArrayList<String> summaryTotalsList){
        emptyMessage.setVisibility(View.GONE);

        if(getView() != null){

            CardView balanceCardView = getView().findViewById(R.id.balanceCardView);

            TextView periodTotal = getView().findViewById(R.id.periodTotalAmount);
            TextView othersPeriodsTotal = getView().findViewById(R.id.otherPeriodsTotalAmount);
            TextView coursesTotal = getView().findViewById(R.id.coursesTotalAmount);

            ConstraintLayout periodCC = getView().findViewById(R.id.periodBalanceCC);
            ConstraintLayout otherPeriodsCC = getView().findViewById(R.id.otherPeriodsBalanceCC);
            ConstraintLayout coursesCC = getView().findViewById(R.id.coursesBalanceCC);

            View separatorSummary = getView().findViewById(R.id.balanceSummarySeparator);

            stList.clear();
            stList.addAll(summaryTypeList);
            stAdapter.notifyDataSetChanged();

            boolean cardViewEmpty = true;

            if(!summaryTotalsList.get(0).isEmpty()) {
                periodTotal.setText(summaryTotalsList.get(0));
                periodCC.setVisibility(View.VISIBLE);
                cardViewEmpty = false;
            }

            if(!summaryTotalsList.get(1).isEmpty()) {
                othersPeriodsTotal.setText(summaryTotalsList.get(1));
                otherPeriodsCC.setVisibility(View.VISIBLE);
                othersPeriodsTotal.setVisibility(View.VISIBLE);
                cardViewEmpty = false;
            }

            if(!summaryTotalsList.get(2).isEmpty()) {
                coursesTotal.setText(summaryTotalsList.get(2));
                //if(summaryTotalsList.get(2).startsWith("+")) coursesTotal.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPositiveGreen));
                coursesCC.setVisibility(View.VISIBLE);
                coursesTotal.setVisibility(View.VISIBLE);
                cardViewEmpty = false;
            }

            if(!cardViewEmpty){
                separatorSummary.setVisibility(View.VISIBLE);
                balanceCardView.setVisibility(View.VISIBLE);
            }

            summaryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<ArrayList> getSummary(String serviceCookie, String period) throws IOException, LoginTimeoutException {
        ArrayList<ArrayList> returnList = new ArrayList<>();
        Element summaryTable = getBalanceDocument(serviceCookie, period).getElementById("ctl00_mainContentZone_ucAccountBalance_gvSummaryTypeTotals");
        ArrayList<SummaryType> summaryTypeList = getSummaryList(summaryTable, period);
        ArrayList<String> totalsList = getTotalSL(summaryTable);

        returnList.add(summaryTypeList);
        returnList.add(totalsList);

        return returnList;

        //"Total: " + totalsList.get(0);
        //"From other periods: " + totalsList.get(1);
        //"Balance: " + totalsList.get(2);
    }

    private Document getBalanceDocument(String serviceCookie, String period) throws IOException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", serviceCookie);

        Connection.Response balancePageGet = Jsoup.connect(CommonStrings.baseURL + "Finances/Balance.aspx")
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

        Document balanceDocumentPost = Jsoup.connect(CommonStrings.baseURL + "Finances/Balance.aspx")
                .data("ctl00$pageOptionsZone$btnSubmit", "Change")
                .data("ctl00$pageOptionsZone$ucBalanceOptions$ucbalanceViewOptions$ViewsButtonList", "Summary")
                .data("ctl00$pageOptionsZone$ucFinancialPeriods$PeriodsDropDown$ddlbPeriods", period)
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .data("__VIEWSTATEENCRYPTED", viewStateEncrypted.attr("value"))
                .cookies(cookies)
                .post();

        return balanceDocumentPost;
    }

    public static ArrayList<SummaryType> getSummaryList(Element table, String period) {
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

    public static ArrayList<String> getTotalSL(Element table) {
        ArrayList<String> summaryList = new ArrayList<>();
        Elements labels = table.getElementsByAttributeValue("class", "label");

        summaryList.add(assignSign(labels.get(3).text(), false));
        summaryList.add(assignSign(labels.get(4).text(), false));
        summaryList.add(assignSign(labels.get(5).text(), true));

        return summaryList;
    }

    public static String assignSign(String amount, boolean onlyNegative) {
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
//ctl00$pageOptionsZone$ucBalanceOptions$ucbalanceViewOptions$ViewsButtonList = Summary / SummaryTypeDetail / ChargeCreditDetail

