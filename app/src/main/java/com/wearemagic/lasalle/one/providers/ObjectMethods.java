package com.wearemagic.lasalle.one.providers;

import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

public class ObjectMethods {

    public static String capitalizeSubjectTitle(String subjectTitle){
        String returnString;
        returnString = subjectTitle
                .replaceAll("\\bViii\\b","VIII")
                .replaceAll("\\bVii\\b","VII")
                .replaceAll("\\bVi\\b","VI")
                .replaceAll("\\bIv\\b","IV")
                .replaceAll("\\bIii\\b","III")
                .replaceAll("\\bIi\\b","II")

                .replaceAll("\\bFm\\b","FM")
                .replaceAll("\\bQb\\b","QB")
                .replaceAll("\\bEa\\b","EA")
                .replaceAll("\\bHcs\\b","HCS");

        return returnString;
    }

    public static String accentuateSubjectTitle(String subjectTitle) {
        String returnString;

        returnString = subjectTitle.replace("Fisica", "Física");
        returnString = returnString.replaceAll("(\\w+)ia\\b", "$1ía");
        returnString = returnString.replace("Historía", "Historia");
        returnString = returnString.replaceAll("(\\w+)ion\\b", "$1ión");
        returnString = returnString.replace("Etica", "Ética");
        returnString = returnString.replace("Informatica", "Informática");
        returnString = returnString.replace("Frances", "Francés");
        returnString = returnString.replaceAll("Filosofic([ao])", "Filosófic$1");
        returnString = returnString.replaceAll("Estrategic([ao])", "Estratégic$1");
        returnString = returnString.replace("Calculo", "Cálculo");
        returnString = returnString.replace("Socioeconomica", "Socioeconómica");
        returnString = returnString.replace("Mexico", "México");
        returnString = returnString.replace("Matematicas", "Matemáticas");
        returnString = returnString.replace("Estadistica", "Estadística");
        returnString = returnString.replace("Metodos", "Métodos");
        returnString = returnString.replace("Fonetica", "Fonética");
        returnString = returnString.replace("Contemporanea", "Contemporánea");
        returnString = returnString.replace("Ingles", "Inglés");
        returnString = returnString.replaceAll("Comunicacio(n?)", "Comunicación");
        returnString = returnString.replaceAll("([Ll])ogi([c][oa]s?)", "$1ógi$2");
        returnString = returnString.replaceAll("Basic([ao]s?)", "Básic$1");
        returnString = returnString.replaceAll("Quimica(s?)", "Química$1");

        return returnString;
    }

    public static String dateToISO(String imperialDate){
        String[] dateList = imperialDate.split("/");
        String[] isoDateList = {dateList[2].trim(), StringUtils.leftPad(dateList[0].trim(),2, "0"),
                StringUtils.leftPad(dateList[1].trim(),2, "0")};
        String isoDate = TextUtils.join("-", isoDateList);
        return isoDate;
    }
}
