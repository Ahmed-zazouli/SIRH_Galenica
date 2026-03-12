package com.moovapps.ibb.rh.Controllers.Helpers;

import com.axemble.vdoc.sdk.interfaces.IStorageResource;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("###.##");
//		IResourceController resourceController = null;

    public DateHelper() {
    }

    public String formatDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public Date formatDateAsDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.parse(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isDateAfterDate(Date date1, Date date2) throws Exception {
        try {
            date1 = simpleFormat.parse(simpleFormat.format(date1));
            date2 = simpleFormat.parse(simpleFormat.format(date2));
        } catch (Exception e) {
//	        resourceController.alert("Something went wrong");
            e.printStackTrace();
            throw e;
        }
        return date1.after(date2);
    }

    public boolean isDateBeforeDate(Date date1, Date date2) throws Exception {
        try {
            date1 = simpleFormat.parse(simpleFormat.format(date1));
            date2 = simpleFormat.parse(simpleFormat.format(date2));
        } catch (Exception e) {
            throw e;
        }
        return date1.before(date2);
    }

    public boolean isDateEqualsDate(Date date1, Date date2) throws Exception {
        try {
            date1 = simpleFormat.parse(simpleFormat.format(date1));
            date2 = simpleFormat.parse(simpleFormat.format(date2));
        } catch (Exception e) {
            throw e;
        }
        return date1.equals(date2);
    }

    public boolean isDateInsideRange(Date dateToValidate, Date startDate, Date endDate) throws Exception {
        try {
            dateToValidate = simpleFormat.parse(simpleFormat.format(dateToValidate));
            startDate = simpleFormat.parse(simpleFormat.format(startDate));
            endDate = simpleFormat.parse(simpleFormat.format(endDate));
        } catch (Exception e) {
            throw e;
        }
        return (dateToValidate.equals(startDate) || dateToValidate.equals(endDate)) || (dateToValidate.after(startDate) && dateToValidate.before(endDate));
    }

    public boolean isTwoDatesOverlapped(Date start1, Date end1, Date start2, Date end2) throws Exception {
        try {
            start1 = simpleFormat.parse(simpleFormat.format(start1));
            end1 = simpleFormat.parse(simpleFormat.format(end1));
            start2 = simpleFormat.parse(simpleFormat.format(start2));
            end2 = simpleFormat.parse(simpleFormat.format(end2));
        } catch (Exception e) {
            throw e;
        }
        return (start1.equals(start2) || start1.equals(end2)) || (end1.equals(start2) || end1.equals(end2)) || (start2.before(end1) && end2.after(start1));
    }

    public long getDurationBetweenTwoDatesInMin(Date startDate, Date endDate) {
        long difference_In_Minutes = (endDate.getTime() - startDate.getTime()) / 60000;
        return difference_In_Minutes;
    }

    public double durationToDays(long durationInMinutes) {
        double numberOfWorkingHours = 480;
        double days = ((double) durationInMinutes / ((double) (numberOfWorkingHours)));
        // days = Math.ceil(days*4) / 4f;
        return Double.valueOf(df.format(days));
    }

    public double durationToDays(long durationInMinutes, IStorageResource societe) {
        double numberOfWorkingHours = 480;
        if (societe != null && societe.getValue("durationWorkHours") != null) {
            numberOfWorkingHours = ((Number) societe.getValue("durationWorkHours")).doubleValue();
        }
        double days = ((double) durationInMinutes / ((double) (numberOfWorkingHours)));
        // Uncomment this line in order to get 0.25 0.5 0.75 1
        // days = Math.ceil(days*4) / 4f;
        return Double.parseDouble(df.format(days).replace(',', '.'));
    }

    public String durationToString(long durationInMinutes) {
        long hours = durationInMinutes / 60;
        long minutes = durationInMinutes % 60;
        return String.format("%02d", hours) + "h " + String.format("%02d", minutes) + "min";
    }

    public String durationToStringIncludingDays(long durationInMinutes, IStorageResource societe) {
        double numberOfWorkingHours = 480;
        if (societe != null && societe.getValue("durationWorkHours") != null) {
            numberOfWorkingHours = ((Number) societe.getValue("durationWorkHours")).doubleValue();
        }
        long days = (((Number) durationInMinutes).longValue()) / (((Number) numberOfWorkingHours).longValue());
        long hours = (durationInMinutes - days * 1440) / 60;
        long minutes = durationInMinutes % 60;
        return ((days > 0) ? String.format("%02d", days) + "j " : "") + ((hours > 0) ? String.format("%02d", hours) + "h " : "") + String.format("%02d", minutes) + "min";
    }

    public String durationToStringIncludingDays(long durationInMinutes) {
        long days = durationInMinutes / 1440;
        long hours = (durationInMinutes - days * 1440) / 60;
        long minutes = durationInMinutes % 60;
        return ((days > 0) ? String.format("%02d", days) + "j " : "") + ((hours > 0) ? String.format("%02d", hours) + "h " : "") + String.format("%02d", minutes) + "min";
    }

    public String durationToString(Date startDate, Date endDate) {
        long duration = getDurationBetweenTwoDatesInMin(startDate, endDate);
        long hours = duration / 60;
        long minutes = duration % 60;
        return String.format("%02d", hours) + "h " + String.format("%02d", minutes) + "min";
    }

    public int durationToMonths(long durationInMinutes) {
        double numberOfWorkingHours = 60 * 24;
        double days = ((double) durationInMinutes / ((double) (numberOfWorkingHours)));
        int months = ((Number) (days / 30.417)).intValue();
        return months;
    }

    public int monthsBetweenTwoDates(Date startDate, Date endDate) {
        long difference_In_Minutes = (endDate.getTime() - startDate.getTime()) / 60000;
        double numberOfWorkingHours = 60 * 24;
        double days = ((double) difference_In_Minutes / ((double) (numberOfWorkingHours)));
        int months = ((Number) (days / 30.417)).intValue();
        return months;
    }
}
