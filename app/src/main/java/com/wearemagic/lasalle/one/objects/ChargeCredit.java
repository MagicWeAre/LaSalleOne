package com.wearemagic.lasalle.one.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

public class ChargeCredit implements Parcelable {
    private String amount;
    private String date;
    private String description;

    private String period;
    private String type;

    public ChargeCredit(String amount, String desc, String date, String p, String t) {
        String a = amount;
        if (t.equals("Charge")) {a = "- " + a;}
        else {a = "+ " + a;}
        this.amount = a;

        String[] dateParts = date.split("/");
        String[] isoDateList = {dateParts[2], StringUtils.leftPad(dateParts[0],2, "0"),
                StringUtils.leftPad(dateParts[1],2, "0")};
        this.date = TextUtils.join("-", isoDateList);
        this.description = desc;

        this.period = p;
        this.type = t;
    }
    public String getDate() {
        return this.date;
    }

    public String getDesc() {
        return this.description;
    }

    public String getAmount() {
        return this.amount;
    }

    public String getType() {
        return this.type;
    }

    public String getPeriod() {
        return this.period;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.amount);
        dest.writeString(this.date);
        dest.writeString(this.description);
        dest.writeString(this.period);
        dest.writeString(this.type);
    }

    protected ChargeCredit(Parcel in) {
        this.amount = in.readString();
        this.date = in.readString();
        this.description = in.readString();
        this.period = in.readString();
        this.type = in.readString();
    }

    public static final Parcelable.Creator<ChargeCredit> CREATOR = new Parcelable.Creator<ChargeCredit>() {
        @Override
        public ChargeCredit createFromParcel(Parcel source) {
            return new ChargeCredit(source);
        }

        @Override
        public ChargeCredit[] newArray(int size) {
            return new ChargeCredit[size];
        }
    };
}