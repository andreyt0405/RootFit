package com.sportschule.rootfit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class DatePickerFragment {
    private String date;
    public int year;
    public int month;
    public int day;
    public DatePickerFragment()
    {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

    }
    public String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day;
        month = month + 1;
            day = cal.get(Calendar.DAY_OF_MONTH);
        date = makeDateString(day, month, year);
        if(date.length()<11){date=convertOddDayDate(date);}
        return date;
    }
    public String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }
    private String getMonthFormat(int month) {
        if (month == 1) return "JAN";
        if (month == 2) return "FEB";
        if (month == 3) return "MAR";
        if (month == 4) return "APR";
        if (month == 5) return "MAY";
        if (month == 6) return "JUN";
        if (month == 7) return "JUL";
        if (month == 8) return "AUG";
        if (month == 9) return "SEP";
        if (month == 10) return "OCT";
        if (month == 11) return "NOV";
        if (month == 12) return "DEC";
        return "JAN";
    }
    public String getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day;
        month = month + 1;
            DateFormat dateFormat = new SimpleDateFormat("dd");
            cal.add(Calendar.DATE, -1);
            day = Integer.parseInt(dateFormat.format(cal.getTime()));
        date = makeDateString(day, month, year);
        if(date.length()<11){date=convertOddDayDate(date);}
        return date;
    }
    private String convertOddDayDate(String date) {
        String oddDayDate = "";
        for (int i = 0; i < date.length(); i++) {
            oddDayDate += date.charAt(i);

            if (i == date.indexOf(" ")) {
                oddDayDate += "0";
            }
        }
        return oddDayDate;
    }
}
