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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agrilens.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    Button signUp;
    EditText name, email, password;
    TextView signIn;

    FirebaseAuth auth;
    FirebaseDatabase database;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Authentication and Database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        signUp = findViewById(R.id.login_btn);
        name = findViewById(R.id.name_reg);
        email = findViewById(R.id.email_reg);
        password = findViewById(R.id.password_reg);
        signIn = findViewById(R.id.sign_in);

        // Set onClick listeners
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
                progressBar.setVisibility(View.VISIBLE);  // Show progress bar while registering
            }
        });
    }

    private void createUser() {
        String userName = name.getText().toString();
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        // Validation checks for empty fields
        if (TextUtils.isEmpty(userName)){
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userEmail)){
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userPassword)){
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.length() < 6){
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Create UserModel and save to Firebase Realtime Database
                            UserModel userModel = new UserModel(userName, userEmail, userPassword);
                            String userId = task.getResult().getUser().getUid();
                            database.getReference().child("Users").child(userId).setValue(userModel);

                            progressBar.setVisibility(View.GONE);  // Hide progress bar after registration

                            Toast.makeText(RegistrationActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class)); // Redirect to login page
                            finish();  // Close RegistrationActivity
                        } else {
                            progressBar.setVisibility(View.GONE);  // Hide progress bar if registration fails
                            Toast.makeText(RegistrationActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
