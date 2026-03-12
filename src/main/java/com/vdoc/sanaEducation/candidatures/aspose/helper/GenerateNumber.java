package com.vdoc.sanaEducation.candidatures.aspose.helper;
//package com.ibm.icu.text;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.text.UFormat;

import java.util.Locale;
import java.text.Format;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

public class GenerateNumber {
public GenerateNumber(){

}
   public static String ConvertNumber(Number num){
       RuleBasedNumberFormat format = new RuleBasedNumberFormat(Locale.FRANCE,RuleBasedNumberFormat.SPELLOUT);
       return format.format(num);
   }
   public static boolean allowPeriodeEssai(Float num){
   return num % 0.5 ==0 ?true : false;
   }
   public static String ConvertNumbePeriodeEssai(Float num){
    String result = "";
    if(allowPeriodeEssai(num)){
        result+=((Number)num).intValue();
        if(((Number)num).intValue()!=1){
            result = "e ".concat(GenerateNumber.ConvertNumber(num.intValue()));
        }else{
            result = "'".concat(GenerateNumber.ConvertNumber(num.intValue()));
        }
        Float rest = num - ((Number)num).intValue();
        if(rest!=0){
            result+=" mois et demi";
        }else{
            result+=" mois";
        }
        return result;
    }else{
        return null;
    }

   }







}
