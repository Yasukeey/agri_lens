package com.example.agrilens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    TextView aboutContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        aboutContent = findViewById(R.id.about_content);

        // Set the content for the About page
        aboutContent.setText("AgriLens\n\n" +
                "Version: 1.0\n\n" +
                "AgriLens is a plant disease identification app designed to help farmers and gardeners " +
                "quickly identify plant diseases and learn about effective treatments and preventive measures. " +
                "Simply upload or take a picture of the affected plant, and AgriLens will provide relevant information " +
                "on the disease and suggestions on how to care for your plants.");
    }
}
