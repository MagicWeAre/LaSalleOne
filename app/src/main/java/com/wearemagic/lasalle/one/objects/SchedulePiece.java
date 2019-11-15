package com.wearemagic.lasalle.one.objects;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SchedulePiece implements Parcelable, Serializable {
    private String weekDay;
    private Integer dayInt;
    private String campus;
    private String building;
    private String room;
    private String startHour;
    private String endHour;
    private Date startDate = new Date();
    private Date endDate = new Date();

    public SchedulePiece(String unparsedSchedule) {
        unparsedSchedule = unparsedSchedule.replaceFirst("(\\d)", "|$1");
        String[] semicolonDivision = unparsedSchedule.split(";", 2);
        String[] commaDivision = semicolonDivision[1].split(",", 3);
        String[] barDivision = semicolonDivision[0].split("\\|", 2);
        String[] dashDivision = barDivision[1].split("-");

        weekDay = barDivision[0].trim();
        campus = commaDivision[0].trim();
        building = commaDivision[1].trim().replace("Edificio", "").trim();
        room = commaDivision[2].trim().replace("Room", "").trim();
        startHour = to24Hour(dashDivision[0].trim());
        endHour = to24Hour(dashDivision[1].trim());

        switch (weekDay) {
            case "Lunes": dayInt = 2;
                break;
            case "Martes": dayInt = 3;
                break;
            case "Miercoles": dayInt = 4;
                break;
            case "Jueves": dayInt = 5;
                break;
            case "Viernes": dayInt = 6;
                break;
            case "Sabado": dayInt = 7;
                break;
            case "Domingo": dayInt = 1;
                break;
            default: dayInt = 0;
                break;
        }

        SimpleDateFormat isoTime = new SimpleDateFormat("hh:mm", Locale.US);

        try {
            startDate = isoTime.parse(startHour);
            endDate = isoTime.parse(endHour);
        } catch (ParseException e) { }


    }

    public String getWeekDay() {
        return weekDay;
    }

    public Integer getDayInt() {
        return dayInt;
    }

    public String getStartHour() {
        return startHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public Date getStartDate(){
        return startDate;
    }

    public Date getEndDate(){
        return endDate;
    }

    public String getHourRange(){
        String hourRange = startHour.concat(" / ").concat(endHour);
        return hourRange;
    }

    public String getBuilding(){
        return building;
    }

    public String getRoom(){
        return room;
    }

    public String to24Hour(String twelveHour){
        String[] hourSuffix = twelveHour.split(" ", 2);
        String[] hourMinutes = hourSuffix[0].split(":");
        String newHour, newMinutes;

        if (hourSuffix[1].equals("PM") && !hourMinutes[0].equals("12")) {
            newHour = String.valueOf(Integer.parseInt(hourMinutes[0]) + 12);
        } else {
            newHour = StringUtils.leftPad(hourMinutes[0], 2, "0");
        }

        newMinutes = hourMinutes[1];

        return newHour.concat(":").concat(newMinutes);

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.weekDay);
        dest.writeValue(this.dayInt);
        dest.writeString(this.campus);
        dest.writeString(this.building);
        dest.writeString(this.room);
        dest.writeString(this.startHour);
        dest.writeString(this.endHour);
        dest.writeLong(this.startDate != null ? this.startDate.getTime() : -1);
        dest.writeLong(this.endDate != null ? this.endDate.getTime() : -1);
    }

    protected SchedulePiece(Parcel in) {
        this.weekDay = in.readString();
        this.dayInt = (Integer) in.readValue(Integer.class.getClassLoader());
        this.campus = in.readString();
        this.building = in.readString();
        this.room = in.readString();
        this.startHour = in.readString();
        this.endHour = in.readString();
        long tmpStartDate = in.readLong();
        this.startDate = tmpStartDate == -1 ? null : new Date(tmpStartDate);
        long tmpEndDate = in.readLong();
        this.endDate = tmpEndDate == -1 ? null : new Date(tmpEndDate);
    }

    public static final Creator<SchedulePiece> CREATOR = new Creator<SchedulePiece>() {
        @Override
        public SchedulePiece createFromParcel(Parcel source) {
            return new SchedulePiece(source);
        }

        @Override
        public SchedulePiece[] newArray(int size) {
            return new SchedulePiece[size];
        }
    };
}
