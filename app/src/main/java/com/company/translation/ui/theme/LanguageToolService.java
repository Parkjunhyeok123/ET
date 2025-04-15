package com.company.translation.ui.theme;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LanguageToolService {
    @GET("v2/check")
    Call<com.company.translation.ui.theme.LanguageToolResponse> checkGrammar(@Query("text") String text, @Query("language") String language);
}
