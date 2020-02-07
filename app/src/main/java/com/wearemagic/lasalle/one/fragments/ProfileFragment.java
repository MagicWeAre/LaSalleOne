package com.wearemagic.lasalle.one.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.wearemagic.lasalle.one.LoginActivity;
import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;
import com.wearemagic.lasalle.one.providers.StudentData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProfileListener listener;

    private String sessionCookie;
    private String moodleCookie;
    private String salleId;
    private ConstraintLayout profileLayout;

    private ConnectivityManager mConnectivityManager;

    private ProfileAsyncTaskRunner profileTask = new ProfileAsyncTaskRunner();
    private PictureAsyncTaskRunner pictureTask = new PictureAsyncTaskRunner();

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private CardView profileCardView, addressCardView;
    LinearLayout email_layout, cellphone_layout, phone_layout, address_layout;
    private ArrayList<String> profileArray = new ArrayList<>();

    private boolean viewCreated;


    public ProfileFragment() { }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        sessionCookie = "";
        moodleCookie = "";
        salleId = "";

        if (savedInstanceState != null) {
            profileArray = savedInstanceState.getStringArrayList("profileArray");
            moodleCookie = savedInstanceState.getString("moodleCookie");
            sessionCookie = savedInstanceState.getString("sessionCookie");
            salleId = savedInstanceState.getString("salleId");
        } else {
            if (getArguments() != null) {
                sessionCookie = getArguments().getString("sessionCookie");
                moodleCookie = getArguments().getString("moodleCookie");
                salleId = getArguments().getString("salleId");
            }
        }

        mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        swipeRefreshLayout = view.findViewById(R.id.profileRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        viewPager = getActivity().findViewById(R.id.mainPager);
        tabLayout = getActivity().findViewById(R.id.mainTabs);
        profileLayout = getView().findViewById(R.id.profileLayout);

        profileCardView = getView().findViewById(R.id.profileCardView);
        addressCardView = getView().findViewById(R.id.addressCardView);

        email_layout = getView().findViewById(R.id.profileLinearA);
        cellphone_layout = getView().findViewById(R.id.profileLinearB);
        phone_layout = getView().findViewById(R.id.profileLinearC);

        address_layout = getView().findViewById(R.id.addressLinearA);

        final TextView emailText = getView().findViewById(R.id.profileEmail);
        final TextView cellphoneText = getView().findViewById(R.id.profileCellphone);
        final TextView phoneText = getView().findViewById(R.id.profilePhone);

        try {
            FileInputStream savedImageStream = getActivity().openFileInput(salleId + ".jpg");
            ImageView profileImage = getView().findViewById(R.id.profileImage);
            Bitmap bitmap = BitmapFactory.decodeStream(savedImageStream);
            profileImage.setImageBitmap(bitmap);
        }

        catch (FileNotFoundException e) { e.printStackTrace();
            if (pictureTask != null){
                pictureTask.cancel(true);
                pictureTask = null;}

            pictureTask = new PictureAsyncTaskRunner();
            pictureTask.execute(moodleCookie, salleId);
        }

        email_layout.setOnClickListener((View v) -> {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.label_clipboard), emailText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_email_clipboard_profile), Toast.LENGTH_SHORT).show();
        });

        email_layout.setOnLongClickListener((View v) -> {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.fromParts("mailto", emailText.getText().toString(), null));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.label_email)));
                return true;
        });

        cellphone_layout.setOnClickListener((View v) -> {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.label_clipboard), cellphoneText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_phone_clipboard_profile), Toast.LENGTH_SHORT).show();

        });

        cellphone_layout.setOnLongClickListener((View v) -> {

                Intent cellphoneIntent = new Intent(Intent.ACTION_DIAL);
                cellphoneIntent.setData(Uri.fromParts("tel", cellphoneText.getText().toString(), null));
                startActivity(cellphoneIntent);
                return true;
        });

        phone_layout.setOnClickListener((View v) -> {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.label_clipboard), phoneText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_phone_clipboard_profile), Toast.LENGTH_SHORT).show();
        });

        phone_layout.setOnLongClickListener((View v) -> {

                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                phoneIntent.setData(Uri.fromParts("tel", phoneText.getText().toString(), null));
                startActivity(phoneIntent);
                return true;
        });

        viewCreated = true;
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private void sendEventCode(String code) {
        switch (code) {
            case "ERROR_NO_INTERNET_CONNECTION":
                if(viewCreated) {
                    Snackbar noInternetSB = Snackbar
                            .make(profileLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.retry_internet_connection), (View view) -> {
                                onRefresh();
                            });

                    noInternetSB.show();
                }

                break;
            default:
                break;
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileListener) {
            listener = (ProfileListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        if(profileArray.isEmpty()){
            onRefresh();
        } else {
            fillProfile(profileArray);
        }
        super.onResume();
    }


    @Override
    public void onDestroy() {
        if (profileTask != null){
            profileTask.cancel(true);
            profileTask = null;}

        if (pictureTask != null){
            pictureTask.cancel(true);
            pictureTask = null;}
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("profileArray", profileArray);
        outState.putString("sessionCookie", sessionCookie);
        outState.putString("moodleCookie", moodleCookie);
        outState.putString("salleId", salleId);
        super.onSaveInstanceState(outState);
    }

    // Populate the three dot menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        viewPager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        if (!sessionCookie.isEmpty()) {
            if(viewCreated) {
                if (profileTask != null) {
                    profileTask.cancel(true);
                    profileTask = null;
                }

                profileTask = new ProfileAsyncTaskRunner();
                profileTask.execute(sessionCookie);
            }
        }
    }

    private class PictureAsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            String moodleCookie = params[0];
            String salleId = params[1];

            if(moodleCookie != null && !moodleCookie.isEmpty()){
                try {
                    StudentData.savePicture(moodleCookie, salleId, getActivity());
                } catch (IOException e){
                    e.printStackTrace();
                } catch (NullPointerException np) {
                    Log.d(TAG, np.getMessage());
                    startActivity(new Intent(getActivity(), LoginActivity.class));

                    if (getActivity() != null){
                        getActivity().finish();
                    }
                }
            }

            return salleId;

        }

        @Override
        protected void onPostExecute(String salleId) {
            ImageView profileImage = getView().findViewById(R.id.profileImage);
            Bitmap bitmap;

            try { FileInputStream savedImageStream = getActivity().openFileInput(salleId + ".jpg");
                bitmap = BitmapFactory.decodeStream(savedImageStream);
                profileImage.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e){e.printStackTrace();}

        }

        @Override
        protected void onPreExecute() {     }

        @Override
        protected void onProgressUpdate(String... text) {      }

    }

    private void fillProfile(ArrayList<String> profileArray){
        View separatorA = getView().findViewById(R.id.profileSeparatorA);
        View separatorB = getView().findViewById(R.id.profileSeparatorB);

        TextView nameText = getView().findViewById(R.id.profileName);
        TextView surnameText = getView().findViewById(R.id.profileSurname);
        TextView idText = getView().findViewById(R.id.profileId);
        TextView curpText = getView().findViewById(R.id.profileCurp);
        TextView emailText = getView().findViewById(R.id.profileEmail);
        TextView cellphoneText = getView().findViewById(R.id.profileCellphone);
        TextView phoneText = getView().findViewById(R.id.profilePhone);

        TextView addressOneText = getView().findViewById(R.id.addressLineOne);
        TextView addressTwoText = getView().findViewById(R.id.addressLineTwo);
        TextView addressThreeText = getView().findViewById(R.id.addressLineThree);
        TextView addressFourText = getView().findViewById(R.id.addressLineFour);
        TextView addressCountryText = getView().findViewById(R.id.addressStateCountry);

        String idStr = profileArray.get(0);
        String curpStr = profileArray.get(8);
        String nameStr = (profileArray.get(1) + " " + profileArray.get(2)).trim();
        String surnameStr = (profileArray.get(3) + " " + profileArray.get(4)).trim();

        idText.setText(idStr);
        nameText.setText(nameStr);
        surnameText.setText(surnameStr);

        if (!curpStr.isEmpty()){
            curpText.setText(curpStr);
        }

        String emailStr = profileArray.get(5);
        String cellphoneStr = profileArray.get(6);
        String phoneStr = profileArray.get(7);

        boolean profileEmpty = true;

        if (!emailStr.isEmpty()){
            emailText.setText(emailStr.toLowerCase());
            email_layout.setVisibility(View.VISIBLE);
            profileEmpty = false;
        }
        if (!cellphoneStr.isEmpty()){
            cellphoneText.setText(cellphoneStr);
            cellphone_layout.setVisibility(View.VISIBLE);
            profileEmpty = false;
        }
        if (!phoneStr.isEmpty()){
            phoneText.setText(phoneStr);
            phone_layout.setVisibility(View.VISIBLE);
            profileEmpty = false;
        }

        boolean separatorAVisible = false;
        boolean separatorBVisible = false;

        if (!emailStr.isEmpty() && !cellphoneStr.isEmpty()){
            separatorA.setVisibility(View.VISIBLE);
            separatorAVisible = true;
        }
        if (!cellphoneStr.isEmpty() && !phoneStr.isEmpty()){
            separatorB.setVisibility(View.VISIBLE);
            separatorBVisible = true;
        }

        if (!separatorAVisible && !separatorBVisible){
            if (!emailStr.isEmpty() && !phoneStr.isEmpty()){
                separatorA.setVisibility(View.VISIBLE);
                separatorAVisible = true;
            }
        }

        if (!profileEmpty){
            profileCardView.setVisibility(View.VISIBLE);
        }

        boolean male = false;

        if (curpStr.charAt(10) == 'H'){
            male = true;
        }

        listener.onSexSent(male);
        listener.onUserDataSent(nameStr, surnameStr, emailStr);

        /*
        boolean addressEmpty = true;
        if(!profileArray.get(8).isEmpty() || !profileArray.get(9).isEmpty()){
            addressOneText.setText((profileArray.get(8) + " " + profileArray.get(9)).trim());
            addressOneText.setVisibility(View.VISIBLE);
            addressEmpty = false;
        }
        if (!profileArray.get(11).isEmpty()){
            addressTwoText.setText(profileArray.get(10));
            addressTwoText.setVisibility(View.VISIBLE);
            addressEmpty = false;
        }
        if (!profileArray.get(11).isEmpty()){
            addressThreeText.setText(profileArray.get(11));
            addressThreeText.setVisibility(View.VISIBLE);
            addressEmpty = false;
        }
        if (!profileArray.get(12).isEmpty()){
            addressFourText.setText(profileArray.get(12));
            addressFourText.setVisibility(View.VISIBLE);
            addressEmpty = false;
        }
        if (!profileArray.get(13).isEmpty() || !profileArray.get(14).isEmpty() || !profileArray.get(15).isEmpty() || !profileArray.get(16).isEmpty()){
            addressCountryText.setText((profileArray.get(13) + " " + profileArray.get(14) + " " + profileArray.get(15) + ", " + profileArray.get(16)).trim());
            addressCountryText.setVisibility(View.VISIBLE);
            addressEmpty = false;
        }
        if (!addressEmpty){
            address_layout.setVisibility(View.VISIBLE);
            addressCardView.setVisibility(View.VISIBLE);
        }
        */
        profileLayout.setVisibility(View.VISIBLE);
    }

    public interface ProfileListener {
        void onSexSent(boolean male);
        void onUserDataSent(String name, String surname, String email);
        void onRedoLogin();
    }

    private class ProfileAsyncTaskRunner extends AsyncTask<String, String, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            String sessionCookie = params[0];
            ArrayList<String> profileArrayLocal = new ArrayList<>();

            try { profileArrayLocal = StudentData.getProfile(sessionCookie);
            } catch (IOException e) {
                Log.e(TAG, "IOException on ProfileAsyncTask");
            } catch (LoginTimeoutException lt) {
                Log.d(TAG, "LoginTimeoutException on ProfileAsyncTask");

                if (getActivity() != null){
                    listener.onRedoLogin();
                }
            } catch (NullPointerException np){
                Log.e(TAG, np.getMessage());
                profileArrayLocal = null;
            }


            return profileArrayLocal;

        }

        @Override
        protected void onPostExecute(ArrayList<String> profileArrayLocal) {
            if (profileArrayLocal != null){
                if (!profileArrayLocal.isEmpty()){
                    profileArray = profileArrayLocal;
                    fillProfile(profileArray);
                } else {
                    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if (isConnected){
                        sendEventCode("ERROR_HTTP_IO_EXCEPTION");
                    } else {
                        sendEventCode("ERROR_NO_INTERNET_CONNECTION");
                    }
                }
            } // else {}
            swipeRefreshLayout.setRefreshing(false);

        }

        @Override
        protected void onPreExecute() {
            profileLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onProgressUpdate(String... text) {        }

    }
}
