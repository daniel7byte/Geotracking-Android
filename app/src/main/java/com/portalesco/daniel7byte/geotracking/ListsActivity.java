package com.portalesco.daniel7byte.geotracking;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;

public class ListsActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();
            }
        });
        FloatingActionButton fabHome = (FloatingActionButton) findViewById(R.id.fabHome);
        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHome();
            }
        });

        fAuth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user == null){
                    goToMain();
                }
            }
        };
    }

    private void goToHome() {
        Intent goToHome = new Intent(ListsActivity.this, HomeActivity.class);
        startActivity(goToHome);
        finish();
    }

    private void goToMain() {
        Intent goToMain = new Intent(ListsActivity.this, MainActivity.class);
        startActivity(goToMain);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(listener);

        // Cargar datos
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null){
            fAuth.removeAuthStateListener(listener);
        }
    }

}
