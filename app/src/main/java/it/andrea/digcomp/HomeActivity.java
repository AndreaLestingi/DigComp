package it.andrea.digcomp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();
    SharedPreferences sharedPreferences;

    private LinearLayout historyContainer;
    private Button examBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences("sessions", MODE_PRIVATE);
        historyContainer = findViewById(R.id.history_container);
        examBtn = findViewById(R.id.btn_esame);

        loadUserInfo();
        loadHistory();

        examBtn.setOnClickListener(v -> {
            Intent examIntent = new Intent(HomeActivity.this, ExamActivity.class);
            startActivity(examIntent);
        });

    }

    private void loadUserInfo() {
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url("https://api.lestingi.it/progetto/info/" + sharedPreferences.getString("sessionid", ""))
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Errore di connessione: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseBody);
                                String username = json.optString("username", "Utente");
                                TextView benvenuto = findViewById(R.id.username);
                                benvenuto.setText("Bentornato " + username);
                            } catch (Exception e) {
                                Snackbar.make(findViewById(R.id.HomeActivity), "Errore nella risposta del server: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Errore nel caricamento delle informazioni utente", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }).start();
    }

    private void loadHistory() {
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url("https://api.lestingi.it/progetto/history/" + sharedPreferences.getString("sessionid", ""))
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Errore di connessione: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseBody);

                                if (json.getBoolean("success")) {
                                    JSONArray history = json.getJSONArray("history");
                                    displayHistory(history);
                                } else {
                                    Toast.makeText(HomeActivity.this, "Nessuna cronologia disponibile", Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Snackbar.make(findViewById(R.id.HomeActivity), "Errore nella risposta del server: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Errore nel caricamento della cronologia", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }).start();
    }

    private void displayHistory(JSONArray history) {
        try {
            historyContainer.removeAllViews();
            
            if (history.length() == 0) {
                createHistoryCard("Nessuna Attività", "Non hai ancora completato lezioni o esami", R.mipmap.ic_launcher_foreground);
                return;
            }

            for (int i = 0; i < history.length(); i++) {
                JSONObject item = history.getJSONObject(i);
                String type = item.getString("type");
                String timestamp = item.getString("timestamp");
                String formattedDate = formatTimestamp(timestamp);

                String title, description;
                if (type.equals("exam")) {
                    int level = item.getInt("level");
                    title = "Esito Esame";
                    description = "Hai superato l'esame col livello " + level + "\n" + formattedDate;
                } else if (type.equals("lesson")) {
                    int lezione = item.getInt("lezione");
                    title = "Esito Lezione";
                    description = "Hai completato la lezione " + lezione + "\n" + formattedDate;
                } else {
                    continue;
                }

                createHistoryCard(title, description, R.mipmap.ic_launcher_foreground);
            }

            findViewById(R.id.HomeActivity).requestLayout();

        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.HomeActivity), "Errore nel visualizzare la cronologia: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void createHistoryCard(String title, String description, int imageResource) {
        LayoutInflater inflater = LayoutInflater.from(this);
        
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(15));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(20));
        cardView.setCardElevation(dpToPx(10));
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setPadding(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15));
        mainLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        mainLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                dpToPx(80), dpToPx(80)
        );
        imageParams.setMarginEnd(dpToPx(15));
        imageView.setLayoutParams(imageParams);
        imageView.setImageResource(imageResource);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setClipToOutline(true);
        
        LinearLayout textLayout = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        textLayout.setLayoutParams(textParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        
        TextView titleView = new TextView(this);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        titleView.setText(title);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextSize(20);
        
        TextView descriptionView = new TextView(this);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.setMargins(0, dpToPx(5), 0, 0);
        descriptionView.setLayoutParams(descParams);
        descriptionView.setText(description);
        descriptionView.setTextColor(getResources().getColor(android.R.color.black));
        descriptionView.setTextSize(16);
        
        textLayout.addView(titleView);
        textLayout.addView(descriptionView);
        mainLayout.addView(imageView);
        mainLayout.addView(textLayout);
        cardView.addView(mainLayout);
        
        historyContainer.addView(cardView);
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }
}