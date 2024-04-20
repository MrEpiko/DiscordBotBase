package me.mrepiko.discordbotbase.discord.mics.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {

    public static String formatToTimeString(int amount, boolean boldValues) {
        if (amount == 0) return (boldValues) ? "**0 seconds**" : "0 seconds";

        int days = amount / (24 * 3600);
        int hours = (amount % (24 * 3600)) / 3600;
        int minutes = ((amount % (24 * 3600)) % 3600) / 60;
        int seconds = ((amount % (24 * 3600)) % 3600) % 60;

        if (days == 0) {
            if (hours == 0) {
                if (minutes == 0) {
                    if (seconds == 1) return "1 second";
                    else return String.format((boldValues) ? "**%d seconds**" : "%d seconds", seconds);
                } else {
                    if (seconds == 1) return String.format((boldValues) ? "**%d minutes** and **1 second**" : "%d minutes and 1 second", minutes);
                    else return String.format((boldValues) ? "**%d minutes** and **%d seconds**" : "%d minutes and %d seconds", minutes, seconds);
                }
            } else {
                if (minutes == 0) {
                    if (seconds == 1) return String.format((boldValues) ? "**%d hours** and **1 second**" : "%d hours and 1 second", hours);
                    else return String.format((boldValues) ? "**%d hours** and **%d seconds**" : "%d hours and %d seconds", hours, seconds);
                } else {
                    if (seconds == 1) return String.format((boldValues) ? "**%d hours**, **%d minutes**, and **1 second**" : "%d hours, %d minutes, and 1 second", hours, minutes);
                    else return String.format((boldValues) ? "**%d hours**, **%d minutes** and **%d seconds**" : "%d hours, %d minutes and %d seconds", hours, minutes, seconds);
                }
            }
        } else {
            if (days == 1) {
                if (hours == 0 && minutes == 0 && seconds == 0) return (boldValues) ? "**1 day**" : "1 day";
                else if (hours == 1 && minutes == 0 && seconds == 0) return (boldValues) ? "**1 day** and **1 hour**" : "1 day and 1 hour";
                else if (hours == 0 && minutes == 1 && seconds == 0) return (boldValues) ? "**1 day** and **1 minute**" : "1 day and 1 minute";
                else if (hours == 0 && minutes == 0 && seconds == 1) return (boldValues) ? "**1 day** and **1 second**" : "1 day and 1 second";
                else if (hours == 1 && minutes == 1 && seconds == 0) return (boldValues) ? "**1 day**, **1 hour** and **1 minute**" : "1 day, 1 hour and 1 minute";
                else if (hours == 1 && minutes == 0 && seconds == 1) return (boldValues) ? "**1 day**, **1 hour** and **1 second**" : "1 day, 1 hour and 1 second";
                else if (hours == 0 && minutes == 1 && seconds == 1) return (boldValues) ? "**1 day**, **1 minute** and **1 second**" : "1 day, 1 minute and 1 second";
                else return String.format((boldValues) ? "**1 day**, **%d hours**, **%d minutes** and **%d seconds**" : "1 day, %d hours, %d minutes and %d seconds", hours, minutes, seconds);
            } else {
                if (hours == 0 && minutes == 0 && seconds == 0) return String.format((boldValues) ? "**%d days**" : "%d days", days);
                else if (hours == 1 && minutes == 0 && seconds == 0) return String.format((boldValues) ? "**%d days** and **1 hour**" : "%d days and 1 hour", days);
                else if (hours == 0 && minutes == 1 && seconds == 0) return String.format((boldValues) ? "**%d days** and **1 minute**" : "%d days and 1 minute", days);
                else if (hours == 0 && minutes == 0 && seconds == 1) return String.format((boldValues) ? "**%d days** and **1 second**" : "%d days and 1 second", days);
                else if (hours == 1 && minutes == 1 && seconds == 0) return String.format((boldValues) ? "**%d days**, **1 hour** and **1 minute**" : "%d days, 1 hour and 1 minute", days);
                else if (hours == 1 && minutes == 0 && seconds == 1) return String.format((boldValues) ? "**%d days**, **1 hour** and **1 second**" : "%d days, 1 hour and 1 second", days);
                else if (hours == 0 && minutes == 1 && seconds == 1) return String.format((boldValues) ? "**%d days**, **1 minute** and **1 second**" : "%d days, 1 minute and 1 second", days);
                else return String.format((boldValues) ? "**%d days**, **%d hours**, **%d minutes** and **%d seconds**" : "%d days, %d hours, %d minutes and %d seconds", days, hours, minutes, seconds);
            }
        }
    }

    public static boolean isUrl(String string) {
        try {
            new URL(string);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static long convertToSeconds(String input) {
        int totalSeconds = 0;

        for (String part : input.split(" ")) {
            char unit = part.charAt(part.length() - 1);
            int value;
            try {
                value = Integer.parseInt(part.substring(0, part.length() - 1));
            } catch (NumberFormatException e) {
                continue;
            }
            switch (unit) {
                case 'd' -> totalSeconds += value * 24 * 60 * 60;
                case 'h' -> totalSeconds += value * 60 * 60;
                case 'm' -> totalSeconds += value * 60;
                case 's' -> totalSeconds += value;
            }
        }
        return totalSeconds;
    }

    public static String capitalizeEveryWord(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static String adaptEmoji(String rawEmoji) {
        return rawEmoji.replace("<", "").replace(">", "");
    }

}
