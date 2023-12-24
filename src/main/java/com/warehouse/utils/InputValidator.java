package com.warehouse.utils;

import java.util.regex.Pattern;
public class InputValidator {

    static final String SQL_INJ_PATTERN = ".*\\b(?:SELECT|FROM|WHERE|DROP|INSERT|UPDATE|DELETE|UNION|AND|OR)\\b.*";

    public static boolean containsSqlInjection(String input) {
        return Pattern.matches(SQL_INJ_PATTERN, input);
    }

    public static boolean isValidString(String input) {
        return input != null && !input.trim().isEmpty() && !containsSqlInjection(input);
    }

    public static boolean isValidPrice(double price) {
        return price >= 0;
    }

}
