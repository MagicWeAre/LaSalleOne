package com.wearemagic.lasalle.one.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.adapters.SummaryTypeAdapter;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.SummaryType;
import com.wearemagic.lasalle.one.providers.StudentData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BalanceFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private BalanceListener listener;

    private BalanceAsyncTaskRunner balanceTask = new BalanceAsyncTaskRunner();
    private ArrayList<String> periodValueList = new ArrayList<>();
    private ArrayList<SummaryType> summaryList = new ArrayList<>();
    private ArrayList<String> totalsList = new ArrayList<>();

    private ConnectivityManager mConnectivityManager;
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

        mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void setEmptyMessage() {
        emptyMessage.setText(getString(R.string.error_no_data));
        emptyMessage.setVisibility(View.VISIBLE);
    }

    private void sendEventCode(String code) {
        switch (code) {
            case "ERROR_NO_INTERNET_CONNECTION":
                if(viewCreated) {
                    Snackbar noInternetSB = Snackbar
                            .make(linearLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.retry_internet_connection), (View view) -> {
                                onRefresh();
                            });

                    noInternetSB.show();
                }

                break;
            default:
                emptyMessage.setText(getString(R.string.error_parsing_data));
                emptyMessage.setVisibility(View.VISIBLE);
                break;
        }

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
                    returnList = StudentData.getSummary(serviceCookie, periodArgument);
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
                    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if (isConnected){
                        sendEventCode("ERROR_HTTP_IO_EXCEPTION");
                    } else {
                        sendEventCode("ERROR_NO_INTERNET_CONNECTION");
                    }
                }
            } else {
                sendEventCode("ERROR_HTTP_PARSE");
            }

            swipeRefreshLayout.setRefreshing(false);
        }
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
}
//ctl00$pageOptionsZone$ucBalanceOptions$ucbalanceViewOptions$ViewsButtonList = Summary / SummaryTypeDetail / ChargeCreditDetail

