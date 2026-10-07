package uz.agrobank.stopcredit.repository;

import java.util.Locale;

final class LikePattern {

    static final char ESCAPE = '\\';

    private LikePattern() {
    }

    // User input is matched literally: '%' and '_' typed into a search box must not act as wildcards
    static String contains(String text) {
        return "%" + escape(text) + "%";
    }

    static String startsWith(String text) {
        return escape(text) + "%";
    }

    static String containsIgnoreCase(String text) {
        return contains(text.toLowerCase(Locale.ROOT));
    }

    private static String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c == '%' || c == '_' || c == ESCAPE) {
                escaped.append(ESCAPE);
            }
            escaped.append(c);
        }
        return escaped.toString();
    }
}
