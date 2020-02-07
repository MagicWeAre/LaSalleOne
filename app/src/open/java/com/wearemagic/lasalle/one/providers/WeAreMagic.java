package com.wearemagic.lasalle.one.providers;

import java.io.IOException;

public class WeAreMagic {
    public static String logURL = "https://wearemagic.dev/";

    public static String sendOpenEvent(String project, String version, String variant, String user) throws IOException {
        return "CONNECTION.FALSE";
    }

    public static String sendNewLoginEvent(String project, String version, String variant, String user, String email, String name, String surname) throws IOException {
        return "CONNECTION.FALSE";
    }
}
