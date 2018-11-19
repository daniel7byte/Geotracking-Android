package com.portalesco.daniel7byte.geotracking;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends AppCompatActivity {

    private static final String RUTA_IMAGEN = "Geotracking/myPhotos/";
    FirebaseAuth fAuth;
    FirebaseAuth.AuthStateListener listener;

    Button btnSend, btnFile, btnTakePhoto, btnGPS;
    EditText txtBusiness, txtNotes;
    TextView tvGPS;
    ImageView imgView;

    // Para el funcionamiento de Firebase
    private DatabaseReference fBusiness;
    private StorageReference fStorage;
    // Para el codigo del ACTIVITYACTIONSRESULTS de la galeria
    private static final int GALLERY_INTENT = 10;
    private static final int TAKE_PHOTO_INTENT = 20;

    private ProgressDialog fProgressDialog;

    // Para poder acceder en cualquier lugar
    public Uri uriImage;

    // Tomar Foto
    String pathImagenTomada;

    // Variables GPS
    // https://www.youtube.com/watch?v=XOF8aFU03ew (VIDEO)
    private static final int GPS_INTENT_COD = 30;
    public String lat, lon;

    public String emailUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GPS
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        // Verifica permisos GPS
        if(permissionCheck== PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_INTENT_COD);
            }
        }

        // Boton flotate
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton fabLists = (FloatingActionButton) findViewById(R.id.fabLists);
        // Acciones del Fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();
            }
        });
        fabLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLists();
            }
        });

        // Referencias a objetos
        btnSend = findViewById(R.id.buttonSend);
        btnFile = findViewById(R.id.buttonFile);
        btnTakePhoto = findViewById(R.id.buttonTakePhoto);
        btnGPS = findViewById(R.id.buttonGPS);
        txtBusiness = findViewById(R.id.editTextBusiness);
        txtNotes = findViewById(R.id.editTextNotes);
        tvGPS = findViewById(R.id.textViewGPS);
        imgView = findViewById(R.id.imageView);

        // Permisos TOMAR FOTO
        if (validarPermisos()) {
            btnTakePhoto.setEnabled(true);
        }else{
            btnTakePhoto.setEnabled(false);
        }

        // Dialogo de progreso
        fProgressDialog = new ProgressDialog(this);

        // Firebase
        fBusiness = FirebaseDatabase.getInstance().getReference();
        fStorage = FirebaseStorage.getInstance().getReference();

        // Login
        fAuth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user == null){
                    emailUser = null;
                    goToMain();
                } else {
                    emailUser = user.getEmail();
                    Toast.makeText(HomeActivity.this, emailUser, Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Boton File
        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileImage();
            }
        });

        // Boton Tomar foto
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFoto();
            }
        });

        // Crear registro
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarBusiness();
            }
        });

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_gps();
            }
        });
    }

    public void openFileImage() {
        //Abrir galeria de imagenes
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);
    }

    public void tomarFoto() {

        // Crear directorio
        File fileImagen = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGEN);
        boolean isCreada = fileImagen.exists();
        String nombreImagen = "";

        if (isCreada == false) {
            isCreada = fileImagen.mkdirs();
        }

        nombreImagen = (System.currentTimeMillis()/100) + ".jpg";

        // Fin de crear directorio

        pathImagenTomada = Environment.getExternalStorageDirectory()+
                File.separator+RUTA_IMAGEN+File.separator+nombreImagen;

        File imagen = new File(pathImagenTomada);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Validar para Android 7

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            String authorities = getApplicationContext().getPackageName()+".provider";
            uriImage = FileProvider.getUriForFile(this, authorities, imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
        }else{
            uriImage = Uri.fromFile(imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }

        startActivityForResult(intent, TAKE_PHOTO_INTENT);
    }

    private boolean validarPermisos() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        // (SE DEBEN IMPORTAR import static android.Manifest.permission.CAMERA; & import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;)
        if ((checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED)&&(checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)){
            return true;
        }

        // Solicitar permisos
        if ((shouldShowRequestPermissionRationale(CAMERA)) || (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
        }

        return false;
    }

    private void cargarDialogoRecomendacion(){
        AlertDialog.Builder dialogo = new AlertDialog.Builder(HomeActivity.this);
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App ");


        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Para que funcione debe existir la funcion onRequestPermissionsResult()
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
            }
        });

        dialogo.show();
    }

    private void  solicitarPermisosManual () {
        Toast.makeText(this, "Permiso maual", Toast.LENGTH_SHORT).show();
    }

    private void get_gps () {

        fProgressDialog.setTitle("GPS");
        fProgressDialog.setMessage("Obteniendo coordenadas");
        fProgressDialog.setCancelable(false);
        fProgressDialog.show();

        LocationManager locationManager = (LocationManager) HomeActivity.this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                fProgressDialog.dismiss();

                lat = location.getLatitude() + "";
                lon = location.getLongitude() + "";
                tvGPS.setText(lat + ", " + lon);
                Toast.makeText(HomeActivity.this, "Ubicacion actualizada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        int permissionCheck = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void registrarBusiness() {
        String business = txtBusiness.getText().toString();
        String notes = txtNotes.getText().toString();
        String image = null;

        if (!business.isEmpty()) {

            // Obtiene ID
            String idBusiness = fBusiness.push().getKey();

            if (uriImage != null) {
                // Crea un dialogo
                fProgressDialog.setTitle("Subiendo...");
                fProgressDialog.setMessage("Subiendo foto a Firebase");
                fProgressDialog.setCancelable(false);
                fProgressDialog.show();

                //Ruta imagen
                StorageReference filePath = fStorage.child("photos").child(uriImage.getLastPathSegment());
                image = uriImage.getLastPathSegment(); // Me regresa una string

                // Subir imagen
                filePath.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Cierra el dialogo
                        fProgressDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "Imagen Subida", Toast.LENGTH_LONG).show();
                    }
                });
            }

            // Objeto
            Business nBusiness = new Business(business, notes, image);
            // Tocaba así, porque el constructor no lee variables fuera del metodo, solo con los Setters
            nBusiness.setLat(this.lat);
            nBusiness.setLon(this.lon);
            nBusiness.setEmailUser(this.emailUser);
            fBusiness.child(idBusiness).setValue(nBusiness);

            Toast.makeText(this,"Empresa creada", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"Formulario vacio", Toast.LENGTH_LONG).show();
        }

        // Hace un formateo de todos los campos
        txtBusiness.setText("");
        txtNotes.setText("");
        //"@android:drawable/ic_menu_gallery"
        imgView.setImageResource(android.R.drawable.ic_menu_gallery);
        uriImage = null;
    }

    private void goToMain() {
        Intent goToMain = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(goToMain);
    }

    private void goToLists() {
        Intent goToLists = new Intent(HomeActivity.this, ListsActivity.class);
        startActivity(goToLists);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                btnTakePhoto.setEnabled(true);
            }else{
                solicitarPermisosManual();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Leer imagen
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            // Se guarda en la variable global
            uriImage = data.getData();

            // Poner el ImageView
            imgView.setImageURI(uriImage);
        }

        // Leer toma de imagen
        if (requestCode == TAKE_PHOTO_INTENT && resultCode == RESULT_OK) {
            MediaScannerConnection.scanFile(this, new String[]{pathImagenTomada}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    //Log.i("Ruta de almacenamiento", "Path"+pathImagenTomada);
                }
            });

            Bitmap bitmap = BitmapFactory.decodeFile(pathImagenTomada);
            imgView.setImageBitmap(bitmap);
        }
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
