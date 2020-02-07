package com.wearemagic.lasalle.one.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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


public class ChargesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private ChargesListener listener;
    private BalanceAsyncTaskRunner balanceTask = new BalanceAsyncTaskRunner();

    private RecyclerView chargesRecyclerView;
    private ConnectivityManager mConnectivityManager;
    private ChargeCreditAdapter chargesAdapter;
    private ArrayList<ChargeCredit> chargesList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<String> periodValueList = new ArrayList<>();
    private ConstraintLayout constraintLayout;
    private TextView emptyMessage;


    private String sessionCookie;
    private boolean viewCreated;
    private int spinnerPosition = 0;

    public ChargesFragment() {}

    public interface ChargesListener {
        void onRedoLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            spinnerPosition = savedInstanceState.getInt("spinnerPosition");
            chargesList = savedInstanceState.getParcelableArrayList("chargesList");
            periodValueList = savedInstanceState.getStringArrayList("periodValueList");
        }

        mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charges, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments() != null){
            sessionCookie = getArguments().getString("sessionCookie");}

        swipeRefreshLayout = view.findViewById(R.id.chargesRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        constraintLayout = getView().findViewById(R.id.chargesLayout);
        emptyMessage = getView().findViewById(R.id.chargesEmpty);

        chargesRecyclerView = getView().findViewById(R.id.chargesRecyclerView);
        chargesAdapter = new ChargeCreditAdapter(chargesList, getContext());

        RecyclerView.LayoutManager chargesLayoutManager = new LinearLayoutManager(getContext());
        chargesRecyclerView.setLayoutManager(chargesLayoutManager);
        chargesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chargesRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        chargesRecyclerView.setAdapter(chargesAdapter);
        viewCreated = true;

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        if(chargesList.isEmpty()){
           onRefresh();
        } else {
            chargesAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (balanceTask != null){
            balanceTask.cancel(true);
            balanceTask = null;

            if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);}
        }

        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SubjectsFragment.SubjectsListener) {
            listener = (ChargesFragment.ChargesListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ChargesListener");
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
        outState.putParcelableArrayList("chargesList", chargesList);
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

    public void sharePeriodValues(ArrayList<String> periodList) {
        if (periodList != null){
            periodValueList.addAll(periodList);
        } else {
            periodValueList = null;
        }    }

    public void shareSpinnerPosition(int spinnerPos) {
        spinnerPosition = spinnerPos;
    }

    private void sendEventCode(String code) {
        switch (code) {
            case "ERROR_NO_INTERNET_CONNECTION":
                if(viewCreated) {
                    Snackbar noInternetSB = Snackbar
                            .make(constraintLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
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

    private void setEmptyMessage() {
        emptyMessage.setText(getString(R.string.error_no_data));
        emptyMessage.setVisibility(View.VISIBLE);
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

                try {
                    returnList = StudentData.getChargesCredits(serviceCookie, periodArgument, false);
                } catch (IOException e) {
                    Log.e(TAG, "IOException on chargesTask");
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException on ChargesAsyncTask");
                    returnList = null;
                } catch (NullPointerException np) {
                    Log.d(TAG, "NullPointerException on ChargesAsyncTask");
                    returnList = null;
                } catch (LoginTimeoutException lt) {
                    Log.d(TAG, "LoginTimeoutException on ChargesAsyncTask");
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

                        ArrayList<ChargeCredit> chargesListLocal = (ArrayList) returnList.get(0);
                        chargesList.clear();

                        if (chargesListLocal.isEmpty()){
                            setEmptyMessage();
                        } else {
                            emptyMessage.setVisibility(View.GONE);
                            chargesList.addAll(chargesListLocal);
                        }

                        chargesAdapter.notifyDataSetChanged();

                        onPostFinished = true;
                    }
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

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onProgressUpdate(String... text) {        }

    }
}
