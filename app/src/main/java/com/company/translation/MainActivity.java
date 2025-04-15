package com.company.translation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.company.translation.ui.theme.LanguageToolResponse;
import com.company.translation.ui.theme.LanguageToolService;
import com.company.translation.ui.theme.RetrofitClient;
import com.company.translation.ui.theme.TranslationResponse;
import com.company.translation.ui.theme.TranslationService;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {

    private SpeechRecognizer speechRecognizer;
    private TextView originalText;
    private TextView translatedText;
    private ImageView micIcon;
    private Spinner languageSpinner;
    private Button resetButton;
    private TextToSpeech textToSpeech;

    private static final String API_KEY = "AIzaSyBVPGlGLGFezBg9dSncH-LodATWdUQtWas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI 요소 연결
        originalText = findViewById(R.id.originalText);
        translatedText = findViewById(R.id.translatedText);
        micIcon = findViewById(R.id.micIcon);
        languageSpinner = findViewById(R.id.languageSpinner);
        resetButton = findViewById(R.id.resetButton);

        // ✅ Spinner 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"영어", "한국어", "일본어", "중국어", "스페인어", "독일어"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // ✅ Spinner 선택 리스너
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "선택한 언어: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🔊 TTS 초기화
        textToSpeech = new TextToSpeech(this, status -> {
            try {
                if (status == TextToSpeech.SUCCESS) {
                    int langResult = textToSpeech.setLanguage(Locale.US);
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "TTS 언어 지원되지 않음", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "TTS 초기화 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 🎙️ 음성 인식 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {
                micIcon.setImageResource(R.drawable.ic_mic_on); // 마이크 ON 아이콘
            }
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                micIcon.setImageResource(R.drawable.ic_mic_off); // 마이크 OFF 아이콘
            }
            @Override public void onError(int error) {
                Toast.makeText(MainActivity.this, "음성 인식 오류: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                try {
                    List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        originalText.setText(recognizedText);
                        checkGrammarAndSuggest(recognizedText);
                        translateText(recognizedText);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "음성 인식 결과 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        // 🎙️ 마이크 클릭 시 음성 인식 시작
        micIcon.setOnClickListener(v -> startVoiceRecognition());

        // 🔄 초기화 버튼
        resetButton.setOnClickListener(v -> {
            originalText.setText("");
            translatedText.setText("");
        });
    }

    // 🎙️ 음성 인식 시작
    private void startVoiceRecognition() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            Toast.makeText(this, "음성 인식 시작 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 🌍 번역 기능 호출
    private void translateText(String text) {
        try {
            String selectedLanguage = languageSpinner.getSelectedItem().toString();
            String targetLanguage = getTargetLanguageCode(selectedLanguage);

            TranslationService service = RetrofitClient.getClient().create(TranslationService.class);
            Call<TranslationResponse> call = service.translateText(text, targetLanguage, API_KEY);

            call.enqueue(new Callback<TranslationResponse>() {
                @Override
                public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String translated = response.body().getData().getTranslations()[0].getTranslatedText();
                        translatedText.setText(translated);
                    } else {
                        translatedText.setText("번역 실패: 응답 오류");
                    }
                }

                @Override
                public void onFailure(Call<TranslationResponse> call, Throwable t) {
                    translatedText.setText("번역 실패: 네트워크 오류");
                    Toast.makeText(MainActivity.this, "번역 요청 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "번역 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 🌐 언어명 → 코드 변환
    private String getTargetLanguageCode(String language) {
        switch (language) {
            case "영어": return "en";
            case "한국어": return "ko";
            case "일본어": return "ja";
            case "중국어": return "zh";
            case "스페인어": return "es";
            case "독일어": return "de";
            default: return "en";
        }
    }

    // 🧹 리소스 해제
    @Override
    protected void onDestroy() {
        try {
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
            if (speechRecognizer != null) {
                speechRecognizer.destroy();
            }
        } catch (Exception e) {
            Toast.makeText(this, "리소스 해제 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
    }

    // 📝 문법 교정 및 제안
    private void checkGrammarAndSuggest(String text) {
        try {
            String language = "ko"; // 한국어 기준 교정
            LanguageToolService service = RetrofitClient.getClient().create(LanguageToolService.class);
            Call<LanguageToolResponse> call = service.checkGrammar(text, language);

            call.enqueue(new Callback<LanguageToolResponse>() {
                @Override
                public void onResponse(Call<LanguageToolResponse> call, Response<LanguageToolResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<LanguageToolResponse.Matches> matches = response.body().getMatches();
                        String correctedText = text;
                        for (LanguageToolResponse.Matches match : matches) {
                            correctedText = correctedText.replace(match.getReplacement(), match.getReplacement());
                        }
                        originalText.setText(correctedText);
                    }
                }

                @Override
                public void onFailure(Call<LanguageToolResponse> call, Throwable t) {
                    originalText.setText("문법 교정 실패");
                    Toast.makeText(MainActivity.this, "문법 교정 요청 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "문법 교정 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
