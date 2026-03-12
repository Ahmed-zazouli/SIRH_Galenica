package com.moovapps.capone.rh.cummonHelpers;

import com.axemble.vdoc.sdk.interfaces.IResourceController;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidator {
	
	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
	IResourceController resourceController = null;
	
	public DateValidator(IResourceController resourceController) {
		this.resourceController = resourceController;
	}
	
	public DateValidator() {
	}
	
	public String formatDate(Date date, String format){
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	public boolean isDateAfterDate(Date date1, Date date2){
		try {
			date1 = simpleFormat.parse(simpleFormat.format(date1));
			date2 = simpleFormat.parse(simpleFormat.format(date2));
		} catch (Exception e) {
			resourceController.alert("Something went wrong");
			e.printStackTrace();
			return false;
		}
		return date1.after(date2);
	}
	
	public boolean isDateBeforeDate(Date date1, Date date2){
		try {
			date1 = simpleFormat.parse(simpleFormat.format(date1));
			date2 = simpleFormat.parse(simpleFormat.format(date2));
		} catch (Exception e) {
			resourceController.alert("Something went wrong");
			e.printStackTrace();
			return false;
		}
		return date1.before(date2);
	}
	
	public boolean isDateEqualsDate(Date date1, Date date2){
		try {
			date1 = simpleFormat.parse(simpleFormat.format(date1));
			date2 = simpleFormat.parse(simpleFormat.format(date2));
		} catch (Exception e) {
			resourceController.alert("Something went wrong");
			e.printStackTrace();
			return false;
		}
		return date1.equals(date2);
	}

	public boolean isDateInsideRange(Date dateToValidate,Date startDate, Date endDate){
		try {
			dateToValidate = simpleFormat.parse(simpleFormat.format(dateToValidate));
			startDate = simpleFormat.parse(simpleFormat.format(startDate));
			endDate = simpleFormat.parse(simpleFormat.format(endDate));
		} catch (Exception e) {
			resourceController.alert("Something went wrong");
			e.printStackTrace();
			return false;
		}
		return (dateToValidate.equals(startDate) || dateToValidate.equals(endDate)) || (dateToValidate.after(startDate) && dateToValidate.before(endDate));
	}
	
	public boolean isTwoDatesOverlapped(Date start1,Date end1 ,Date start2, Date end2){
		try {
			start1 = simpleFormat.parse(simpleFormat.format(start1));
			end1 = simpleFormat.parse(simpleFormat.format(end1));
			start2 = simpleFormat.parse(simpleFormat.format(start2));
			end2 = simpleFormat.parse(simpleFormat.format(end2));
		} catch (Exception e) {
			resourceController.alert("Something went wrong");
			e.printStackTrace();
			return false;
		}
	    return (start1.equals(start2) || start1.equals(end2)) || (end1.equals(start2) || end1.equals(end2)) || (start2.before(end1) && end2.after(start1));
	}
	
	public long getDurationBetweenTwoDatesInMin(Date startDate, Date endDate){
		long difference_In_Minutes = (endDate.getTime() - startDate.getTime()) / 60000; 
		return difference_In_Minutes;
	}
	
	public String durationToString(long durationInMinutes){
		long hours = durationInMinutes / 60;
		long minutes = durationInMinutes % 60;
		return String.format("%02d", hours)+"h "+String.format("%02d", minutes)+"min";
	}
	
	public String durationToStringIncludingDays(long durationInMinutes){
		long days = durationInMinutes / 1440;
		long hours = (durationInMinutes-days*1440) / 60;
		long minutes = durationInMinutes % 60;
		return ((days > 0) ? String.format("%02d", days)+"j " : "") + ((hours > 0) ? String.format("%02d", hours)+"h ":"" ) + String.format("%02d", minutes)+"min";
	}
	
	public String durationToString(Date startDate, Date endDate){
		long duration = getDurationBetweenTwoDatesInMin(startDate, endDate);
		long hours = duration / 60;
		long minutes = duration % 60;
		return String.format("%02d", hours)+"h "+String.format("%02d", minutes)+"min";
	}
	
}
