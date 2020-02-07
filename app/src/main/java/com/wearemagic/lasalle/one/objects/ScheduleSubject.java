package com.wearemagic.lasalle.one.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.wearemagic.lasalle.one.providers.ObjectMethods;

import org.apache.commons.text.WordUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ScheduleSubject implements Parcelable {

    private String subjectName;
    private String startDate;
    private String endDate;
    private String credits;
    private String instructorName;
    private String type;
    private String sectionCode;
    private String courseCode;
    private String numericCode;
    private String periodCode;
    private boolean teorico;
    private boolean practico;
    private ArrayList<SchedulePiece> scheduleList;

    public ScheduleSubject(String subjectTitle, String datesString, String creditString, String instructorString, String nC, String p){

        //subjectTitle
        String[] titleList = subjectTitle.split(" - ", 2);
        this.subjectName = ObjectMethods.capitalizeSubjectTitle(WordUtils.capitalizeFully(titleList[1].trim()));
        this.subjectName = ObjectMethods.accentuateSubjectTitle(this.subjectName);

        String[] courseDataList = titleList[0].split("/");
        this.courseCode = courseDataList[0].trim();
        this.sectionCode = courseDataList[2].trim();

        if (courseDataList[1].contains("Teorico")){
            this.teorico = true;
        }

        if (courseDataList[1].contains("Practico")){
            this.practico = true;
        }

        //numericCode
        this.numericCode = nC;

        //datesString
        String[] datesList = datesString.split("-", 2);
        startDate = ObjectMethods.dateToISO(datesList[0]);
        endDate = ObjectMethods.dateToISO(datesList[1]);

        //creditString
        String[] creditList = creditString.split(" ", 2);
        this.credits = creditList[0].trim();
        this.type = creditList[1].trim();

        //instructorString
        if (!instructorString.isEmpty()){
            this.instructorName = orderName(instructorString);
        } else {
            this.instructorName = "";
        }

        this.periodCode = p;
    }

    public void setScheduleList(ArrayList<SchedulePiece> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getISOStartDate() {
        return startDate;
    }

    public String getISOEndDate() {
        return endDate;
    }

    public String getDateRange(){
        return startDate.concat(" / ").concat(endDate);
    }

    public String getCredits() {
        return credits;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public String getType() {
        return type;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getNumericCode() {
        return numericCode;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public boolean isTeorico() {
        return teorico;
    }

    public boolean isPractico() {
        return practico;
    }

    public ArrayList<SchedulePiece> getScheduleList() {
        return scheduleList;
    }

    public Date getStartDate() throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return isoFormat.parse(startDate);
    }
    public Date getEndDate() throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return isoFormat.parse(endDate);
    }

    public ArrayList<Integer> getWeekDaysList() {
        ArrayList<Integer> returnList = new ArrayList<>();

        for (SchedulePiece schedulePiece: scheduleList){
            int dayInt = schedulePiece.getDayInt();
            ArrayList<Integer> temporalList = new ArrayList<>();

            if (dayInt == 0) {
                Integer[] weekDays = new Integer[] {2, 3, 4, 5, 6};
                Collections.addAll(temporalList, weekDays);
            } else {
                temporalList.add(dayInt);
            }

            for (int addInt : temporalList) {
                if (!returnList.contains(addInt)) {
                    returnList.add(addInt);
                }
            }
        }
        return returnList;
    }

    public ArrayList<SchedulePiece> getClassesOfDay(Integer day) {
        ArrayList<SchedulePiece> returnList = new ArrayList<>();
        for (SchedulePiece schedulePiece : scheduleList) {
            Integer dayInt = schedulePiece.getDayInt();
            Integer[] weekDays = new Integer[] {2, 3, 4, 5, 6};
            boolean dayOfWeek = Arrays.asList(weekDays).contains(day);

            if (dayInt.equals(day) || (dayInt == 0 && dayOfWeek)) {
                returnList.add(schedulePiece);
            }
        }
        return returnList;
    }

    public ArrayList<SchedulePiece> getClassesAfterTime(Date date){

        ArrayList<SchedulePiece> returnList = new ArrayList<>();

        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(date);
        final int todayDayWeek = todayCal.get(Calendar.DAY_OF_WEEK);

        ArrayList<SchedulePiece> classesOfDay = getClassesOfDay(todayDayWeek);

        for (SchedulePiece piece : classesOfDay){
            Date startTime = piece.getStartDate();

            Date startDate = combineDates(date, startTime);

            if(startDate.after(date)){
                returnList.add(piece);
            }
        }

        Collections.sort(returnList, new Comparator<SchedulePiece>() {
            @Override public int compare(SchedulePiece sp1, SchedulePiece sp2) {
                return sp1.getStartDate().compareTo(sp2.getStartDate()); // Ascending
            }

        });
        return returnList;
    }

    public String orderName(String commaName) {
        String disorderedName = commaName.replace(",", "");
        String[] instructorArray = disorderedName.split("\\s+");
        String secondName = "";

        if (instructorArray.length > 3){
            secondName = instructorArray[3] + " "; }

        instructorName = WordUtils.capitalizeFully(instructorArray[2] + " " +
                secondName + instructorArray[0] + " " + instructorArray[1]);

        return instructorName;
    }

    private static Date combineDates(Date date, Date time) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);

        return cal.getTime();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.subjectName);
        dest.writeString(this.startDate);
        dest.writeString(this.endDate);
        dest.writeString(this.credits);
        dest.writeString(this.instructorName);
        dest.writeString(this.type);
        dest.writeString(this.sectionCode);
        dest.writeString(this.courseCode);
        dest.writeString(this.numericCode);
        dest.writeString(this.periodCode);
        dest.writeByte(this.teorico ? (byte) 1 : (byte) 0);
        dest.writeByte(this.practico ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.scheduleList);
    }

    protected ScheduleSubject(Parcel in) {
        this.subjectName = in.readString();
        this.startDate = in.readString();
        this.endDate = in.readString();
        this.credits = in.readString();
        this.instructorName = in.readString();
        this.type = in.readString();
        this.sectionCode = in.readString();
        this.courseCode = in.readString();
        this.numericCode = in.readString();
        this.periodCode = in.readString();
        this.teorico = in.readByte() != 0;
        this.practico = in.readByte() != 0;
        this.scheduleList = in.createTypedArrayList(SchedulePiece.CREATOR);
    }

    public static final Creator<ScheduleSubject> CREATOR = new Creator<ScheduleSubject>() {
        @Override
        public ScheduleSubject createFromParcel(Parcel source) {
            return new ScheduleSubject(source);
        }

        @Override
        public ScheduleSubject[] newArray(int size) {
            return new ScheduleSubject[size];
        }
    };
}
