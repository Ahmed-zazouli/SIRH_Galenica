package com.moovapps.capone.rh.GestionDeConge.helpers;

import java.util.Calendar;
import java.util.Date;

public class countSoldeConges {

	public int countSoldeConges(Date d1, Date d2, double d) {

		int soldeConges = 0;
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);

		while (!c1.after(c2)) {

			soldeConges += d;
			c1.add(Calendar.MONTH, 1);
		}

		System.out.println("Sunday Count = " + d);
		return soldeConges;
	}

}
