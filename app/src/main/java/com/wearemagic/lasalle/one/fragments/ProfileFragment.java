package com.wearemagic.lasalle.one.fragments;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wearemagic.lasalle.one.LoginActivity;
import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.exceptions.LoginTimeoutException;

import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LaSalleOne";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProfileListener listener;

    private String sessionCookie;
    private String moodleCookie;
    private String salleId;
    private ConstraintLayout profileLayout;

    private ProfileAsyncTaskRunner profileTask = new ProfileAsyncTaskRunner();
    private PictureAsyncTaskRunner pictureTask = new PictureAsyncTaskRunner();

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private CardView profileCardView, addressCardView;
    LinearLayout email_layout, cellphone_layout, phone_layout, address_layout;
    private ArrayList<String> profileArray = new ArrayList<>();

    private boolean viewCreated;


    public ProfileFragment() { }

    public interface ProfileListener {
        void onSexSent(boolean male);
        void onRedoLogin();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

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

        email_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.label_clipboard), emailText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_email_clipboard_profile), Toast.LENGTH_SHORT).show();
            }
        });

        email_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.fromParts("mailto", emailText.getText().toString(), null));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.label_email)));
                return true;
            }
        });

        cellphone_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.label_clipboard), cellphoneText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_phone_clipboard_profile), Toast.LENGTH_SHORT).show();

            }
        });

        cellphone_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent cellphoneIntent = new Intent(Intent.ACTION_DIAL);
                cellphoneIntent.setData(Uri.fromParts("tel", cellphoneText.getText().toString(), null));
                startActivity(cellphoneIntent);
                return true;
            }
        });

        phone_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.label_clipboard), phoneText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_phone_clipboard_profile), Toast.LENGTH_SHORT).show();
            }
        });

        phone_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                phoneIntent.setData(Uri.fromParts("tel", phoneText.getText().toString(), null));
                startActivity(phoneIntent);
                return true;
            }
        });

        viewCreated = true;
        super.onViewCreated(view, savedInstanceState);

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
                    savePicture(moodleCookie, salleId);
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

    private class ProfileAsyncTaskRunner extends AsyncTask<String, String, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            String sessionCookie = params[0];
            ArrayList<String> profileArrayLocal = new ArrayList<>();

            try { profileArrayLocal = getProfile(sessionCookie);
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
                    if(viewCreated){
                        Snackbar noInternetSB = Snackbar
                                .make(profileLayout, getString(R.string.error_internet_failure), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.retry_internet_connection), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onRefresh(); }
                                });

                        noInternetSB.show();
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
        if (!emailStr.isEmpty() && !cellphoneStr.isEmpty()){
            separatorA.setVisibility(View.VISIBLE);
        }
        if (!cellphoneStr.isEmpty()){
            cellphoneText.setText(cellphoneStr);
            cellphone_layout.setVisibility(View.VISIBLE);
            profileEmpty = false;
        }
        if (!cellphoneStr.isEmpty() && !phoneStr.isEmpty()){
            separatorB.setVisibility(View.VISIBLE);
        }
        if (!phoneStr.isEmpty()){
            phoneText.setText(phoneStr);
            phone_layout.setVisibility(View.VISIBLE);
            profileEmpty = false;
        }

        if (!profileEmpty){
            profileCardView.setVisibility(View.VISIBLE);
        }

        boolean male = false;

        if (curpStr.charAt(10) == 'H'){
            male = true;
        }

        listener.onSexSent(male);

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

    private void savePicture(String moodleCookie, String userName) throws IOException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("MoodleSession", moodleCookie);

        Document profilePicDocument = Jsoup.connect("http://micurso.ulsaoaxaca.edu.mx/user/profile.php")
                .cookies(cookies)
                .get();

        Element profilePicture = profilePicDocument.getElementsByAttributeValueStarting("class", "userpicture").first();
        String pictureLink = profilePicture.attr("src");
        String linkTemplate = pictureLink.replace("f1", "f3");

        HttpURLConnection imageConnection = (HttpURLConnection) new URL(linkTemplate).openConnection();
        imageConnection.setRequestMethod("GET");
        imageConnection.addRequestProperty("Cookie","MoodleSession=" + moodleCookie);

        InputStream imageInput = imageConnection.getInputStream();
        Bitmap profileBitmap = BitmapFactory.decodeStream(imageInput);
        String filename = userName + ".jpg";

        File file = new File(getActivity().getFilesDir(), filename);

        try {
            OutputStream stream = new FileOutputStream(file);

            profileBitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
            stream.flush();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getProfile(String sessionCookie) throws IOException, LoginTimeoutException {

        String idBase = "ctl00_mainContentZone_LoginInformationControl_LoginInfoFormView_";
        String addressBase = "ctl00_mainContentZone_ucEditAddress_";

        Map<String, String> cookies = new HashMap<>();
        cookies.put("SelfService", sessionCookie);

        Document perfilDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Account/LoginInformation.aspx")
                .cookies(cookies)
                .get();

        if(perfilDocument.getElementById("ctl00_mainContent_lvLoginUser_ucLoginUser") != null){
            throw new LoginTimeoutException();
        }

        Element userName = perfilDocument.getElementById(idBase + "UserNameField_UserNameFieldValueLabel");
        Element firstName = perfilDocument.getElementById(idBase + "FirstNameField_FirstNameFieldValueLabel");
        Element middleName = perfilDocument.getElementById(idBase + "MiddleNameField_MiddleNameFieldValueLabel");
        Element lastName = perfilDocument.getElementById(idBase + "LastNameField_LastNameFieldValueLabel");
        Element lastNamePrefix = perfilDocument.getElementById(idBase + "LastNamePrefixField_LastNamePrefixFieldValueLabel");
        Element email = perfilDocument.getElementById(idBase + "EmailField_EmailFieldValueLabel");

        Document phoneDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Account/PhoneNumbers.aspx")
                .cookies(cookies)
                .get();

        Element mainPhoneTable = phoneDocument.getElementById("primaryphone");
        Element otherPhoneTable = phoneDocument.getElementById("otherphone");

        Map<String, String> phones = new HashMap<>();

        if (mainPhoneTable != null) {
            phones.put(mainPhoneTable.getElementsByTag("td").get(0).text(), mainPhoneTable.getElementsByTag("td").get(2).text()); }
        if (otherPhoneTable != null) {
            phones.put(otherPhoneTable.getElementsByTag("td").get(0).text(), otherPhoneTable.getElementsByTag("td").get(2).text()); }

        Document curpDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Records/Transcripts.aspx")
                .cookies(cookies)
                .get();

        Element curpElement = curpDocument.getElementsByAttribute("colspan").first().nextElementSibling();
        String curp = curpElement.text().replace("ID:", "").trim();

        /*Document addressDocument = Jsoup.connect("https://miportal.ulsaoaxaca.edu.mx/ss/Account/ChangeAddress.aspx")
                .cookies(cookies)
                .get();

        String addressLink = addressDocument.getElementById("ctl00_mainContentZone_ucChangeAddress_EditAddress_Hyperlink").attr("abs:href");

        Document editAddressDocument = Jsoup.connect(addressLink)
                .cookies(cookies)
                .get();

        String houseNumber = editAddressDocument.getElementById(addressBase + "AddressHouseNumberTextBox").attr("value");
        String addressLineOne = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox").attr("value");
        String addressLineTwo = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox2").attr("value");
        String addressLineThree = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox3").attr("value");
        String addressLineFour = editAddressDocument.getElementById(addressBase + "AddressAddressTextBox4").attr("value");
        String city = editAddressDocument.getElementById(addressBase + "AddressCityTextBox").attr("value");
        String postalCode = editAddressDocument.getElementById(addressBase + "AddressZipCodeTextBox").attr("value");

        Element stateDropdown = editAddressDocument.getElementById("ctl00_mainContentZone_ucEditAddress_AddressStateDropDown");
        String state = stateDropdown.getElementsByAttribute("selected").first().text();

        Element countryDropdown = editAddressDocument.getElementById("ctl00_mainContentZone_ucEditAddress_AddressCountryDropDown");
        String country = countryDropdown.getElementsByAttribute("selected").first().text();*/


        Element[] elementArray = {userName, firstName, middleName, lastName, lastNamePrefix, email};
        //String[] addressArray = {houseNumber, addressLineOne, addressLineTwo, addressLineThree, addressLineFour, city, postalCode, state, country};

        //String[] returnArray = new String[elementArray.length + addressArray.length + 2];
        String[] returnArray = new String[elementArray.length + 2];

        for (int i = 0; i < elementArray.length; i++) {
            returnArray[i] = WordUtils.capitalizeFully(elementArray[i].text()); }

        returnArray[elementArray.length] = ((phones.get("Celular") == null) ? "" : phones.get("Celular"));
        returnArray[elementArray.length + 1 ] = ((phones.get("Casa") == null) ? "" : phones.get("Casa"));

        //System.arraycopy(addressArray, 0, returnArray, 8, addressArray.length);

        ArrayList<String> profileArrayList = new ArrayList<>();
        profileArrayList.addAll(Arrays.asList(returnArray));
        profileArrayList.add(curp);

        return profileArrayList;
    }

}
