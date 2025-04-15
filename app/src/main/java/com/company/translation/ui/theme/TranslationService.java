package com.company.translation.ui.theme;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslationService {

    @GET("language/translate/v2")
    Call<TranslationResponse> translateText(
            @Query("q") String text,
            @Query("target") String targetLang,
            @Query("key") String apiKey
    );
}
