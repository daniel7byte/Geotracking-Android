package com.portalesco.daniel7byte.geotracking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;

public class NewUserActivity extends AppCompatActivity {

    Button btnCrear;
    ProgressDialog progressDialogLoad;
    EditText edEmail, edPassword;

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        btnCrear = findViewById(R.id.buttonNewUser);
        progressDialogLoad = new ProgressDialog(this);
        edEmail = findViewById(R.id.editTextNewEMail);
        edPassword = findViewById(R.id.editTextNewPassword);

        fAuth = FirebaseAuth.getInstance();

        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = fAuth.getCurrentUser();

                //Verifica la existencia de un usuario logueado
                if (user != null){
                    Toast.makeText(getApplicationContext(), "LOGUEADO", Toast.LENGTH_LONG).show();
                    goToHome();
                }
            }
        };

        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newUser();
            }
        });
    }

    private void newUser() {

        final String email = edEmail.getText().toString();
        final String password = edPassword.getText().toString();

        progressDialogLoad.setTitle("Creando nuevo usuario");
        progressDialogLoad.setMessage("Esperando respuesta de Firebase");
        progressDialogLoad.setCancelable(false);
        progressDialogLoad.show();

        if (!email.isEmpty() && !password.isEmpty()){
            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(NewUserActivity.this, "ERROR EN EL REGISTRO", Toast.LENGTH_SHORT).show();
                    }
                    progressDialogLoad.dismiss();
                }
            });
        }

        progressDialogLoad.dismiss();
    }

    private void goToHome() {
        Intent goToHome = new Intent(this, HomeActivity.class);
        startActivity(goToHome);
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
