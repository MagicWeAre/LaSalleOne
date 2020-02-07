package com.wearemagic.lasalle.one.providers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class WeAreMagic {
    public static String logURL = "https://wearemagic.dev/";

    public static String sendOpenEvent(String project, String version, String variant, String user) throws IOException {
        Connection.Response loginPageGet =
                Jsoup.connect(logURL.concat("log.php").concat("?")
                .concat("project=").concat(project)
                .concat("&version=").concat(version)
                .concat("&variant=").concat(variant)
                .concat("&user=").concat(user)
                .concat("&mode=").concat("1"))

                .method(Connection.Method.GET)
                .execute();

        Document debugDoc = loginPageGet.parse();

        String requestCode = loginPageGet.statusMessage();

        return requestCode;
    }

    public static String sendNewLoginEvent(String project, String version, String variant, String user, String email, String name, String surname) throws IOException {
        Connection.Response loginPageGet =
                Jsoup.connect(logURL.concat("log.php").concat("?")
                .concat("project=").concat(project)
                .concat("&version=").concat(version)
                .concat("&variant=").concat(variant)
                .concat("&mode=").concat("0")
                .concat("&user=").concat(user)
                .concat("&email=").concat(email)
                .concat("&name=").concat(name)
                .concat("&surname=").concat(surname))
                .method(Connection.Method.GET)
                .execute();

        Document debugDoc = loginPageGet.parse();

        String requestCode = loginPageGet.statusMessage();

        return requestCode;
    }
}
