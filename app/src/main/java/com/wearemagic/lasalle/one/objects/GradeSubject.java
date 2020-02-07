package com.wearemagic.lasalle.one.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.wearemagic.lasalle.one.providers.ObjectMethods;

import org.apache.commons.text.WordUtils;

public class GradeSubject implements Parcelable {
    private String subjectName;
    private String courseInfo;
    private String sectionCode;
    private String numericCode;
    private String periodCode;
    private String projectedGrade;
    private String finalGrade;
    private String qualityPoints;
    private double credits;
    private boolean teorico = false;
    private boolean practico = false;
    private boolean excento;
    private boolean interna = false;


    public GradeSubject(String nC, String p, String sN, String cI, String sC, String c) {
        this.subjectName = ObjectMethods.capitalizeSubjectTitle(WordUtils.capitalizeFully(sN));
        this.subjectName = ObjectMethods.accentuateSubjectTitle(this.subjectName);

        if(cI.contains("Teorico")) {
            this.teorico = true;
        }

        if(cI.contains("Practico")) {
            this.practico = true;
        }

        if(cI.contains("INT")) {
            this.interna = true;
        }

        this.courseInfo = cI.split(" ", 2)[0];
        this.sectionCode = sC;
        double cDouble = Double.parseDouble(c);
        this.credits = cDouble;

        this.numericCode = nC;
        this.periodCode = p;
    }

    public String getNumericCode() {
        return numericCode;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public double getCredits() {
        return credits;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public String getProjectedGrade() {
        return projectedGrade;
    }

    public void setProjectedGrade(String projectedGrade) {
        this.projectedGrade = projectedGrade;
    }

    public int gradeState(){
        int returnInt;
        double gSDouble = Double.parseDouble(getCurrentGrade());

        if (gSDouble < 60.00) {
            returnInt = 1;
        } else if (gSDouble >= 90.00) {
            returnInt = 2;
        } else if (gSDouble == 00.00) {
            returnInt = 3;
        } else {
            returnInt = 0;
        }

        return returnInt;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getQualityPoints() {
        return qualityPoints;
    }

    public void setQualityPoints(String qualityPoints) {

        if (qualityPoints != null && !qualityPoints.isEmpty()){

            double qPDouble = Double.parseDouble(qualityPoints);
            qPDouble = qPDouble * 10;

            this.qualityPoints = String.valueOf(qPDouble);
        } else {
            this.qualityPoints = "N/A";
        }
    }

    public String getCurrentGrade(){
        if (projectedGrade != null && !projectedGrade.isEmpty()){
            return projectedGrade;
        } else {
            return qualityPoints;
        }
    }

    public int getCurrentDoubleGrade(){
        if (!projectedGrade.isEmpty()){
            Double pG = Double.parseDouble(projectedGrade);
            return pG.intValue();
        } else {
            Double qP = Double.parseDouble(qualityPoints);
            return qP.intValue();
        }
    }

    public boolean isTeorico() {
        return teorico;
    }

    public boolean isPractico() {
        return practico;
    }

    public boolean isExcento() {
        return excento;
    }

    public void setExcento(boolean excento) {
        this.excento = excento;
    }

    public boolean isInterna() {
        return interna;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.subjectName);
        dest.writeString(this.courseInfo);
        dest.writeString(this.sectionCode);
        dest.writeString(this.numericCode);
        dest.writeString(this.periodCode);
        dest.writeString(this.projectedGrade);
        dest.writeString(this.finalGrade);
        dest.writeString(this.qualityPoints);
        dest.writeDouble(this.credits);
        dest.writeByte(this.teorico ? (byte) 1 : (byte) 0);
        dest.writeByte(this.practico ? (byte) 1 : (byte) 0);
        dest.writeByte(this.excento ? (byte) 1 : (byte) 0);
        dest.writeByte(this.interna ? (byte) 1 : (byte) 0);
    }

    protected GradeSubject(Parcel in) {
        this.subjectName = in.readString();
        this.courseInfo = in.readString();
        this.sectionCode = in.readString();
        this.numericCode = in.readString();
        this.periodCode = in.readString();
        this.projectedGrade = in.readString();
        this.finalGrade = in.readString();
        this.qualityPoints = in.readString();
        this.credits = in.readDouble();
        this.teorico = in.readByte() != 0;
        this.practico = in.readByte() != 0;
        this.excento = in.readByte() != 0;
        this.interna = in.readByte() != 0;
    }

    public static final Creator<GradeSubject> CREATOR = new Creator<GradeSubject>() {
        @Override
        public GradeSubject createFromParcel(Parcel source) {
            return new GradeSubject(source);
        }

        @Override
        public GradeSubject[] newArray(int size) {
            return new GradeSubject[size];
        }
    };
}


