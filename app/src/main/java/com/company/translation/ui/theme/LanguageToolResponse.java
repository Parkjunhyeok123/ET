package com.company.translation.ui.theme;

import java.util.List;

public class LanguageToolResponse {
    private List<Matches> matches;

    public List<Matches> getMatches() {
        return matches;
    }

    public static class Matches {
        private String message;
        private int offset;
        private int length;
        private String replacement;

        public String getMessage() {
            return message;
        }

        public String getReplacement() {
            return replacement;
        }
    }
}
