package com.wearemagic.lasalle.one.providers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Logins {
    public static String doLogin(String id, String pass, String baseURL) throws IOException {

        Connection.Response loginPageGet = Jsoup.connect(baseURL.concat("Home.aspx"))
                .method(Connection.Method.GET)
                .execute();

        Document loginDocument = loginPageGet.parse();

        Element eventValidation = loginDocument.select("input[name=__EVENTVALIDATION]").first();
        Element viewState = loginDocument.select("input[name=__VIEWSTATE]").first();

        Connection.Response loginPagePost = Jsoup.connect(baseURL.concat("Home.aspx"))
                .method(Connection.Method.POST)
                .data("ctl00$ucUserLogin$lvLoginUser$ucLoginUser$lcLoginUser$UserName", id)
                .data("ctl00$ucUserLogin$lvLoginUser$ucLoginUser$lcLoginUser$Password", pass)
                .data("ctl00$ucUserLogin$lvLoginUser$ucLoginUser$lcLoginUser$LoginButton", "Log+In")
                .data("__VIEWSTATE", viewState.attr("value"))
                .data("__EVENTVALIDATION", eventValidation.attr("value"))
                .execute();

        String serviceCookie = loginPagePost.cookie("SelfService");

        return serviceCookie;
    }

    public static String doMoodleLogin(String id, String pass) throws IOException {

        String moodleURL = "http://micurso.ulsaoaxaca.edu.mx/login/index.php";

        Connection.Response firstResponse = Jsoup.connect(moodleURL)
                .method(Connection.Method.GET)
                .execute();

        String firstMoodleCookie = firstResponse.cookie("MoodleSession");

        Map<String, String> firstCookieMap = new HashMap<>();
        firstCookieMap.put("MoodleSession", firstMoodleCookie);

        Document firstDocument = firstResponse.parse();

        Element logintoken = firstDocument.select("input[name=logintoken]").first();

        Connection.Response loginPost = Jsoup.connect(moodleURL)
                .method(Connection.Method.POST)
                .cookies(firstCookieMap)
                .data("username", id)
                .data("password", pass)
                .data("logintoken", logintoken.attr("value"))
                .execute();

        String secondMoodleCookie = loginPost.cookie("MoodleSession");

        return secondMoodleCookie;
    }

}
