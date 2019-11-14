package com.wearemagic.lasalle.one.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.adapters.ChargeCreditAdapter;
import com.wearemagic.lasalle.one.common.CommonStrings;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.objects.ChargeCredit;

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


public class ChargesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private ChargesListener listener;
    private BalanceAsyncTaskRunner balanceTask = new BalanceAsyncTaskRunner();

    private RecyclerView chargesRecyclerView;
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

    private class BalanceAsyncTaskRunner extends AsyncTask<String, String, List> {

        @Override
        protected List doInBackground(String... params) {

            List returnList = new ArrayList();
            boolean doBackgroundFinished = false;

            while (!doBackgroundFinished){
                if (isCancelled()) break;

                String serviceCookie = params[0];
                String periodArgument = periodValueList.get(spinnerPosition);

                try {returnList = getCharges(serviceCookie, periodArgument);}
                catch (IOException e) {
                    Log.e(TAG, "IOException on chargesTask");
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException on ChargesAsyncTask");
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
                    if(viewCreated){
                        Snackbar noInternetSB = Snackbar
                                .make(constraintLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
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


    private List getCharges(String serviceCookie, String periodLocal) throws IOException, IllegalArgumentException, LoginTimeoutException {
        // [Charges] [TotalCharges]
        List returnList = new ArrayList();

        // Charges Table
        Element chargesTable = getBalanceDocument(serviceCookie, periodLocal).getElementById("ctl00_mainContentZone_ucAccountBalance_ChargeDetailsUserControl_gvBalanceDetails");
        String totalCharges = getTotalCC(chargesTable);
        List<ChargeCredit> chargesList = getChargesCreditsList(chargesTable, periodLocal);

        returnList.add(chargesList);
        returnList.add(totalCharges);

        return returnList;
    }

    private Document getBalanceDocument(String serviceCookie, String periodLocal) throws IOException, IllegalArgumentException, LoginTimeoutException {
        Map<String, String> cookies = new HashMap<String, String>();
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
                .data("ctl00$pageOptionsZone$ucBalanceOptions$ucbalanceViewOptions$ViewsButtonList", "ChargeCreditDetail")
                .data("ctl00$pageOptionsZone$ucFinancialPeriods$PeriodsDropDown$ddlbPeriods", periodLocal)
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .data("__VIEWSTATEENCRYPTED", viewStateEncrypted.attr("value"))
                .cookies(cookies)
                .post();
        return balanceDocumentPost;
    }

    public static List getChargesCreditsList(Element table, String periodLocal) {
        List<ChargeCredit> movementsList = new ArrayList();
        Elements tableRows = table.getElementsByTag("tr");
        tableRows.remove(tableRows.size() -1);

        for (Element row : tableRows) {
            if (!row.hasAttr("class")) {
                Elements cells = row.getElementsByTag("td");
                ChargeCredit chargeCredit = new ChargeCredit(row.getElementsByTag("span").first().text(),
                        cells.get(3).text(), cells.get(0).text(), periodLocal, cells.get(2).text());
                movementsList.add(chargeCredit);
            }
        }

        return movementsList;
    }

    public static String getTotalCC(Element table) {
        Elements chargesTableRows = table.getElementsByTag("tr");
        Element finalRowCharges = chargesTableRows.last();
        String total =  finalRowCharges.getElementsByTag("span").first().text();

        if(total.equals("No charges exist for the selected period.") || total.equals(" No credits exist for the selected period. ")){
            total = "$0.00";
        } else {
            total = total.split(":")[1].trim();
        }
        return total;
    }

}
