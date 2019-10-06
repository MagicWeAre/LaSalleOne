package com.wearemagic.lasalle.one.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

public class ParcialGrade implements Parcelable {
    private String parcialName;
    private String dueDate;
    private String gradeEntryDate;
    private String pointsEarned;
    private String maxPoints;
    private String percentage;
    private String maxPercentage;

    public ParcialGrade(String pN, String pE, String mP, String p, String mE) {
        this.parcialName = WordUtils.capitalizeFully(pN);
        if (!pE.isEmpty()){
            this.pointsEarned = pE;

        } else {
            this.pointsEarned = "N/A";

        }
        this.maxPoints = mP.replace("/", "");
        this.percentage = p.replace("%", "");
        this.maxPercentage = mE.replace("%", "");
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        if(!dueDate.isEmpty()){
            String[] dateParts = dueDate.split("/");
            String[] isoDateList = {dateParts[2], StringUtils.leftPad(dateParts[0],2, "0"),
                    StringUtils.leftPad(dateParts[1],2, "0")};
            this.dueDate = TextUtils.join("-", isoDateList);
        } else {
            this.dueDate = "";
        }
    }

    public String getGradeEntryDate() {
        return gradeEntryDate;
    }

    public void setGradeEntryDate(String gradeEntryDate) {
        if (!gradeEntryDate.isEmpty()){
            String[] dateParts = gradeEntryDate.split("/");
            String[] isoDateList = {dateParts[2], StringUtils.leftPad(dateParts[0],2, "0"),
                    StringUtils.leftPad(dateParts[1],2, "0")};
            this.gradeEntryDate = TextUtils.join("-", isoDateList);
        } else {
            this.gradeEntryDate = "";
        }
    }

    public String getParcialName() {
        return parcialName;
    }

    public String getPointsEarned() {
        return pointsEarned;
    }

    public String getMaxPoints() {
        return "/" + maxPoints;
    }

    public String getPercentage() {
        String returnString = percentage;
        if (!percentage.isEmpty()){
            returnString = "(" + returnString + " %)";
        }
        return returnString;
    }

    public String getMaxPercentage() {
        return maxPercentage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.parcialName);
        dest.writeString(this.dueDate);
        dest.writeString(this.gradeEntryDate);
        dest.writeString(this.pointsEarned);
        dest.writeString(this.maxPoints);
        dest.writeString(this.maxPercentage);
    }

    protected ParcialGrade(Parcel in) {
        this.parcialName = in.readString();
        this.dueDate = in.readString();
        this.gradeEntryDate = in.readString();
        this.pointsEarned = in.readString();
        this.maxPoints = in.readString();
        this.maxPercentage = in.readString();
    }

    public static final Parcelable.Creator<ParcialGrade> CREATOR = new Parcelable.Creator<ParcialGrade>() {
        @Override
        public ParcialGrade createFromParcel(Parcel source) {
            return new ParcialGrade(source);
        }

        @Override
        public ParcialGrade[] newArray(int size) {
            return new ParcialGrade[size];
        }
    };
}