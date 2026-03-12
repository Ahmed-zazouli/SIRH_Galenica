package com.moovapps.capone.rh.GestionDeConge.helpers;

import com.axemble.vdoc.sdk.interfaces.IStorageResource;

import java.util.ArrayList;
import java.util.Arrays;

public class JoursOuvrableOuvres {

	public static ArrayList<Integer> getJoursOuvrables(IStorageResource societe) {
		ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6,7));
		int joursOuvrablesDebut = -1;
		int joursOuvrablesFin = -1;
		
		if(societe.getValue("JoursOuvrablesDebut") != null){
			joursOuvrablesDebut = Integer.parseInt(((String)societe.getValue("JoursOuvrablesDebut")));
		}
		if(societe.getValue("JoursOuvrablesFin") != null){
			joursOuvrablesFin = Integer.parseInt(((String)societe.getValue("JoursOuvrablesFin")));
		}
		if(joursOuvrablesDebut != -1 && joursOuvrablesFin != -1){	
			joursOuvrables.clear();
			int i = joursOuvrablesDebut;
			joursOuvrables.add(i);
			do {
				i = ((i+1)/8) != 0 ? 1 : (i+1);
				joursOuvrables.add(i);
			} while(i != joursOuvrablesFin);
		}
		
		return joursOuvrables;
	}
	
	public static ArrayList<Integer> getJoursOuvres(IStorageResource societe) {
		ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6));
		int joursOuvresDebut = -1;
		int joursOuvresFin = -1;
		
		if(societe.getValue("JoursOuvresDebut") != null){
			joursOuvresDebut = Integer.parseInt(((String)societe.getValue("JoursOuvresDebut")));
		}
		if(societe.getValue("JoursOuvresFin") != null){
			joursOuvresFin = Integer.parseInt(((String)societe.getValue("JoursOuvresFin")));
		}
		if(joursOuvresDebut != -1 && joursOuvresFin != -1){
			joursOuvres.clear();
			int i = joursOuvresDebut;
			joursOuvres.add(joursOuvresDebut);
			do {
				i = ((i+1)/8) != 0 ? 1 : (i+1);
				joursOuvres.add(i);
			} while(i != joursOuvresFin);			
		}
		
		return joursOuvres;
	}
	
}
