package com.wearemagic.lasalle.one;

import com.wearemagic.lasalle.one.providers.ObjectMethods;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;



public class ObjectMethodsTest {

    // Object Methods
    @Test
    public void testObjectMethods_Accentuate() {
        ArrayList<String> subjectList = new ArrayList<>();
        subjectList.add("Matematicas");
        subjectList.add("Fisica");
        subjectList.add("Quimica");
        subjectList.add("Ecologia");
        subjectList.add("Argumentacion");


        ArrayList<String> accentuatedSubjectList = new ArrayList<>();
        accentuatedSubjectList.add("Matemáticas");
        accentuatedSubjectList.add("Física");
        accentuatedSubjectList.add("Química");
        accentuatedSubjectList.add("Ecología");
        accentuatedSubjectList.add("Argumentación");

        for (String subject : subjectList) {
            String accentuatedSubject = ObjectMethods.accentuateSubjectTitle(subject);
            assertEquals(accentuatedSubject, accentuatedSubjectList.get(subjectList.indexOf(subject)));
        }
    }

    @Test
    public void testObjectMethods_Capitalize() {
        ArrayList<String> subjectList = new ArrayList<>();
        subjectList.add("Matematicas Ii");
        subjectList.add("Fisica Iii");
        subjectList.add("Quimica Iv");
        subjectList.add("Ecologia Vi");
        subjectList.add("Argumentacion Vii");
        subjectList.add("Argumentacion Viii");
        subjectList.add("Matematicas Fm");

        ArrayList<String> accentuatedSubjectList = new ArrayList<>();
        accentuatedSubjectList.add("Matematicas II");
        accentuatedSubjectList.add("Fisica III");
        accentuatedSubjectList.add("Quimica IV");
        accentuatedSubjectList.add("Ecologia VI");
        accentuatedSubjectList.add("Argumentacion VII");
        accentuatedSubjectList.add("Argumentacion VIII");
        accentuatedSubjectList.add("Matematicas FM");

        for (String subject : subjectList) {
            String accentuatedSubject = ObjectMethods.capitalizeSubjectTitle(subject);
            assertEquals(accentuatedSubject, accentuatedSubjectList.get(subjectList.indexOf(subject)));
        }
    }
}
