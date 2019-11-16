package com.wearemagic.lasalle.one.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.adapters.ChargeCreditAdapter;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.ChargeCredit;
import com.wearemagic.lasalle.one.providers.StudentData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CreditsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private CreditsListener listener;
    private ArrayList<ChargeCredit> creditsList = new ArrayList<>();
    BalanceAsyncTaskRunner balanceTask = new BalanceAsyncTaskRunner();

    private RecyclerView creditsRecyclerView;
    private ChargeCreditAdapter creditsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<String> periodValueList = new ArrayList<>();
    private ConstraintLayout constraintLayout;
    private TextView emptyMessage;

    private String sessionCookie;
    private boolean viewCreated;
    private int spinnerPosition = 0;

    public CreditsFragment() {}

    public interface CreditsListener {
        void onRedoLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            spinnerPosition = savedInstanceState.getInt("spinnerPosition");
            creditsList = savedInstanceState.getParcelableArrayList("creditsList");
            periodValueList = savedInstanceState.getStringArrayList("periodValueList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_credits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        if (getArguments() != null){
            sessionCookie = getArguments().getString("sessionCookie");}

        swipeRefreshLayout = view.findViewById(R.id.creditsRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        constraintLayout = getView().findViewById(R.id.creditsLayout);

        emptyMessage = getView().findViewById(R.id.creditsEmpty);

        creditsRecyclerView = getView().findViewById(R.id.creditsRecyclerView);
        creditsAdapter = new ChargeCreditAdapter(creditsList, getContext());

        RecyclerView.LayoutManager creditsLayoutManager = new LinearLayoutManager(getContext());
        creditsRecyclerView.setLayoutManager(creditsLayoutManager);
        creditsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        creditsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        creditsRecyclerView.setAdapter(creditsAdapter);
        viewCreated = true;

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        if(creditsList.isEmpty()){
            onRefresh();
        } else {
            creditsAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (balanceTask != null){
            balanceTask.cancel(true);
            balanceTask = null;

            if(swipeRefreshLayout != null){
                swipeRefreshLayout.setRefreshing(false);}        }

        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SubjectsFragment.SubjectsListener) {
            listener = (CreditsFragment.CreditsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CreditsListener");
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
        outState.putParcelableArrayList("creditsList", creditsList);
        outState.putStringArrayList("periodValueList", periodValueList);
    }

    // Populate the three dot menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
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
                    swipeRefreshLayout.setRefreshing(false);
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

    private class BalanceAsyncTaskRunner extends AsyncTask<String, String, List> {

        @Override
        protected List doInBackground(String... params) {

            List returnList = new ArrayList();
            boolean doBackgroundFinished = false;

            while (!doBackgroundFinished){
                if (isCancelled()) break;

                String serviceCookie = params[0];
                String periodArgument = periodValueList.get(spinnerPosition);

                try {returnList = StudentData.getChargesCredits(serviceCookie, periodArgument, true);}
                catch (IOException e) {
                    Log.e(TAG, "IOException on creditsTask");
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException on CreditsAsyncTask");
                    returnList = null;
                } catch (NullPointerException np) {
                    Log.d(TAG, "NullPointerException on CreditsAsyncTask");
                    returnList = null;
                } catch (LoginTimeoutException lt) {
                    Log.d(TAG, "LoginTimeoutException on CreditsAsyncTask");
                    if (getActivity() != null){
                        listener.onRedoLogin();
                    }
                }

                doBackgroundFinished = true;
                if (isCancelled()) break;
            }
            return returnList;
        }

        @Override
        protected void onPostExecute(List returnList) {
            boolean onPostFinished = false;
            if (returnList != null){
                if (!returnList.isEmpty()){
                    while (!onPostFinished) {
                        if (isCancelled()) break;

                        ArrayList<ChargeCredit> creditsListLocal = (ArrayList) returnList.get(0);
                        creditsList.clear();

                        if(creditsListLocal.isEmpty()){
                            setEmptyMessage();
                        } else {
                            emptyMessage.setVisibility(View.GONE);
                            creditsList.addAll(creditsListLocal);
                        }

                        creditsAdapter.notifyDataSetChanged();

                        onPostFinished = true;
                    }
                } else {
                    if(viewCreated) {
                        Snackbar noInternetSB = Snackbar
                                .make(constraintLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onRefresh();
                                    }
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

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onProgressUpdate(String... text) {        }

    }

    private void setEmptyMessage() {
        emptyMessage.setText(getString(R.string.error_no_data));
        emptyMessage.setVisibility(View.VISIBLE);
    }
}
