package com.moovapps.ibb.rh.Cummon;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateHelper {
	
	private static SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");

	public static String getDurationFromADateToNow(Date date){
		long difference_In_Milliseconds = ((new Date().getTime() - date.getTime())); 
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(difference_In_Milliseconds);
		int years = c.get(Calendar.YEAR) - 1970;
		int months = c.get(Calendar.MONTH);
		int days = c.get(Calendar.DAY_OF_MONTH);
		return years + " ans " + months + " mois " + days + " jours";  
	}


	public static String getDurationFromTwoDate(Date start,Date end){
		long difference_In_Milliseconds = ((end.getTime() - start.getTime()));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(difference_In_Milliseconds);
		int years = c.get(Calendar.YEAR) - 1970;
		int months = c.get(Calendar.MONTH);
		int days = c.get(Calendar.DAY_OF_MONTH);
		return years + " ans " + months + " mois " + days + " jours";
	}
	
	public static Integer getDurationFromADateToNowInYears(Date date){
		Integer years = null;
		try {
			long difference_In_Milliseconds = ((new Date().getTime() - date.getTime())); 
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(difference_In_Milliseconds);
			years = c.get(Calendar.YEAR) - 1970;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return years;  
	}
	
	
	
	public static boolean isTwoRangesOverlapped(Date start1,Date end1 ,Date start2, Date end2){
		try {
			start1 = simpleFormat.parse(simpleFormat.format(start1));
			end1 = simpleFormat.parse(simpleFormat.format(end1));
			start2 = simpleFormat.parse(simpleFormat.format(start2));
			end2 = simpleFormat.parse(simpleFormat.format(end2));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	    return (start1.equals(start2) || start1.equals(end2)) || (end1.equals(start2) || end1.equals(end2)) || (start2.before(end1) && end2.after(start1));
	}
	
	public static boolean isDateAfterDate(Date date1, Date date2){
		try {
			date1 = simpleFormat.parse(simpleFormat.format(date1));
			date2 = simpleFormat.parse(simpleFormat.format(date2));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return date1.after(date2);
	}
	
	public static boolean isDateBeforeDate(Date date1, Date date2){
		try {
			date1 = simpleFormat.parse(simpleFormat.format(date1));
			date2 = simpleFormat.parse(simpleFormat.format(date2));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return date1.before(date2);
	}
	
	public static boolean isDateEqualsDate(Date date1, Date date2){
		try {
			date1 = simpleFormat.parse(simpleFormat.format(date1));
			date2 = simpleFormat.parse(simpleFormat.format(date2));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return date1.equals(date2);
	}
	
}
