package com.moovapps.ibb.rh.GestionPaie.Excel.Exceptions;

public class IncoherantDataException extends Exception{
	public IncoherantDataException(){
		super("Merci de vérifier la coherance du fichier excel");
	}
}