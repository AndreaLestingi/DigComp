package it.andrea.digcomp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText email, password;
    private TextView errorMessage;
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences sharedPreferences;
    private String hashedPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("sessions", MODE_PRIVATE);


        Intent successIntent = new Intent(MainActivity.this, HomeActivity.class);
        String session = sharedPreferences.getString("sessionid", null);
        if(session != null) {
            startActivity(successIntent);
        }

        errorMessage = findViewById(R.id.errorMsg);
        loginButton = findViewById(R.id.accedibtn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.passwd);

        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email e Password obbligatorie", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {

                //hashedPassword = auth.hash(passwordText.toCharArray());

                RequestBody requestBody = new FormBody.Builder()
                        .add("email", emailText)
                        .add("password", passwordText)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.lestingi.it/progetto/login")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Errore di connessione: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string(); // Leggi la risposta come stringa

                        runOnUiThread(() -> {
                            try {
                                if (response.isSuccessful()) {
                                    JSONObject json = new JSONObject(responseBody);
                                    String sessionId = json.optString("session", null);

                                    if (sessionId != null) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("sessionid", sessionId);
                                        editor.apply();

                                        errorMessage.setVisibility(View.INVISIBLE);


                                        startActivity(successIntent);
                                        finish();
                                    } else {
                                        Snackbar.make(v, "Risposta senza session ID", Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    errorMessage.setVisibility(View.VISIBLE);
                                    Snackbar.make(v, "Login fallito (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Snackbar.make(v, "Errore nella risposta del server: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }

                });
            }).start();
        });
    }
}
