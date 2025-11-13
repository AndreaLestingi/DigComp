package it.andrea.digcomp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences("sessions", MODE_PRIVATE);

        new Thread(() -> {

            //hashedPassword = auth.hash(passwordText.toCharArray());

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
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                String username = json.optString("username", null);
                                TextView benvenuto = findViewById(R.id.username);
                                benvenuto.setText("Bentornato " + username);
                            } catch (Exception e) {
                                Snackbar.make(findViewById(R.id.HomeActivity), "Errore nella risposta del server: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        } else {

                        }
                    });
                }
            });
        }).start();
    }
}
