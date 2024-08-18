package me.lukiiy.discordBridge.util;

public class GenericHelper {
    public static String cleanFormat(String txt) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) if (txt.charAt(i) == '§') i++; else s.append(txt.charAt(i));
        return s.toString();
    }
}
