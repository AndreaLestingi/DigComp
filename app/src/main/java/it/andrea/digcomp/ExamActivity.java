package it.andrea.digcomp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

    TextView examName;
    RadioButton option1, option2, option3, option4;
    RadioGroup radioGroup;

    private OkHttpClient client = new OkHttpClient();
    private String correctAnswer = null;
    private String currentArea = null;

    private int currentLevel = 1;
    private int currentQuestion = 1;
    private final int totalLevels = 5;
    private final int questionsPerLevel = 5;

    private Map<String, Integer> areaScores = new HashMap<>();
    private Map<String, Integer> areaQuestionCount = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exam);

        examName = findViewById(R.id.question);
        radioGroup = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        fetchQuestion();
    }

    private void enableAnswerChecking() {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (correctAnswer == null || currentArea == null) return;

            String chosen = null;
            if (checkedId == R.id.option1) chosen = "option1";
            else if (checkedId == R.id.option2) chosen = "option2";
            else if (checkedId == R.id.option3) chosen = "option3";
            else if (checkedId == R.id.option4) chosen = "option4";

            if (chosen != null) {
                areaQuestionCount.put(currentArea, areaQuestionCount.getOrDefault(currentArea, 0) + 1);
                if (chosen.equals(correctAnswer)) {
                    areaScores.put(currentArea, areaScores.getOrDefault(currentArea, 0) + 1);
                }
                radioGroup.clearCheck();
                nextQuestion();
            }
        });
    }

    private void nextQuestion() {
        currentQuestion++;
        if (currentQuestion > questionsPerLevel) {
            currentQuestion = 1;
            currentLevel++;
        }
        if (currentLevel > totalLevels) {
            showResults();
        } else {
            fetchQuestion();
        }
    }

    private void fetchQuestion() {
        Request request = new Request.Builder()
                .url("https://api.lestingi.it/progetto/question/" + currentLevel)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ExamActivity.this, "Errore di connessione: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        Toast.makeText(ExamActivity.this, "Errore nel caricamento della domanda", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);

                        examName.setText(json.optString("question"));
                        option1.setText(json.optString("option1"));
                        option2.setText(json.optString("option2"));
                        option3.setText(json.optString("option3"));
                        option4.setText(json.optString("option4"));

                        correctAnswer = json.optString("correct");
                        currentArea = json.optString("area");

                        enableAnswerChecking();

                    } catch (Exception e) {
                        Snackbar.make(findViewById(R.id.examactivity),
                                "Errore nella risposta del server: " + e.getMessage(),
                                Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showResults() {
        StringBuilder sb = new StringBuilder();
        for (String area : areaQuestionCount.keySet()) {
            int correct = areaScores.getOrDefault(area, 0);
            int total = areaQuestionCount.get(area);
            double percent = 100.0 * correct / total;
            int masteryLevel = 1;
            if (percent >= 80) masteryLevel = 4;
            else if (percent >= 60) masteryLevel = 3;
            else if (percent >= 40) masteryLevel = 2;
            sb.append(area).append(": livello ").append(masteryLevel)
                    .append(" (").append(String.format("%.0f", percent)).append("%)\n");
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

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
                .setContentTitle("DigComp Results")
                .setContentText("Hai completato l'esame! Controlla i dettagli.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(sb.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }

        finish();
    }
}
