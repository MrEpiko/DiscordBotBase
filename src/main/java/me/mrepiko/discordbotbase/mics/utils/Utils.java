package me.mrepiko.discordbotbase.mics.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.mics.requests.HttpRequest;
import me.mrepiko.discordbotbase.mics.requests.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static me.mrepiko.discordbotbase.mics.Constants.*;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final HashMap<String, Boolean> cachedWords = new HashMap<>();

    public static boolean isEnglishWord(String word) {
        word = sanitize(word, true).toLowerCase(Locale.ROOT);

        if (cachedWords.containsKey(word)) return cachedWords.get(word);
        DiscordBot instance = DiscordBot.getInstance();

        try (HttpRequest httpRequest = new HttpRequest(instance.getEndpoints().get("dictionary") + word)) {
            HttpResponse response = httpRequest.sendRequest();
            if (response == null) return false;
            int responseCode = response.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 404) cachedWords.put(word, false);
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        cachedWords.put(word, true);
        return true;
    }

    public static String generateFile(String fileName, StringBuilder output) {
        Path filePath = Paths.get(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write(output.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to write content to file with name {}", fileName, e);
        }
        return filePath.toString();
    }

    public static String getLetterByOrder(int order) {
        if (order == 0) return "A";
        else if (order == 1) return "B";
        else if (order == 2) return "C";
        else return "D";
    }

    public static boolean isAlphabet(String string) {
        return string.matches("[a-zA-Z]+");
    }

    public static String formatToTimeString(int amount, boolean boldValues) {
        if (amount == 0) return (boldValues) ? "**0 seconds**" : "0 seconds";

        int days = amount / (HOURS_IN_DAY * SECONDS_IN_HOUR);
        int hours = (amount % (HOURS_IN_DAY * SECONDS_IN_HOUR)) / SECONDS_IN_HOUR;
        int minutes = ((amount % (HOURS_IN_DAY * SECONDS_IN_HOUR)) % SECONDS_IN_HOUR) / MINUTES_IN_HOUR;
        int seconds = ((amount % (HOURS_IN_DAY * SECONDS_IN_HOUR)) % SECONDS_IN_HOUR) % MINUTES_IN_HOUR;

        if (days == 0) {
            if (hours == 0) {
                if (minutes == 0) {
                    if (seconds == 1) return "1 second";
                    else return String.format((boldValues) ? "**%d seconds**" : "%d seconds", seconds);
                } else {
                    if (seconds == 1)
                        return String.format((boldValues) ? "**%d minutes** and **1 second**" : "%d minutes and 1 second", minutes);
                    else
                        return String.format((boldValues) ? "**%d minutes** and **%d seconds**" : "%d minutes and %d seconds", minutes, seconds);
                }
            } else {
                if (minutes == 0) {
                    if (seconds == 1)
                        return String.format((boldValues) ? "**%d hours** and **1 second**" : "%d hours and 1 second", hours);
                    else
                        return String.format((boldValues) ? "**%d hours** and **%d seconds**" : "%d hours and %d seconds", hours, seconds);
                } else {
                    if (seconds == 1)
                        return String.format((boldValues) ? "**%d hours**, **%d minutes**, and **1 second**" : "%d hours, %d minutes, and 1 second", hours, minutes);
                    else
                        return String.format((boldValues) ? "**%d hours**, **%d minutes** and **%d seconds**" : "%d hours, %d minutes and %d seconds", hours, minutes, seconds);
                }
            }
        } else {
            if (days == 1) {
                if (hours == 0 && minutes == 0 && seconds == 0) return (boldValues) ? "**1 day**" : "1 day";
                else if (hours == 1 && minutes == 0 && seconds == 0)
                    return (boldValues) ? "**1 day** and **1 hour**" : "1 day and 1 hour";
                else if (hours == 0 && minutes == 1 && seconds == 0)
                    return (boldValues) ? "**1 day** and **1 minute**" : "1 day and 1 minute";
                else if (hours == 0 && minutes == 0 && seconds == 1)
                    return (boldValues) ? "**1 day** and **1 second**" : "1 day and 1 second";
                else if (hours == 1 && minutes == 1 && seconds == 0)
                    return (boldValues) ? "**1 day**, **1 hour** and **1 minute**" : "1 day, 1 hour and 1 minute";
                else if (hours == 1 && minutes == 0 && seconds == 1)
                    return (boldValues) ? "**1 day**, **1 hour** and **1 second**" : "1 day, 1 hour and 1 second";
                else if (hours == 0 && minutes == 1 && seconds == 1)
                    return (boldValues) ? "**1 day**, **1 minute** and **1 second**" : "1 day, 1 minute and 1 second";
                else
                    return String.format((boldValues) ? "**1 day**, **%d hours**, **%d minutes** and **%d seconds**" : "1 day, %d hours, %d minutes and %d seconds", hours, minutes, seconds);
            } else {
                if (hours == 0 && minutes == 0 && seconds == 0)
                    return String.format((boldValues) ? "**%d days**" : "%d days", days);
                else if (hours == 1 && minutes == 0 && seconds == 0)
                    return String.format((boldValues) ? "**%d days** and **1 hour**" : "%d days and 1 hour", days);
                else if (hours == 0 && minutes == 1 && seconds == 0)
                    return String.format((boldValues) ? "**%d days** and **1 minute**" : "%d days and 1 minute", days);
                else if (hours == 0 && minutes == 0 && seconds == 1)
                    return String.format((boldValues) ? "**%d days** and **1 second**" : "%d days and 1 second", days);
                else if (hours == 1 && minutes == 1 && seconds == 0)
                    return String.format((boldValues) ? "**%d days**, **1 hour** and **1 minute**" : "%d days, 1 hour and 1 minute", days);
                else if (hours == 1 && minutes == 0 && seconds == 1)
                    return String.format((boldValues) ? "**%d days**, **1 hour** and **1 second**" : "%d days, 1 hour and 1 second", days);
                else if (hours == 0 && minutes == 1 && seconds == 1)
                    return String.format((boldValues) ? "**%d days**, **1 minute** and **1 second**" : "%d days, 1 minute and 1 second", days);
                else
                    return String.format((boldValues) ? "**%d days**, **%d hours**, **%d minutes** and **%d seconds**" : "%d days, %d hours, %d minutes and %d seconds", days, hours, minutes, seconds);
            }
        }
    }

    public static long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static boolean isUrl(String string) {
        try {
            new URL(string);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static boolean isAttachmentUri(String string) {
        return string.startsWith("attachment://");
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

    public static String formatToBars(int current) {
        return formatToBars(current, 100);
    }

    public static String formatToBars(int current, int max) {
        StringBuilder output = new StringBuilder();
        JsonObject emojis = DiscordBot.getInstance().getConstantPlaceholders();
        int PRECISION = max / 5;
        int STEP = max / 50;
        boolean empty = false;
        for (int i = PRECISION; i <= max; i += PRECISION) {
            if (current <= 0) empty = true;
            if (empty) {
                if (i == PRECISION) output.append(emojis.get("bar_e_s").getAsString());
                else if (i == max) output.append(emojis.get("bar_e_e").getAsString());
                else output.append(emojis.get("bar_e_m").getAsString());
                continue;
            }
            if (i == PRECISION) {
                if (current < PRECISION - STEP) output.append(emojis.get("bar_h_s").getAsString());
                else output.append(emojis.get("bar_f_s").getAsString());
            } else if (i == max) {
                if (current < max - (PRECISION / STEP) - STEP) output.append(emojis.get("bar_e_e").getAsString());
                else if (current < max) output.append(emojis.get("bar_h_e").getAsString());
                else output.append(emojis.get("bar_f_e").getAsString());
            } else {
                if (current < i - (PRECISION / STEP) - STEP) {
                    output.append(emojis.get("bar_e_m").getAsString());
                    empty = true;
                } else if (current + STEP >= i) output.append(emojis.get("bar_f_m").getAsString());
                else output.append(emojis.get("bar_h_m").getAsString());
            }
        }
        return output.toString();
    }

    public static int getRandomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static String sanitize(String input) {
        return sanitize(input, false);
    }

    public static String sanitize(String input, boolean alphabetOnly) {
        return (alphabetOnly) ? input.replaceAll("[^a-zA-Z0-9]", "") : input.replaceAll("[^a-zA-Z0-9.,_+\\-*' &-]", "");
    }

    public static <T> void sortHashMapByKey(LinkedHashMap<Integer, T> hashMap) {
        LinkedHashMap<Integer, T> newHashMap = new LinkedHashMap<>();
        List<Integer> sortedKeys = new ArrayList<>(hashMap.keySet());
        Collections.sort(sortedKeys);
        for (Integer i : sortedKeys) {
            newHashMap.put(i, hashMap.get(i));
        }
        hashMap.clear();
        hashMap.putAll(newHashMap);
    }

    public static String unescapeHtml3(final String input) {
        StringWriter writer = null;
        int len = input.length();
        int i = 1;
        int st = 0;
        while (true) {
            while (i < len && input.charAt(i - 1) != '&') i++;
            if (i >= len) break;
            int j = i;
            while (j < len && j < i + MAX_ESCAPE + 1 && input.charAt(j) != ';') j++;
            if (j == len || j < i + MIN_ESCAPE || j == i + MAX_ESCAPE + 1) {
                i++;
                continue;
            }
            if (input.charAt(i) == '#') {
                int k = i + 1;
                int radix = 10;
                final char firstChar = input.charAt(k);
                if (firstChar == 'x' || firstChar == 'X') {
                    k++;
                    radix = 16;
                }
                try {
                    int entityValue = Integer.parseInt(input.substring(k, j), radix);
                    if (writer == null) writer = new StringWriter(input.length());
                    writer.append(input.substring(st, i - 1));
                    if (entityValue > 0xFFFF) {
                        final char[] chrs = Character.toChars(entityValue);
                        writer.write(chrs[0]);
                        writer.write(chrs[1]);
                    } else writer.write(entityValue);
                } catch (NumberFormatException ex) {
                    i++;
                    continue;
                }
            } else {
                CharSequence value = lookupMap.get(input.substring(i, j));
                if (value == null) {
                    i++;
                    continue;
                }
                if (writer == null) writer = new StringWriter(input.length());
                writer.append(input.substring(st, i - 1));
                writer.append(value);
            }
            st = j + 1;
            i = st;
        }
        if (writer != null) {
            writer.append(input.substring(st, len));
            return writer.toString();
        }
        return input;
    }

    private static final String[][] ESCAPES = {
            {"\"", "quot"}, // " - double-quote
            {"&", "amp"}, // & - ampersand
            {"<", "lt"}, // < - less-than
            {">", "gt"}, // > - greater-than

            // Mapping to escape ISO-8859-1 characters to their named HTML 3.x equivalents.
            {"\u00A0", "nbsp"},   // Non-breaking space
            {"\u00A1", "iexcl"},  // Inverted exclamation mark
            {"\u00A2", "cent"},   // Cent sign
            {"\u00A3", "pound"},  // Pound sign
            {"\u00A4", "curren"}, // Currency sign
            {"\u00A5", "yen"},    // Yen sign = yuan sign
            {"\u00A6", "brvbar"}, // Broken bar = broken vertical bar
            {"\u00A7", "sect"},   // Section sign
            {"\u00A8", "uml"},    // Diaeresis = spacing diaeresis
            {"\u00A9", "copy"},   // © - copyright sign
            {"\u00AA", "ordf"},   // Feminine ordinal indicator
            {"\u00AB", "laquo"},  // Left-pointing double angle quotation mark = left pointing guillemet
            {"\u00AC", "not"},    // Not sign
            {"\u00AD", "shy"},    // Soft hyphen = discretionary hyphen
            {"\u00AE", "reg"},    // ® - registered trademark sign
            {"\u00AF", "macr"},   // Macron = spacing macron = overline = APL overbar
            {"\u00B0", "deg"},    // Degree sign
            {"\u00B1", "plusmn"}, // Plus-minus sign = plus-or-minus sign
            {"\u00B2", "sup2"},   // Superscript two = superscript digit two = squared
            {"\u00B3", "sup3"},   // Superscript three = superscript digit three = cubed
            {"\u00B4", "acute"},  // Acute accent = spacing acute
            {"\u00B5", "micro"},  // Micro sign
            {"\u00B6", "para"},   // Pilcrow sign = paragraph sign
            {"\u00B7", "middot"}, // Middle dot = Georgian comma = Greek middle dot
            {"\u00B8", "cedil"},  // Cedilla = spacing cedilla
            {"\u00B9", "sup1"},   // Superscript one = superscript digit one
            {"\u00BA", "ordm"},   // Masculine ordinal indicator
            {"\u00BB", "raquo"},  // Right-pointing double angle quotation mark = right pointing guillemet
            {"\u00BC", "frac14"}, // Vulgar fraction one quarter = fraction one quarter
            {"\u00BD", "frac12"}, // Vulgar fraction one half = fraction one half
            {"\u00BE", "frac34"}, // Vulgar fraction three quarters = fraction three quarters
            {"\u00BF", "iquest"}, // Inverted question mark = turned question mark
            {"\u00C0", "Agrave"}, // А - uppercase A, grave accent
            {"\u00C1", "Aacute"}, // Б - uppercase A, acute accent
            {"\u00C2", "Acirc"},  // В - uppercase A, circumflex accent
            {"\u00C3", "Atilde"}, // Г - uppercase A, tilde
            {"\u00C4", "Auml"},   // Д - uppercase A, umlaut
            {"\u00C5", "Aring"},  // Е - uppercase A, ring
            {"\u00C6", "AElig"},  // Ж - uppercase AE
            {"\u00C7", "Ccedil"}, // З - uppercase C, cedilla
            {"\u00C8", "Egrave"}, // И - uppercase E, grave accent
            {"\u00C9", "Eacute"}, // Й - uppercase E, acute accent
            {"\u00CA", "Ecirc"},  // К - uppercase E, circumflex accent
            {"\u00CB", "Euml"},   // Л - uppercase E, umlaut
            {"\u00CC", "Igrave"}, // М - uppercase I, grave accent
            {"\u00CD", "Iacute"}, // Н - uppercase I, acute accent
            {"\u00CE", "Icirc"},  // О - uppercase I, circumflex accent
            {"\u00CF", "Iuml"},   // П - uppercase I, umlaut
            {"\u00D0", "ETH"},    // Р - uppercase Eth, Icelandic
            {"\u00D1", "Ntilde"}, // С - uppercase N, tilde
            {"\u00D2", "Ograve"}, // Т - uppercase O, grave accent
            {"\u00D3", "Oacute"}, // У - uppercase O, acute accent
            {"\u00D4", "Ocirc"},  // Ф - uppercase O, circumflex accent
            {"\u00D5", "Otilde"}, // Х - uppercase O, tilde
            {"\u00D6", "Ouml"},   // Ц - uppercase O, umlaut
            {"\u00D7", "times"},  // Multiplication sign
            {"\u00D8", "Oslash"}, // Ш - uppercase O, slash
            {"\u00D9", "Ugrave"}, // Щ - uppercase U, grave accent
            {"\u00DA", "Uacute"}, // Ъ - uppercase U, acute accent
            {"\u00DB", "Ucirc"},  // Ы - uppercase U, circumflex accent
            {"\u00DC", "Uuml"},   // Ь - uppercase U, umlaut
            {"\u00DD", "Yacute"}, // Э - uppercase Y, acute accent
            {"\u00DE", "THORN"},  // Ю - uppercase THORN, Icelandic
            {"\u00DF", "szlig"},  // Я - lowercase sharps, German
            {"\u00E0", "agrave"}, // а - lowercase a, grave accent
            {"\u00E1", "aacute"}, // б - lowercase a, acute accent
            {"\u00E2", "acirc"},  // в - lowercase a, circumflex accent
            {"\u00E3", "atilde"}, // г - lowercase a, tilde
            {"\u00E4", "auml"},   // д - lowercase a, umlaut
            {"\u00E5", "aring"},  // е - lowercase a, ring
            {"\u00E6", "aelig"},  // ж - lowercase ae
            {"\u00E7", "ccedil"}, // з - lowercase c, cedilla
            {"\u00E8", "egrave"}, // и - lowercase e, grave accent
            {"\u00E9", "eacute"}, // й - lowercase e, acute accent
            {"\u00EA", "ecirc"},  // к - lowercase e, circumflex accent
            {"\u00EB", "euml"},   // л - lowercase e, umlaut
            {"\u00EC", "igrave"}, // м - lowercase i, grave accent
            {"\u00ED", "iacute"}, // н - lowercase i, acute accent
            {"\u00EE", "icirc"},  // о - lowercase i, circumflex accent
            {"\u00EF", "iuml"},   // п - lowercase i, umlaut
            {"\u00F0", "eth"},    // р - lowercase eth, Icelandic
            {"\u00F1", "ntilde"}, // с - lowercase n, tilde
            {"\u00F2", "ograve"}, // т - lowercase o, grave accent
            {"\u00F3", "oacute"}, // у - lowercase o, acute accent
            {"\u00F4", "ocirc"},  // ф - lowercase o, circumflex accent
            {"\u00F5", "otilde"}, // х - lowercase o, tilde
            {"\u00F6", "ouml"},   // ц - lowercase o, umlaut
            {"\u00F7", "divide"}, // Division sign
            {"\u00F8", "oslash"}, // ш - lowercase o, slash
            {"\u00F9", "ugrave"}, // щ - lowercase u, grave accent
            {"\u00FA", "uacute"}, // ъ - lowercase u, acute accent
            {"\u00FB", "ucirc"},  // ы - lowercase u, circumflex accent
            {"\u00FC", "uuml"},   // ь - lowercase u, umlaut
            {"\u00FD", "yacute"}, // э - lowercase y, acute accent
            {"\u00FE", "thorn"},  // ю - lowercase thorn, Icelandic
            {"\u00FF", "yuml"},   // я - lowercase y, umlaut
    };

    private static final int MIN_ESCAPE = 2;
    private static final int MAX_ESCAPE = 6;

    private static final HashMap<String, CharSequence> lookupMap;

    static {
        lookupMap = new HashMap<String, CharSequence>();
        for (final CharSequence[] seq : ESCAPES)
            lookupMap.put(seq[1].toString(), seq[0]);
    }

    public static String uploadAndGet(String content) {
        DiscordBot instance = DiscordBot.getInstance();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Authorization", instance.getEndpointAuths().get("hastebin"));
        try (HttpRequest request = new HttpRequest(instance.getEndpoints().get("hastebin"), headers, content)) {
            HttpResponse response = request.sendRequest();
            if (response.getContent() == null || response.getResponseCode() != 200) return "";
            return new Gson().fromJson(response.getContent(), JsonObject.class).get("key").getAsString();
        } catch (Exception e) {
            LOGGER.error("Failed to upload content to Hastebin", e);
        }
        return "";
    }

}
