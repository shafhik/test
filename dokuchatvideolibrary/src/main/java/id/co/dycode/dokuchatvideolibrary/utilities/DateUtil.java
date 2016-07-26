package id.co.dycode.dokuchatvideolibrary.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by fahmi on 19/07/2016.
 */
public class DateUtil {

    public static String getStringLastChatDate(String time) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdfSource.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date sdate = null;
        try {
            sdate = sdfSource.parse(time);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
        sdfSource = new SimpleDateFormat("MMM-dd", Locale.getDefault());
        return sdfSource.format(sdate);
    }

    public static String getChatTime(String time) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdfSource.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date sdate = null;
        try {
            sdate = sdfSource.parse(time);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
        sdfSource = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfSource.format(sdate);
    }

    public static String getChatFromEventTime(String time) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdfSource.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date sdate = null;
        try {
            sdate = sdfSource.parse(time);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
        sdfSource = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfSource.format(sdate);
    }


    public static int getDayOfYear(String time) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdfSource.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date sdate = null;
        try {
            sdate = sdfSource.parse(time);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(sdate);

        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static int getDayOfYearFromEventTime(String time) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdfSource.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date sdate = null;
        try {
            sdate = sdfSource.parse(time);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(sdate);

        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static String getSeparatorDay(int dayOfYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        if (dayOfYear == calendar.get(Calendar.DAY_OF_YEAR)) {
            return "Hari ini";
        } else if (dayOfYear == calendar.get(Calendar.DAY_OF_YEAR) - 1) {
            return "Kemarin";
        } else {
            SimpleDateFormat sdfSource = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return sdfSource.format(calendar.getTime());
        }

    }

    public static String getDifferentStringValue(long diffSecond) {
        String time = "";
        if (diffSecond > (365 * 60 * 60 * 24 * 10)) {
            time = "-";
        } else if (diffSecond > (365 * 60 * 60 * 24)) {
            time = diffSecond / (365 * 60 * 60 * 24) + " tahun yang lalu";
        } else if (diffSecond > (60 * 60 * 24)) {
            time = diffSecond / (60 * 60 * 24) + " hari yang lalu";
        } else if (diffSecond > (60 * 60)) {
            time = diffSecond / (60 * 60) + " jam yang lalu";
        } else if (diffSecond > 60) {
            time = diffSecond / (60) + " menit yang lalu";
        } else if (diffSecond == 0) {
            time = "-";
        } else {
            time = "Baru saja";
        }

        int count;
        try {
            count = Integer.parseInt(time.substring(0, 2).trim());
            if (count == 1) {
                if (!time.contains("second")) {
                    time = time.replace("s", "");
                }
            }
        } catch (Exception e) {
        }

        return time;
    }


    public static long getMilisFromTimeStamp(String time, String timeZone) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
                Locale.ENGLISH);
        sdfSource.setTimeZone(TimeZone.getTimeZone(timeZone));
        Date sdate = null;
        try {
            sdate = sdfSource.parse(time);
            return sdate.getTime();
        } catch (ParseException e) {
            System.out.println(e.toString());
            return 0;
        }
    }

    public static long getDifference(String timestamp, String timeZone) {
        long milis = getMilisFromTimeStamp(timestamp, timeZone);
        return (new Date().getTime() - new Date(milis).getTime()) / 1000;
    }
}
