package com.FakarnyAppForTripReminder.Fakarny;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.FakarnyAppForTripReminder.Fakarny.database.Repository;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Intent intent;
    private final int RC_SIGN_IN = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
        boolean firstTime = shared.getBoolean("firstStart", true);
        if (!firstTime) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, DemoActivity.class);
        }
        mAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                Toast.makeText(SplashScreen.this, "Welcome " + firebaseUser.getEmail(), Toast.LENGTH_SHORT)
                        .show();
                startActivity(intent);
                finish();
            } else {
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build()
                        , new AuthUI.IdpConfig.GoogleBuilder().build()
                );
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.Theme_TripReminder)
                        .build(), RC_SIGN_IN);
            }
        };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Repository repository = new Repository(getApplication());
                repository.initDatabase();
                startActivity(intent);
                finish();
            } else {
                finish();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mAuth.addAuthStateListener(authStateListener);
            }
        }, 3000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(authStateListener);
    }
}