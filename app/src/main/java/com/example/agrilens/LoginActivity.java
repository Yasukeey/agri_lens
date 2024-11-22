package com.example.agrilens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    EditText email, password;
    TextView signUp;

    FirebaseAuth auth;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        signIn = findViewById(R.id.login_btn);
        email = findViewById(R.id.email_login);
        password = findViewById(R.id.password_login);
        signUp = findViewById(R.id.sign_up);

        signUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));

        signIn.setOnClickListener(v -> {
            loginUser();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void loginUser() {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, getString(R.string.email_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            Toast.makeText(this, getString(R.string.password_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.length() < 6) {
            Toast.makeText(this, getString(R.string.password_length_error), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(LoginActivity.this, getString(R.string.logged_in_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed) + ": " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
