package it.andrea.digcomp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

    TextView examName, levelIndicator, questionCounter;
    RadioButton option1, option2, option3, option4;
    RadioGroup radioGroup;
    Button submitButton;

    private OkHttpClient client = new OkHttpClient();
    private String correctAnswer = null;
    private String currentQuestionId = null;
    private String examSessionId = null;

    private int currentQuestionNumber = 1;

    private SharedPreferences sharedPreferences;
    private String sessionId = "";

    private static final String BASE_URL = "https://api.lestingi.it/progetto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exam);

        sharedPreferences = getSharedPreferences("sessions", MODE_PRIVATE);
        sessionId = sharedPreferences.getString("sessionid", "");

        examName = findViewById(R.id.question);
        levelIndicator = findViewById(R.id.levelIndicator);
        questionCounter = findViewById(R.id.questionCounter);
        radioGroup = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        submitButton = findViewById(R.id.submitButton);

        if (sessionId.isEmpty()) {
            Toast.makeText(this, "Sessione non valida. Effettua il login.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        submitButton.setOnClickListener(v -> submitAnswer());

        startExam();
    }

    private void startExam() {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/exam/start")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ExamActivity.this, "Errore di connessione: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        Toast.makeText(ExamActivity.this, "Errore nell'avvio dell'esame", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        examSessionId = json.getString("examSessionId");
                        JSONObject question = json.getJSONObject("question");

                        displayQuestion(question);
                        currentQuestionNumber = 1;
                        updateQuestionCounter();

                    } catch (Exception e) {
                        Toast.makeText(ExamActivity.this, "Errore nel parsing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void displayQuestion(JSONObject questionObj) {
        try {
            currentQuestionId = questionObj.getString("questionId");
            String domanda = questionObj.getString("domanda");
            int level = questionObj.getInt("level");
            String area = questionObj.getString("area");

            JSONArray risposte = questionObj.getJSONArray("risposte");

            examName.setText(domanda);
            levelIndicator.setText("Livello " + level + " - " + area);

            if (risposte.length() >= 4) {
                option1.setText(risposte.getString(0));
                option2.setText(risposte.getString(1));
                option3.setText(risposte.getString(2));
                option4.setText(risposte.getString(3));
            }

            radioGroup.clearCheck();
            submitButton.setEnabled(true);

        } catch (Exception e) {
            Toast.makeText(this, "Errore nel caricamento della domanda", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitAnswer() {
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Seleziona una risposta", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedAnswer = "";
        RadioButton selectedRadio = findViewById(selectedId);
        selectedAnswer = selectedRadio.getText().toString();

        submitButton.setEnabled(false);

        sendAnswer(selectedAnswer);
    }

    private void sendAnswer(String answer) {
        JSONObject json = new JSONObject();
        try {
            json.put("examSessionId", examSessionId);
            json.put("questionId", currentQuestionId);
            json.put("answer", answer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/exam/nextquestion")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ExamActivity.this, "Errore di connessione: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    submitButton.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        Toast.makeText(ExamActivity.this, "Errore nell'invio della risposta", Toast.LENGTH_SHORT).show();
                        submitButton.setEnabled(true);
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        boolean finished = json.getBoolean("finished");

                        if (finished) {
                            endExam();
                        } else {

                            JSONObject nextQuestion = json.getJSONObject("question");
                            displayQuestion(nextQuestion);
                            currentQuestionNumber++;
                            updateQuestionCounter();
                        }

                    } catch (Exception e) {
                        Toast.makeText(ExamActivity.this, "Errore nel parsing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        submitButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void updateQuestionCounter() {
        questionCounter.setText("Domanda " + currentQuestionNumber);
    }

    private void endExam() {
        JSONObject json = new JSONObject();
        try {
            json.put("examSessionId", examSessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/exam/end")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ExamActivity.this, "Errore nel completamento dell'esame", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        Toast.makeText(ExamActivity.this, "Errore nel completamento dell'esame", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject results = json.getJSONObject("results");

                        int totalCorrect = results.getInt("totalCorrect");
                        int totalQuestions = results.getInt("totalQuestions");
                        int percentage = results.getInt("overallPercentage");
                        int finalLevel = results.getInt("finalMasteryLevel");

                        JSONObject areaResults = results.getJSONObject("areaResults");

                        StringBuilder sb = new StringBuilder();
                        sb.append("RISULTATI ESAME\n\n");
                        sb.append("Totale: ").append(totalCorrect).append("/").append(totalQuestions)
                                .append(" (").append(percentage).append("%)\n");
                        sb.append("Livello finale: ").append(finalLevel).append("\n\n");

                        sb.append("Risultati per area:\n");
                        String[] areas = {"Information", "Communication", "ContentCreation", "Safety", "ProblemSolving"};
                        String[] areaNames = {"Informazione", "Comunicazione", "Creazione Contenuti", "Sicurezza", "Risoluzione Problemi"};

                        for (int i = 0; i < areas.length; i++) {
                            if (areaResults.has(areas[i])) {
                                JSONObject areaData = areaResults.getJSONObject(areas[i]);
                                int areaPerc = areaData.getInt("percentage");
                                int areaLevel = areaData.getInt("masteryLevel");
                                sb.append(areaNames[i]).append(": Livello ").append(areaLevel)
                                        .append(" (").append(areaPerc).append("%)\n");
                            }
                        }

                        String resultsText = sb.toString();

                        saveResultsLocally(finalLevel);

                        showNotification(resultsText, finalLevel);

                        //Toast.makeText(ExamActivity.this, "Esame completato!\nLivello: " + finalLevel, Toast.LENGTH_LONG).show();

                        finish();

                    } catch (Exception e) {
                        Toast.makeText(ExamActivity.this, "Errore nel parsing dei risultati: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void saveResultsLocally(int finalLevel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_exam_level", finalLevel);
        editor.putLong("last_exam_timestamp", System.currentTimeMillis());
        editor.apply();
    }

    private void showNotification(String resultsText, int finalLevel) {
        String channelId = "digcomp_channel";
        String channelName = "DigComp Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("DigComp Results");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Esame DigComp Completato")
                .setContentText("Hai raggiunto il livello " + finalLevel)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(resultsText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}