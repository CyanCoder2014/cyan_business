package com.cyancoder.taxpaysys.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateChange {


    public static Date getDate(String dateInput){
        Date date = null;
        String fromDateStr = dateInput != "" ? dateInput : "2023-05-01";   // to consider
        SimpleDateFormat fromDateObj = new SimpleDateFormat("yyyy-MM-dd");
        try {
             date = fromDateObj.parse(fromDateStr);
        }catch (Exception e) {}

        return date;
    }
}
