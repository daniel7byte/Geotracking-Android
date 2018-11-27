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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ListsActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseAuth.AuthStateListener listener;
    private DatabaseReference mBusinessReference;

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

        mBusinessReference = FirebaseDatabase.getInstance().getReference()
                .child("-LS5JUYEQP7AO8zfqJAc");

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

        ValueEventListener businessListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Business business = dataSnapshot.getValue(Business.class);
                // ...
                Toast.makeText(ListsActivity.this, business.business, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(ListsActivity.this, "loadPost:onCancelled " + databaseError.toException(), Toast.LENGTH_SHORT).show();
            }
        };
        mBusinessReference.addValueEventListener(businessListener);
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
