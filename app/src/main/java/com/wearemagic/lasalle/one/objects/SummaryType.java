package com.wearemagic.lasalle.one.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class SummaryType implements Parcelable {
    private String amount;
    private String type;
    private String period;

    public SummaryType(String amount, String type, String p) {
        String a;
        String t;
        if (amount.startsWith("(")) {
            a = amount.replace("(", "").replace(")", "");
            a = "+ " + a;
        } else {
            a = "- " + amount;
        }

        //t = type + ":";
        t = type;


        this.amount = a;
        this.type = t;
        this.period = p;
    }

    public String getAmount(){
        return this.amount;
    }

    public String getPeriod(){
        return this.period;
    }

    public String getType(){
        return this.type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.amount);
        dest.writeString(this.type);
        dest.writeString(this.period);
    }

    protected SummaryType(Parcel in) {
        this.amount = in.readString();
        this.type = in.readString();
        this.period = in.readString();
    }

    public static final Parcelable.Creator<SummaryType> CREATOR = new Parcelable.Creator<SummaryType>() {
        @Override
        public SummaryType createFromParcel(Parcel source) {
            return new SummaryType(source);
        }

        @Override
        public SummaryType[] newArray(int size) {
            return new SummaryType[size];
        }
    };
}
