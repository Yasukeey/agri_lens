package com.example.agrilens;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.agrilens.AboutActivity;
import com.example.agrilens.MainActivity;
import com.example.agrilens.ProfileActivity;
import com.example.agrilens.R;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button proceedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        proceedButton = findViewById(R.id.proceedButton);

        // Set up toolbar for navigation drawer
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Open navigation drawer when the toolbar's menu icon is clicked
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Handle Profile click
                if (id == R.id.nav_profile) {
                    // Navigate to Profile Activity (or Fragment)
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));

                } else if (id == R.id.nav_about) {
                    // Navigate to About Activity (or show an About Dialog)
                    startActivity(new Intent(HomeActivity.this, AboutActivity.class));

                } else if (id == R.id.nav_identify_disease) {
                    // Handle navigation to Identify Disease page
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));

                } else if (id == R.id.nav_logout) {
                    // Log out the user (clear session or redirect to login)
                    // Example: startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish(); // Close the current activity
                }

                // Close the navigation drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Handle the "Proceed to Identify Disease" button click
        proceedButton.setOnClickListener(v -> {
            // Navigate to the Identify Disease Activity
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });
    }
}
