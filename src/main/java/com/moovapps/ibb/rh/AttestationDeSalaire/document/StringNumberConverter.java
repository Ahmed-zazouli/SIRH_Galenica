package com.moovapps.ibb.rh.AttestationDeSalaire.document;

import com.ibm.icu.text.RuleBasedNumberFormat;

import java.math.BigDecimal;
import java.util.Locale;

public class StringNumberConverter {
    public static String ConvertNumber(Number num) {
        if(num==null) return "";
        int integerPart = (int) Math.floor(num.floatValue());
        BigDecimal decimalPart = new BigDecimal(String.valueOf(num)).subtract(new BigDecimal(integerPart));
        RuleBasedNumberFormat format = new RuleBasedNumberFormat(Locale.FRANCE, RuleBasedNumberFormat.SPELLOUT);
        String number = format.format(integerPart);
        if (!decimalPart.equals(new BigDecimal("0.0"))) {
            String temp = String.valueOf(decimalPart);
            number += " virgule " + format.format(Integer.parseInt(temp.substring(2)));
        }
        return number.replace("-" ," ");
}

}
