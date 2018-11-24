package com.portalesco.daniel7byte.geotracking;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText fTextEMail, fTextPassword;
    Button fButtonLogin, fButtonRegister;
    ProgressBar fProgressBar;

    FirebaseAuth fAuth;
    FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias obetos
        fTextEMail = findViewById(R.id.editTextEMail);
        fTextPassword = findViewById(R.id.editTextPassword);
        fButtonLogin = findViewById(R.id.buttonEntrar);
        fButtonRegister = findViewById(R.id.buttonRegistro);
        fProgressBar = findViewById(R.id.progressBar);
        fProgressBar.setVisibility(View.INVISIBLE);

        // Objeto Auth
        fAuth = FirebaseAuth.getInstance();

        //Listener
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = fAuth.getCurrentUser();

                //Verifica la existencia de un usuario logueado
                if (user == null){
                    fButtonLogin.setVisibility(View.VISIBLE);
                }else{ ;
                    fButtonLogin.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "YA ESTÁS LOGUEADO", Toast.LENGTH_LONG).show();
                    goToHome();
                }
            }
        };

        fButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingresar();
            }
        });

        fButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });
    }

    private void ingresar() {

        //Obtiene el texto
        String email = fTextEMail.getText().toString();
        String password = fTextPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){

            fProgressBar.setVisibility(View.VISIBLE);

            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "CORRECTO", Toast.LENGTH_LONG).show();
                        goToHome();
                    }else{
                        Toast.makeText(getApplicationContext(), "INCORRECTO", Toast.LENGTH_LONG).show();
                    }
                    fProgressBar.setVisibility(View.INVISIBLE);

                }
            });
        }
    }

    private void goToHome() {
        Intent goToHome = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(goToHome);
        finish();
    }

    private void goToRegister() {
        Intent goToRegister = new Intent(MainActivity.this, NewUserActivity.class);
        startActivity(goToRegister);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null){
            fAuth.removeAuthStateListener(listener);
        }
    }
}
