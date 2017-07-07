package ru.gnm;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws Exception {
//        String allArgsString = Arrays.toString(args);
//        allArgsString = allArgsString.substring(1, allArgsString.length() - 2);
        String allArgsString = "-a 192.168.0.0-192.168.255.255 -p 554-554 -t 40";
        validateInputs(allArgsString);
        Map<String, String> params = setDefaults();
        Pattern pattern = Pattern.compile("-\\b[A-Za-z0-9\\-]\\b( \\b[^ ]*\\b)?");
        Matcher matcher = pattern.matcher(allArgsString);
        while (matcher.find()) {
            String[] group = matcher.group().split(" ");
            params.put(group[0], group[1]);
        }
        PortScanner scanner = new PortScanner(params);
        long time = System.currentTimeMillis();
        scanner.scan();
        System.out.println(System.currentTimeMillis() - time);
    }

    public static final String PARAM_ADDRESSES_NAME = "-a";
    public static final String PARAM_PORTS_NAME = "-p";
    public static final String PARAM_TIMEOUT_NAME = "-t";

    private static Map<String, String> setDefaults() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ADDRESSES_NAME, "192.168.1.1-192.168.1.100");
        params.put(PARAM_PORTS_NAME, "1-1000");
        params.put(PARAM_TIMEOUT_NAME, "100");

        return params;
    }

    private static void validateInputs(String allArgsString) throws Exception {
        if (!allArgsString.matches("^( ?-\\b[A-Za-z0-9\\-]\\b( ?\\b[^ ]*\\b)? ?)*$"))
            throw new Exception("Invalid arguments line");
    }
}
