package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.cscorner.buddyin.admin.AdminApprovedFragment;
import com.cscorner.buddyin.admin.AdminNotesFragment;
import com.cscorner.buddyin.admin.AdminPendingFragment;
import com.cscorner.buddyin.admin.AdminPostFragment;
import com.cscorner.buddyin.admin.AdminProfileFragment;
import com.cscorner.buddyin.admin.AdminRejectedFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminHomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Button logout;
    private DrawerLayout drawerLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar); //Ignore red line errors
        setSupportActionBar(toolbar);
        // Set toolbar title here
        getSupportActionBar().setTitle("BuddyIN Admin");
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminPendingFragment()).commit();
            navigationView.setCheckedItem(R.id.drawer_pending);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.drawer_pending) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminPendingFragment()).commit();
        } else if (id == R.id.drawer_viewlecture) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminApprovedFragment()).commit();
        } else if (id == R.id.drawer_rejectlecture) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminRejectedFragment()).commit();
        } else if (id == R.id.drawer_post) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminPostFragment()).commit();
        } else if (id == R.id.drawer_notes) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminNotesFragment()).commit();
        } else if (id == R.id.drawer_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminProfileFragment()).commit();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }




//        logout = findViewById(R.id.logout);
//
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut(); // Sign out the user
//                // Redirect to a login activity
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });

}