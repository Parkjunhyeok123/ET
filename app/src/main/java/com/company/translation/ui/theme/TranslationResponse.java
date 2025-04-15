package com.company.translation.ui.theme;

public class TranslationResponse {
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        private Translation[] translations;

        public Translation[] getTranslations() {
            return translations;
        }
    }

    public static class Translation {
        private String translatedText;

        public String getTranslatedText() {
            return translatedText;
        }
    }
}
