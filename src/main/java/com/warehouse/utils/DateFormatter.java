package com.warehouse.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    public static String getFormattedDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }
}
