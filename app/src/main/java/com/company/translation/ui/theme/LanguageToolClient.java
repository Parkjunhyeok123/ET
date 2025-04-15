package com.company.translation.ui.theme;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LanguageToolClient {
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.languagetool.org/") // LanguageTool API 기본 URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
